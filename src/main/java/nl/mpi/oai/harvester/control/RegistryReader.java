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

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import nl.mpi.oai.harvester.metadata.NSContext;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 * This class reads information from the REST service of the CLARIN Centre
 * Registry (see http://www.clarin.eu/content/centres for more information). 
 *
 * @author Lari Lampen (MPI-PL)
 */
public class RegistryReader {
    private static final Logger logger = Logger.getLogger(RegistryReader.class);
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
	    for (String providerInfoUrl : provUrls) {
		doc = openRemoteDocument(new URL(providerInfoUrl));
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
	return (endpoints == null) ? null : endpoints;
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
        
	int responseCode = connection.getResponseCode();
        
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

            // get the cookie if need, for login
            String cookies = connection.getHeaderField("Set-Cookie");

            // open the new connnection again
            
            connection = (HttpURLConnection) new URL(newUrl).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/xml");
            
            System.out.println("Redirect to URL : " + newUrl);
            
            System.out.println(System.getProperty("java.runtime.version"));
            
            connection.connect();
            
            responseCode = connection.getResponseCode();
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(connection.getInputStream());
    }
}
