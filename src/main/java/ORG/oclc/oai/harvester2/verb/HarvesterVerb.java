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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;
import javax.xml.stream.XMLStreamException;
import nl.mpi.oai.harvester.utils.DocumentSource;
import nl.mpi.oai.harvester.utils.MarkableFileInputStream;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.evt.XMLEvent2;

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
    private InputStream str = null;
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
    
    public boolean hasStream() {
        return (str!=null);
    }
    
    public boolean hasDocument() {
        return (doc!=null);
    }
    
    public DocumentSource getDocumentSource() {
        if (hasDocument())
            return new DocumentSource(requestURL,doc);
        return new DocumentSource(requestURL,getStream());
    }
    
    /**
     * Get the OAI response as a stream
     * 
     * @return the InputStream for the OAI response
     */
    public InputStream getStream() {
        if (hasStream()) {
            try {
                str.reset();
            } catch (IOException ex) {
                logger.warn("stream for '"+requestURL+"' could not be reset!");
            }
        }
        return str;
    }
    
    public InputSource getSource() {
        return new InputSource(getStream());
    }
    
    /**
     * Get the OAI response as a DOM object
     * 
     * @return the DOM for the OAI response
     */
    public Document getDocument() throws ParserConfigurationException, SAXException, IOException {
        if (doc == null) {
            Thread t = Thread.currentThread();
            DocumentBuilder builder = (DocumentBuilder) builderMap.get(t);
            if (builder == null) {
                builder = factory.newDocumentBuilder();
                builderMap.put(t, builder);
            }
            doc = builder.parse(getSource());
            str = null;
            logger.debug("switched from stream to tree for request["+requestURL+"]",new Throwable());
        }
        return doc;
    }
    
    /**
     * Get the xsi:schemaLocation for the OAI response
     * 
     * @return the xsi:schemaLocation value
     */
    public String getSchemaLocation() throws TransformerException, ParserConfigurationException, SAXException, IOException, XMLStreamException {
        if (this.schemaLocation == null) {
            if (hasDocument()) {
                this.schemaLocation = getSingleString("/*/@xsi:schemaLocation");
                logger.debug("found schemaLocation["+schemaLocation+"] in the XML tree");
            } else {
                XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
                xmlif.configureForConvenience();
                XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(getStream());
                int state = 1; // 1:START 0:STOP -1:ERROR
                while (state > 0) {
                    int eventType = xmlr.getEventType();
                    switch (eventType) {
                        case XMLEvent2.START_ELEMENT:
                            schemaLocation = xmlr.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","schemaLocation");
                            if (schemaLocation != null)
                                state = 0;
                            break;
                    }
                    if (xmlr.hasNext())
                        xmlr.next();
                    else
                        state = state == 1? 0: -1;// if START then STOP else ERROR
                }
                xmlr.close();
                logger.debug("found schemaLocation["+schemaLocation+"] in the XML stream");
            }

            // The URIs in xsi:schemaLocation are separated by (any kind
            // of) white space. Normalize it to a single space.
            this.schemaLocation = schemaLocation.trim().replaceAll("\\s+", " ");
        }
        return schemaLocation;
    }
    
    /**
     * Get the OAI errors
     * @return a NodeList of /oai:OAI-PMH/oai:error elements
     * @throws TransformerException
     */
    public NodeList getErrors() throws TransformerException, ParserConfigurationException, SAXException, IOException, XMLStreamException {
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
        harvest(requestURL,0,null);
    }
    
    public HarvesterVerb(String requestURL,int timeout) throws IOException,
    ParserConfigurationException, SAXException, TransformerException {
        harvest(requestURL,timeout,null);
    }
    
    public HarvesterVerb(String requestURL,int timeout,Path temp) throws IOException,
    ParserConfigurationException, SAXException, TransformerException {
        harvest(requestURL,timeout,temp);
    }

    /**
     * Preforms the OAI request
     * 
     * @param requestURL
     * @param timeout
     * @param temp
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public void harvest(String requestURL, int timeout, Path temp) throws MalformedURLException, IOException {
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
            } catch(Exception e) {
                logger.error("couldn't connect to '"+requestURL+"': "+e.getMessage());
                throw e;
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
        logger.debug("Content-Encoding=" + contentEncoding);
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
        
        if (temp!=null) {
            FileOutputStream out = new FileOutputStream(temp.toFile());
            org.apache.commons.io.IOUtils.copy(in,out,1000000);
            out.close();
            logger.debug("temp["+temp+"] for URL["+requestURL+"]");
            str = new MarkableFileInputStream(new FileInputStream(temp.toFile()));
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int size = org.apache.commons.io.IOUtils.copy(in, baos);
            logger.debug("buffered ["+size+"] bytes for URL["+requestURL+"]");
            str = new ByteArrayInputStream(baos.toByteArray());
        }
    }
    
    /**
     * Get the String value for the given XPath location in the response DOM
     * 
     * @param xpath
     * @return a String containing the value of the XPath location.
     * @throws TransformerException
     */
    public String getSingleString(String xpath) throws TransformerException, ParserConfigurationException, SAXException, IOException {
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
    public NodeList getNodeList(String xpath) throws TransformerException, ParserConfigurationException, SAXException, IOException {
        return XPathAPI.selectNodeList(getDocument(), xpath, namespaceElement);
    }
    
    public String toString() {
        try {
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
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.error("document is invalid: " + ex);
        }
        return null;
    }
}
