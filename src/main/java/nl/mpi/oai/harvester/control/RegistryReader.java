/*
 * Copyright (C) 2015, The Max Planck Institute for
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

package nl.mpi.oai.harvester.control;

import nl.mpi.oai.harvester.metadata.NSContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class reads information from the REST service of the CLARIN Centre
 * Registry (see http://www.clarin.eu/content/centres for more information). 
 *
 * @author Lari Lampen (MPI-PL)
 */
public class RegistryReader {
    private static final Logger logger = LogManager.getLogger(RegistryReader.class);
    private final XPath xpath;

    /** Create a new registry reader object. */
    public RegistryReader() {
	XPathFactory xpf = XPathFactory.newInstance();
	xpath = xpf.newXPath();
	NSContext nsContext = new NSContext();
	nsContext.add("cmd", "http://www.clarin.eu/cmd/");
	xpath.setNamespaceContext(nsContext);
    }

    /**
     * Get a list of all OAI-PMH endpoint URLs defined in the
     * specified registry.
     * 
     * @param registryUrl url of the registry endpoint
     * @return list of all OAI-PMH endpoint URLs
     */
    public List<String> getEndpoints(URL registryUrl) {
	// Basically this makes a simple REST call to get a list of
	// addresses for a further batch of REST calls. This is not
	// documented in detail since it's specific to the CLARIN
	// registry implementation anyway.
	List<String> endpoints = new ArrayList<>();
	try {
	    Document doc = openRemoteDocument(registryUrl);
	    List<String> provUrls = getProviderInfoUrls(doc);

	    logger.info("Fetching information on " + provUrls.size()
		    + " centres");
	    for (String centreInfoUrl : provUrls) {
		doc = openRemoteDocument(new URL(centreInfoUrl));
		NodeList ends = getEndpoints(doc);
		if (ends != null) {
                    for (int i =0;i<ends.getLength();i++)
                        endpoints.add(ends.item(i).getNodeValue().trim());
		}
	    }
	} catch (IOException | ParserConfigurationException | SAXException
		| XPathExpressionException | DOMException e) {
	    logger.error("Error reading from centre registry", e);
	}
	return endpoints;
    }
    
    
    public Map<String, Collection<CentreRegistrySetDefinition>> getEndPointOaiPmhSetMap(URL registryUrl) {
	// Basically this makes a simple REST call to get a list of
	// addresses for a further batch of REST calls. This is not
	// documented in detail since it's specific to the CLARIN
	// registry implementation anyway.
	final Map<String, Collection<CentreRegistrySetDefinition>> map = new HashMap<>();
	try {
            final Document centresDoc = openRemoteDocument(registryUrl);
            final List<String> provUrls = getProviderInfoUrls(centresDoc);
            
	    logger.info("Fetching information on " + provUrls.size()
		    + " centres");
            
	    for (String centreInfoUrl : provUrls) {
		final Document centreDoc = openRemoteDocument(new URL(centreInfoUrl));
		final NodeList endpointsList = getEndpoints(centreDoc);
		if (endpointsList != null) {
                    for (int i =0;i<endpointsList.getLength();i++) {
                        final String endpoint = endpointsList.item(i).getNodeValue().trim();
                        final Set<CentreRegistrySetDefinition> sets = getOaiPmhSetsForEndpoint(centreDoc, endpoint);
                        map.put(endpoint, sets);
                    }
		}
	    }
	} catch (IOException | ParserConfigurationException | SAXException
		| XPathExpressionException | DOMException e) {
	    logger.error("Error reading from centre registry", e);
	}
	return map;
    }

    /**
     * Extract links to all provider information pages from the summary
     * document returned by the centre registry
     * 
     * @param doc center registry cycle response
     * @return list of URLs of provider-specific info pages
     * @throws XPathExpressionException problem with the paths to query the center registry response
     */
    public List<String> getProviderInfoUrls(Document doc) throws XPathExpressionException {
	if (doc == null) {
	    logger.warn("The centre registry response is missing");
	    return Collections.emptyList();
	}

	NodeList centres = (NodeList) xpath.evaluate("/Centers/CenterProfile/Center_id_link/text()",
		doc.getDocumentElement(), XPathConstants.NODESET);
	List<String> provUrls = new ArrayList<>();
	for (int j=0; j<centres.getLength(); j++) {
	    String provUrl = centres.item(j).getNodeValue();
	    if (provUrl != null)
		provUrls.add(provUrl);
	}
	return provUrls;
    }

