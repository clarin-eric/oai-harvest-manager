/*
 * Copyright (C) 2014, The Max Planck Institute for
 * Psycholinguistics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License is included in the file
 * LICENSE-gpl-3.0.txt. If that file is missing, see
 * <http://www.gnu.org/licenses/>.
 */

package nl.mpi.oai.harvester;

import ORG.oclc.oai.harvester2.verb.GetRecord;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import ORG.oclc.oai.harvester2.verb.Identify;
import ORG.oclc.oai.harvester2.verb.ListIdentifiers;
import ORG.oclc.oai.harvester2.verb.ListMetadataFormats;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class represents a single OAI-PMH provider.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class Provider {
    private static final Logger logger = Logger.getLogger(Provider.class);

    /** Name of the provider. */
    private String name;

    /** Address through which the OAI repository is accessed. */
    private String oaiUrl;

    /** List of OAI sets to harvest (optional). */
    private String[] sets = null;

    /**
     * We make so many XPath queries we could just as well keep one XPath
     * object to hand for them.
     */
    private XPath xpath;

    /** Create provider with specified name and URL. */
    public Provider(String name, String oaiUrl) {
	init(oaiUrl);
	this.name = name;
    }

    /**
     * Create provider with specified URL. (The provider will be
     * queried for its name.)
     */
    public Provider(String url) {
	init(url);

	// Fetch name.
	name = getProviderName();

	// If we simply can't find a name, make one up.
	if (name == null || name.isEmpty()) {
	    String domain = oaiUrl.replaceAll(".*//([^/]+)/.*", "$1");
	    name = "Unnamed provider at " + domain;
	}
    }

    public void setSets(String[] sets) {
	this.sets = sets;
    }

    /**
     * Set the address (stripping off junk if necessary) and do other start-up
     * tasks.
     */
    private void init(String url) {
	// If the base URL is given with parameters (most often
	// ?verb=Identify), strip them off to get a uniform
	// representation.
	if (url.contains("?"))
	    url = url.substring(0, url.indexOf("?"));
	this.oaiUrl = url;

	XPathFactory xpf = XPathFactory.newInstance();
	xpath = xpf.newXPath();
    }

    /** Get name with all characters intact. */
    public String getName() {
	return name;
    }

    public String getOaiUrl() {
	return oaiUrl;
    }

    /**
     * Get the name declared by an OAI-PMH provider by making an
     * Identify request. Returns null if no name can be found.
     */
    private String getProviderName() {
	try {
	    Identify ident = new Identify(oaiUrl);
//	    NodeList name = (NodeList)xpath.evaluate("//*[contains(local-name(),'repositoryName')]/text()",
	    NodeList name = (NodeList)xpath.evaluate("//*[local-name() = 'repositoryName']/text()",
						     ident.getDocument(), XPathConstants.NODESET);
	    if (name != null && name.getLength() > 0) {
		String provName = name.item(0).getNodeValue();
		logger.info("Contacted " + oaiUrl + " to get its name, received: \"" + provName + "\"");
		return provName;
	    }
	} catch (TransformerException | XPathExpressionException
		| ParserConfigurationException | SAXException | IOException e) {
	    logger.error(e.getMessage(), e);
	}
	logger.info("Contacted " + oaiUrl + " to get its name but received nothing");
	return null;
    }

    /**
     * Attempt to perform the specified sequence of actions on metadata from
     * this provider (which, of course, is only possible if this provider
     * supports the specified input format(s)).
     * 
     * If the sequence can be performed, this method will start a new thread
     * to do so and return true. Otherwise no action will be taken and false
     * will be returned.
     * 
     * @param ap the sequence of actions
     */
    public boolean performActions(ActionSequence ap) {
	List<String> prefixes = getPrefixes(ap.getInputFormat());
	if (prefixes.isEmpty()) {
	    logger.info("No matching prefixes for format "
		    + ap.getInputFormat());
	    return false;
	}

	// Fetch list of record identifiers separately for each metadata
	// prefix corresponding to the metadata format, then collate all in
	// a single hash (id as key, metadata prefix as value).
	Map<String, String> identifiers = new HashMap<>();
	for (String prefix : prefixes) {
	    List<String> ids;
	    try {
		ids = getIdentifiers(prefix);
	    } catch (ParserConfigurationException | IOException | SAXException
		    | TransformerException | XPathExpressionException
		    | NoSuchFieldException ex) {
		logger.error("Error fetching ids from " + name, ex);
		return false;
	    }
	    for (String id : ids) {
		identifiers.put(id, prefix);
	    }
	}

	for (Map.Entry<String, String> me : identifiers.entrySet()) {
	    MetadataRecord rec = getRecord(me.getKey(), me.getValue());
	    if (rec != null) {
		logger.info("Fetched record " + me.getKey()
			+ " (format " + me.getValue() + ")");
		ap.runActions(rec);
	    }
	}

	return true;
    }

    /**
     * Fetch a single record from this provider.
     * 
     * @param id OAI-PMH identifier of the record
     * @param mdPrefix metadata prefix
     * @return the record, or null if it cannot be fetched
     */
    public MetadataRecord getRecord(String id, String mdPrefix) {
	int maxRetry = Main.getConfig().getMaxRetryCount();
	for (int i=0; i<maxRetry; i++) {
	    try {
		GetRecord gr = new GetRecord(oaiUrl, id, mdPrefix);
		Document doc = gr.getDocument();
		return new MetadataRecord(id, doc, this);
	    } catch (IOException | SAXException | ParserConfigurationException
		    | TransformerException e) {
		logger.error(e);
	    }
	}
	return null;
    }

    /**
     * Make an OAI-PMH GetIdentifiers call to collect all identifiers available
     * with the given metadata prefix from this provider.
     * 
     * @param mdPrefix metadata prefix
     * @return list of identifiers, which may be empty
     */
    public List<String> getIdentifiers(String mdPrefix) throws IOException,
	    ParserConfigurationException, SAXException, TransformerException,
	    XPathExpressionException, NoSuchFieldException {
	List<String> ids = new ArrayList<>();

	if (sets == null) {
	    ListIdentifiers li = new ListIdentifiers(oaiUrl, null, null, null, mdPrefix);
	    addIdentifiers(li, ids);
	} else {
	    for (String set : sets) {
		ListIdentifiers li = new ListIdentifiers(oaiUrl, null, null, set, mdPrefix);
		addIdentifiers(li, ids);
	    }
	}

	return ids;
    }

    /**
     * Make an OAI-PMH GetIdentifiers call to collect all identifiers available
     * with the given metadata prefix from this provider and add them to the
     * given list.
     * 
     * @param li OAI-PMH connection object
     * @param ids a list, already created, that identifiers will be added to
     */
    private void addIdentifiers(ListIdentifiers li, List<String> ids) throws IOException,
	    ParserConfigurationException, SAXException, TransformerException,
	    XPathExpressionException, NoSuchFieldException {
	for (;;) {
	    Document doc = li.getDocument();
	    NodeList nl = (NodeList)xpath.evaluate("//*[starts-with(local-name(),'identifier') and parent::*[local-name()='header' and not(@status='deleted')]]/text()",
		    doc, XPathConstants.NODESET);
	    if (nl == null || nl.getLength() == 0) {
		break;
	    }

	    for (int j = 0; j < nl.getLength(); j++) {
		String currId = nl.item(j).getNodeValue();
		ids.add(currId);
	    }

	    String resumption = li.getResumptionToken();
	    if (resumption == null || resumption.isEmpty()) {
		break;
	    }
	    li = new ListIdentifiers(oaiUrl, resumption);
	}
	
    }
    
    /**
     * Get the list of metadata prefixes corresponding to the specified format
     * that are supported by this provider.
     */
    private List<String> getPrefixes(MetadataFormat format) {
	List<String> prefs = new ArrayList<>();

	logger.debug("Checking format " + format);
	try {
	    ListMetadataFormats lmf = new ListMetadataFormats(oaiUrl);
	    NodeList formats = (NodeList)xpath.evaluate("//*[local-name() = 'metadataFormat']",
		    lmf.getDocument(), XPathConstants.NODESET);

	    if (formats == null) {
		logger.warn("Provider's ListMetadataFormats response looks empty");
		return Collections.emptyList();
	    }

	    for (int i=0; i<formats.getLength(); i++) {
		Node s = formats.item(i);
		String prefix = Util.getNodeText(xpath, "./*[local-name() = 'metadataPrefix']/text()", s);
		String schema = Util.getNodeText(xpath, "./*[local-name() = 'schema']/text()", s);
		String ns = Util.getNodeText(xpath, "./*[local-name() = 'metadataNamespace']/text()", s);
		String comp;
		if ("prefix".equals(format.getType())) {
		    comp = prefix;
		} else if ("schema".equals(format.getType())) {
		    comp = schema;
		} else if ("namespace".equals(format.getType())) {
		    comp = ns;
		} else {
		    logger.error("Unknown match type " + format.getType());
		    return null;
		}
		if (format.getValue().equals(comp)) {
		    logger.debug("Found suitable prefix: " + prefix);
		    prefs.add(prefix);
		}
	    }
	    return prefs;
	} catch (TransformerException | XPathExpressionException
		| ParserConfigurationException | SAXException | IOException e) {
	    logger.error(e.getMessage(), e);
	}
	return Collections.emptyList();
    }

    /**
     * Check if name matches given string (whether in filesystem
     * format or not).
     */
    public boolean matches(String name) {
	return this.name.equals(Util.toFileFormat(name));
    }


    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder(name);
	if (sets != null) {
	    sb.append(" (only set(s):");
	    for (String s : sets) {
		sb.append(" ").append(s);
	    }
	    sb.append(")");
	}
	sb.append(" @ ").append(oaiUrl);
	return sb.toString();
    }
}
