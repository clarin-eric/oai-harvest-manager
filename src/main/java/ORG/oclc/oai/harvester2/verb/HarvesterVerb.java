/**
 Copyright 2006 OCLC, Online Computer Library Center
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 * 
 * 20151104 - maw - added connection timeout
*/

package ORG.oclc.oai.harvester2.verb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 * 
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract class HarvesterVerb {
    private static Logger logger = LogManager.getLogger(HarvesterVerb.class);

    /* Primary OAI namespaces */
    public static final String SCHEMA_LOCATION_V2_0 = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
    public static final String SCHEMA_LOCATION_V1_1_GET_RECORD = "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd";
    public static final String SCHEMA_LOCATION_V1_1_IDENTIFY = "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_IDENTIFIERS = "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS = "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_RECORDS = "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd";
    public static final String SCHEMA_LOCATION_V1_1_LIST_SETS = "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd";
    private Document doc = null;
    private String schemaLocation = null;
    private String requestURL = null;
    private static HashMap builderMap = new HashMap();
    private static Element namespaceElement = null;
    private static DocumentBuilderFactory factory = null;
    private static TransformerFactory xformFactory = TransformerFactory.newInstance();
    
    static {
    	try {
	        /* Load DOM Document */
	        factory = DocumentBuilderFactory
	        .newInstance();
	        factory.setNamespaceAware(true);
	        Thread t = Thread.currentThread();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        builderMap.put(t, builder);
	        
	        DOMImplementation impl = builder.getDOMImplementation();
	        Document namespaceHolder = impl.createDocument(
	                "http://www.oclc.org/research/software/oai/harvester",
	                "harvester:namespaceHolder", null);
	        namespaceElement = namespaceHolder.getDocumentElement();
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:harvester",
	        "http://www.oclc.org/research/software/oai/harvester");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai20", "http://www.openarchives.org/OAI/2.0/");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_GetRecord",
	        "http://www.openarchives.org/OAI/1.1/OAI_GetRecord");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_Identify",
	        "http://www.openarchives.org/OAI/1.1/OAI_Identify");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_ListIdentifiers",
	        "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers");
	        namespaceElement
	        .setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_ListMetadataFormats",
	        "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_ListRecords",
	        "http://www.openarchives.org/OAI/1.1/OAI_ListRecords");
	        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
	                "xmlns:oai11_ListSets",
	        "http://www.openarchives.org/OAI/1.1/OAI_ListSets");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Get the OAI response as a DOM object
     * 
     * @return the DOM for the OAI response
     */
    public Document getDocument() {
        return doc;
    }
    
    /**
     * Get the xsi:schemaLocation for the OAI response
     * 
     * @return the xsi:schemaLocation value
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    /**
     * Get the OAI errors
     * @return a NodeList of /oai:OAI-PMH/oai:error elements
     * @throws TransformerException
     */
    public NodeList getErrors() throws TransformerException {
        if (SCHEMA_LOCATION_V2_0.equals(getSchemaLocation())) {
            return getNodeList("/oai20:OAI-PMH/oai20:error");
        } else {
            return null;
        }
    }
    
    /**
     * Get the OAI request URL for this response
     * @return the OAI request URL as a String
     */
    public String getRequestURL() {
        return requestURL;
    }
    
    /**
     * Mock object creator (for unit testing purposes)
     */
    public HarvesterVerb() {
    }
    
    /**
     * Performs the OAI request
     * 
     * @param requestURL
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public HarvesterVerb(String requestURL) throws IOException,
    ParserConfigurationException, SAXException, TransformerException {
        harvest(requestURL,0);
    }
    
    public HarvesterVerb(String requestURL,int timeout) throws IOException,
    ParserConfigurationException, SAXException, TransformerException {
        harvest(requestURL,timeout);
    }
    
    /**
     * Preforms the OAI request
     * 
     * @param requestURL
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public void harvest(String requestURL, int timeout) throws IOException,
    ParserConfigurationException, SAXException, TransformerException {
        this.requestURL = requestURL;
        logger.debug("requestURL=" + this.requestURL);
        InputStream in = null;
        URL url = new URL(this.requestURL);
        HttpURLConnection con = null;
        int responseCode = 0;
        do {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "OAIHarvester/2.0");
            con.setRequestProperty("Accept-Encoding",
            "compress, gzip, identify");
            if (timeout > 0) {
                logger.debug("timeout=" + timeout);
                con.setConnectTimeout(timeout*1000);
                con.setReadTimeout(timeout*1000);
            }
            try {
                responseCode = con.getResponseCode();
                logger.debug("responseCode=" + responseCode);
            } catch (FileNotFoundException e) {
                // assume it's a 503 response
                logger.info(requestURL, e);
                responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
            }
            
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                this.requestURL = con.getHeaderField("Location");
                logger.debug("redirect to requestURL=" + this.requestURL);
                url = new URL(this.requestURL);
                responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
            } else if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
                long retrySeconds = con.getHeaderFieldInt("Retry-After", -1);
                if (retrySeconds == -1) {
                    long now = (new Date()).getTime();
                    long retryDate = con.getHeaderFieldDate("Retry-After", now);
                    retrySeconds = retryDate - now;
                }
                if (retrySeconds == 0) { // Apparently, it's a bad URL
                    throw new FileNotFoundException("Bad URL?");
                }
                logger.debug("Retry-After=" + retrySeconds);
                if (retrySeconds > 0) {
                    try {
                        Thread.sleep(retrySeconds * 1000);
                    } catch (InterruptedException ex) {
                        logger.error(ex);
                    }
                }
            }
        } while (responseCode == HttpURLConnection.HTTP_UNAVAILABLE);
        String contentEncoding = con.getHeaderField("Content-Encoding");
        logger.debug("contentEncoding=" + contentEncoding);
        if ("compress".equals(contentEncoding)) {
            ZipInputStream zis = new ZipInputStream(con.getInputStream());
            zis.getNextEntry();
            in = zis;
        } else if ("gzip".equals(contentEncoding)) {
            in = new GZIPInputStream(con.getInputStream());
        } else if ("deflate".equals(contentEncoding)) {
            in = new InflaterInputStream(con.getInputStream());
        } else {
            in = con.getInputStream();
        }
        
        InputSource data = new InputSource(in);
        
        Thread t = Thread.currentThread();
        DocumentBuilder builder = (DocumentBuilder) builderMap.get(t);
        if (builder == null) {
            builder = factory.newDocumentBuilder();
            builderMap.put(t, builder);
        }
        doc = builder.parse(data);

        // The URIs in xsi:schemaLocation are separated by (any kind
        // of) white space. Normalize it to a single space.
        String schemaLoc = getSingleString("/*/@xsi:schemaLocation");
        this.schemaLocation = schemaLoc.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Get the String value for the given XPath location in the response DOM
     * 
     * @param xpath
     * @return a String containing the value of the XPath location.
     * @throws TransformerException
     */
    public String getSingleString(String xpath) throws TransformerException {
        return getSingleString(getDocument(), xpath);
//        return XPathAPI.eval(getDocument(), xpath, namespaceElement).str();
//      String str = null;
//      Node node = XPathAPI.selectSingleNode(getDocument(), xpath,
//      namespaceElement);
//      if (node != null) {
//      XObject xObject = XPathAPI.eval(node, "string()");
//      str = xObject.str();
//      }
//      return str;
    }
    
    public String getSingleString(Node node, String xpath)
    throws TransformerException {
        return XPathAPI.eval(node, xpath, namespaceElement).str();
    }
    
    /**
     * Get a NodeList containing the nodes in the response DOM for the specified
     * xpath
     * @param xpath
     * @return the NodeList for the xpath into the response DOM
     * @throws TransformerException
     */
    public NodeList getNodeList(String xpath) throws TransformerException {
        return XPathAPI.selectNodeList(getDocument(), xpath, namespaceElement);
    }
    
    public String toString() {
        // Element docEl = getDocument().getDocumentElement();
        // return docEl.toString();
        Source input = new DOMSource(getDocument());
        StringWriter sw = new StringWriter();
        Result output = new StreamResult(sw);
        try {
        	Transformer idTransformer = xformFactory.newTransformer();
            idTransformer.setOutputProperty(
                    OutputKeys.OMIT_XML_DECLARATION, "yes");
            idTransformer.transform(input, output);
            return sw.toString();
        } catch (TransformerException e) {
            return e.getMessage();
        }
    }
}