    /**
     * Extract the OAI-PMH endpoint of a single provider from its description
     * document.
     * 
     * @param providerInfo xml information from the center registry
     * @return endpoint URL, or null if none available
     * @throws XPathExpressionException problem with the paths to query the center registry response
     */
    public NodeList getEndpoints(Document providerInfo) throws XPathExpressionException {
	if (providerInfo == null)
	    return null;

	NodeList endpoints = (NodeList) xpath.evaluate("/cmd:CMD/cmd:Components/cmd:CenterProfile/cmd:CenterExtendedInformation/cmd:Metadata/cmd:OaiAccessPoint/text()",
		providerInfo.getDocumentElement(), XPathConstants.NODESET);
	return endpoints;
    }

    private Set<CentreRegistrySetDefinition> getOaiPmhSetsForEndpoint(final Document centreDoc, final String endpoint) throws XPathExpressionException, DOMException {
        Set<CentreRegistrySetDefinition> sets = new HashSet<>();
        final NodeList setList = getOaiPmhSets(centreDoc, endpoint);
        if(setList == null) {
            logger.debug("No set list for endpoint {}", endpoint);
        } else {
            for(int s=0;s<setList.getLength();s++) {
                String setSpec = null;
                String setType = null;
                
                final NodeList setNodeProps = setList.item(s).getChildNodes();
                for(int p=0; p<setNodeProps.getLength(); p++) {
                    switch(setNodeProps.item(p).getNodeName()) {
                        case "SetSpec":
                            setSpec = setNodeProps.item(p).getTextContent();
                            logger.debug("{{}} SetSpec={}", endpoint, setSpec);
                            break;
                        case "SetType":
                            setType = setNodeProps.item(p).getTextContent();
                            logger.debug("{{}} SetType={}", endpoint, setType);
                            break;
                    }
                }
                if(setSpec != null && setType != null) {
                    sets.add(new CentreRegistrySetDefinition(setSpec, setType));
                }
            }
        }
        return sets;
    }
    
    /**
     * Extract the OAI-PMH sets for a single endpoint from aprovider's description
     * document.
     * 
     * @param providerInfo xml information from the center registry
     * @param endpoint endpoint URL
     * @return 0 or more "Set" nodes containing "SetSpec" and "SetType" child elements
     * @throws XPathExpressionException problem with the paths to query the center registry response
     */
    public NodeList getOaiPmhSets(Document providerInfo, String endpoint) throws XPathExpressionException {
        if (providerInfo == null)
	    return null;

	NodeList sets = (NodeList) xpath.evaluate("/cmd:CMD/cmd:Components/cmd:CenterProfile/cmd:CenterExtendedInformation/cmd:Metadata[cmd:OaiAccessPoint='" + endpoint +"']/cmd:OaiPmhSets/cmd:Set",
		providerInfo.getDocumentElement(), XPathConstants.NODESET);
	return sets;
    }
    
    /**
     * Fetch the XML document located at the given URL, parse it, and
     * return the resulting DOM tree.
     */
    private static Document openRemoteDocument(URL url) throws IOException,
	    ParserConfigurationException, SAXException {
	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	connection.setInstanceFollowRedirects(false);
	connection.setRequestMethod("GET");
	connection.setRequestProperty("Content-Type", "application/xml");
	connection.connect();
        
	connection.getResponseCode();
        
        Boolean redirect = false;

        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
            }
        }
        
	if (redirect) {
 
            // get redirect url from "location" header field
            String newUrl = connection.getHeaderField("Location");

            // open the new connnection again
            
            connection = (HttpURLConnection) new URL(newUrl).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/xml");
            
            logger.debug("Redirect to URL : " + newUrl);            
            logger.debug(System.getProperty("java.runtime.version"));
            
            connection.connect();
            
            connection.getResponseCode();
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(connection.getInputStream());
    }
}
