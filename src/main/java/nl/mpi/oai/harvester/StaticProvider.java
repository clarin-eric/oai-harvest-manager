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

import ORG.oclc.oai.harvester2.verb.Identify;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class represents a static OAI-PMH provider as defined by the <a
 * href="http://www.openarchives.org/OAI/2.0/guidelines-static-repository.htm">
 * static provider specification</a>. Note that the operations here do not
 * strictly conform to the OAI-PMH specification (which stipulates that static
 * providers should be accessed via an intermediary), so this should be viewed
 * as a superset of standard OAI-PMH functionality.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class StaticProvider extends Provider {
    private static final Logger logger = Logger.getLogger(StaticProvider.class);

    /**
     * The entire content of a static provider can be fitted into a single DOM
     * tree, which is exactly what we're doing here.
     */
    private Document providerContent = null;

    /**
     * Create new static provider with the specified URL.
     * 
     * @param oaiUrl endpoint
     */
    public StaticProvider(String oaiUrl) {
	super(oaiUrl);
    }

    /**
     * Create a new static provider based on the provided contents.
     * 
     * @param doc DOM tree representing the provider's content
     */
    public StaticProvider(Document doc) {
	super(null);
	providerContent = doc;
    }

    @Override
    public void init() {
	fetchContent();
	super.init();
    }

    /**
     * Extract a subtree from the provider's content.
     * 
     * @param xp XPath expression for extraction
     * @return a new document containing a subtree
     */
    private Document getSubtree(String xp) {
	try {
	    NodeList list = (NodeList)xpath.evaluate(xp, providerContent,
		    XPathConstants.NODESET);

	    if (list == null || list.getLength() == 0) {
		logger.error("No subtree matching '" + xp
			+ "'. Probably an error in provider content.");
		return null;
	    }

	    Node el = list.item(0);
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.newDocument();
	    doc.appendChild(doc.importNode(el, true));

	    return doc;
	} catch (XPathExpressionException | ParserConfigurationException e) {
	    logger.error(e.getMessage(), e);
	}
	return null;
    }

    @Override
    protected String getProviderName() {
	Document doc = getSubtree("/os:Repository/os:Identify");
	return parseProviderName(doc);
    }

    @Override
    List<String> getPrefixes(MetadataFormat format) {
	try {
	    Document doc = getSubtree("/os:Repository/os:ListMetadataFormats");
	    return parsePrefixes(providerContent, format);
	} catch (XPathExpressionException e) {
	    logger.error(e.getMessage(), e);
	}
	return Collections.emptyList();
    }

    @Override
    public List<String> getIdentifiers(String mdPrefix) throws IOException,
	    ParserConfigurationException, SAXException, TransformerException,
	    XPathExpressionException, NoSuchFieldException {
	Document doc = getSubtree("/os:Repository/os:ListRecords[@metadataPrefix = '"+mdPrefix+"']");
	List<String> ids = new ArrayList<>();
	addIdentifiers(doc, ids);
	return ids;
    }

    @Override
    public MetadataRecord getRecord(String id, String mdPrefix) {
	String xp = "/os:Repository/os:ListRecords[@metadataPrefix = '"
		+ mdPrefix + "']/oai:record[./oai:header/oai:identifier/text() = '"
		+ id + "']";
	Document doc = getSubtree(xp);
	if (doc == null) return null;
	return new MetadataRecord(id, doc, this);
    }

    /**
     * Fetch the content of the static provider and put it in providerContent.
     */
    private void fetchContent() {
	if (providerContent == null && oaiUrl != null) {
	    try {
		Identify ident = new Identify(oaiUrl);
		providerContent = ident.getDocument();
	    } catch (IOException | ParserConfigurationException | SAXException
		    | TransformerException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    @Override
    public String toString() {
	return name + " (static) @ " + oaiUrl;
    }
}