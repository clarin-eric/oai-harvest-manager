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

package nl.mpi.oai.harvester.harvesting;

import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.metadata.MetadataFormat;
import nl.mpi.oai.harvester.metadata.MetadataInterface;
import nl.mpi.oai.harvester.metadata.NSContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * <br> Help mocking the OAI protocol by supplying the XML document part of OAI
 * responses <br><br>
 *
 * While the harvesting package tests themselves should create the metadata
 * format, and the action sequences, and set up mocking and spying, the helper
 * provides a fixed set of XML documents that can mock part of real OAI
 * responses. <br><br>
 *
 * A TestHelper object contains a table for keeping track of the relation
 * between endpoint, prefix and record identifier. By initialising the table
 * with rows that are true to the predefined response documents, removing each
 * row identifying a harvested record, should, in the end, leave the test with
 * an empty table. A test based on this helper is successful if and only if at
 * the end of it, the table is empty. <br><br>
 *
 * The class constructor creates the table of rows that match the predefined
 * documents in the test resources responses directory. A document is stored
 * in the following form: <br><br>
 *
 * /resources/testName/endpoint0000/FormatLists/resp0000.xml <br><br>
 *
 * In this example <br><br>
 *
 * resp0000.xml <br><br>
 *
 * document contains the XML part of the OAI response. 'FormatLists' identifies
 * the of document. The helper supports these types of responses: <br><br>
 *
 * FormatLists, IdentifierLists, Records, RecordLists <br><br>
 *
 * A test can visit multiple endpoints. Like the documents, the helper
 * enumerates the endpoints. By extending the helper, the endpoint URIs should
 * be defined. The helper's constructor will invoke the <br><br>
 *
 * getEndpointURIs <br><br>
 *
 * method to obtain the URIs. Like loading the URI's the constructor will also
 * invoke the <br><br>
 *
 * method to obtain the metadata format used by the test. Next to this, it will
 * invoke the
 *
 * getTraces <br><br>
 *
 * method to load the table constituting the endpoint, prefix and record
 * identifier relation. Finally, by including a test name in the path to the
 * XML files, the helper supports multiple tests. By creating a helper for each
 * extension, a test can follow multiple scenarios. It could, for example, first
 * test record list harvesting by creating a <br><br>
 *
 * ListRecordsTestHelper <br><br>
 *
 * object, and after that, create a <br><br>
 *
 * ListIdentifiersTestHelper <br><br>
 *
 * object to follow the list identifiers scenario.
 *
 * Note: like implementing the metadata interface, the test also needs to
 * implement the OAI verb interface.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
abstract class TestHelper implements OAIInterface, MetadataInterface {

    /**
     * <br> Get the metadata format used in the test
     * @return the metadata format
     */
    abstract MetadataFormat getMetadataFormat();

    /**
     * <br> Get the URIs of the endpoints involved in the test
     *
     * @return the array of endpoint URIs
     */
    abstract String[] getEndpointURIs();

    /**
     * <br> Get the traces specific to a test
     */
    abstract void getTraces();

    /**
     * <br> Get the name of the test <br><br>
     *
     * @return top level directory associated with the test
     */
    abstract String getTestName();

    // factory for creating XML documents
    private DocumentBuilder db;

    // index pointing to the current endpoint
    private int eIndex;

    // the endpoints involved in the test
    String[] endpointURIs;

    // the relation between endpoints, prefixes and record identifiers
    ArrayList<Trace> traces = new ArrayList<>();

    // name of the designated resources folder
    String testName;

    // only keep one XPath object for querying
    public final XPath xpath;

    /**
     * <br> Create a helper <br><br>
     *
     * A helper provides the XML part of the responses carried through the OAI
     * protocol. It loads the responses from XML files in a resources folder
     * designated for a particular test.
     */
    TestHelper(){

        // set up a factory for the document builders
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // set up a document builder
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        // set up XPath querying
        XPathFactory xpf = XPathFactory.newInstance();
        xpath = xpf.newXPath();
        NSContext nsContext = new NSContext();
        nsContext.add("oai", "http://www.openarchives.org/OAI/2.0/");
        nsContext.add("os", "http://www.openarchives.org/OAI/2.0/static-repository");
        xpath.setNamespaceContext(nsContext);

        // load the endpoint URIs
        endpointURIs = getEndpointURIs(); eIndex = 0;

        // load the predefined traces
        getTraces();

        // get the name of the test
        testName = getTestName();
    }

    /**
     * <br> Return the first endpoint for testing <br><br>
     *
     * @return a real endpoint object, or null no endpoint is left
     */
    Provider getFirstEndpoint() {

        // reset the endpoint index
        eIndex = -1;
        // get the endpoint
        return getNextEndpoint();
    }

    // index pointing to the next document for the current endpoint
    private int dIndex;

    /**
     * <br> Return the next endpoint for testing <br><br>
     *
     * Note: getting a endpoint resets the document index.
     *
     * @return a real endpoint object, or null no endpoint is left
     */
    Provider getNextEndpoint(){

        // remember the endpoint
        Provider endpoint;

        if (eIndex + 1  == endpointURIs.length){
            // no endpoints left for testing
            return null;
        } else {
            // reset the document index
            dIndex = -1;

            // try to create a new endpoint object
            try {
                eIndex++;
                /* Since the test does not use the protocol, it will not
                   need to retry. Use zero for the maximum number of retries.
                 */
                endpoint = new Provider(endpointURIs[eIndex], 0);
            } catch (ParserConfigurationException e) {
                endpoint = null;
                e.printStackTrace();
            }

            return endpoint;
        }
    }

    /**
     * <br> Get the path to the file <br><br>
     *
     * Note: the specification of a resource only includes part of the path to
     * the file.
     *
     * @param resourceName specification of the resource
     */
    private String getFileName (String resourceName){

        // get the URL of the test file in the resources directory
        URL url = TestHelper.class.getResource(resourceName);

        if (url == null) {
            // the resource indicated does not exist
            return null;
        } else {
            /* Convert the URL to a URI to be able to convert the escaped path
               component
             */
            URI uri;

            try {
                uri = url.toURI();
            } catch (URISyntaxException e) {
                uri = null;
                e.printStackTrace();
            }

            if (uri == null) {
                // something went wrong
                return null;
            } else {
                // get the path without escape characters
                return uri.getPath();
            }
        }
    }

    /**
     * <br> Get an XML document from a file <br><br>
     *
     * Because the test is not intended to use the OAI protocol, only the XML
     * part of what is carried over the protocol needs to be represented. This
     * method obtains the predefined 'responses' from XML files in the resources
     * directory.
     *
     * @param resourceName the file to get the document from
     *
     * @return the document
     */
    Document getDocumentFromFile(String resourceName) {

        // get the name of the file
        String fileName = getFileName (resourceName);

        if (fileName == null) {
            // the file does not exist
            return null;
        } else {

            // the document
            Document document;

            // try to return the document contained in the file
            try {
                document = db.parse(getClass().getResourceAsStream(
                        resourceName));
            } catch (SAXException |
                    IOException e) {
                document = null;
                e.printStackTrace();
            }

            return document;
        }
    }

    // list of documents of the current type
    private ArrayList<Document> documentList;

    // the current type of document
    private String type;

    /**
     * <br> Get a list of response documents for the current endpoint, of the
     * type indicated
     *
     * @param type one of: 'FormatLists', 'IdentifierLists', 'Records',
     *             or 'RecordLists'
     */
    private void getDocumentList(String type) {

        // remember the type
        this.type = type;

        // create a string representing the current endpoint
        String endpointIndex = String.format("%04d", eIndex);

        // point to the first document of the type indicated
        int i = 0;
        for (;;) {

            // create a string representing the document
            String responseIndex = String.format("%04d", i);

            // create the name of the file
            String fileName = "/" + testName + "/endpoint" + endpointIndex +
                    "/" + type + "/resp" + responseIndex + ".xml";

            // try to get the document from the file
            Document document = getDocumentFromFile(fileName);

            if (document == null) {
                // no response documents left of this type for this endpoint
                return;
            } else {
                // add the document to the list
                documentList.add(document);
                // point to the next document
                i ++;
            }
        }
    }

    // remember the next document
    private Document nextDocument = null;

    /**
     * <br> Get a response document of the indicated type for the current
     * endpoint
     *
     * @param type one of: 'FormatLists', 'IdentifierLists', 'Records',
     *             or 'RecordLists'
     *
     * @return a response document or null if there are no more documents
     */
    Document getDocument(String type) {

        // check for a change in document type
        if (this.type == null || ! this.type.equals(type)){

            // remember the type
            this.type = type;

            // first document type or switch in document type, create a new list
            documentList = new ArrayList<>();

            // create a new list of documents
            getDocumentList(type);
            dIndex = -1;
        }

        // document to return
        Document document;

        // check if we can return a document already read
        if (nextDocument != null){
            // return the read ahead document

            document = nextDocument;
            nextDocument = null;

        } else {
            // no record available, try to get one from the list

            if (dIndex + 1 < documentList.size()) {
                // point to the next document, and get it
                dIndex ++; document = documentList.get(dIndex);
            } else {
                // no documents left in the list
                document = null;
            }
        }

        return document;
    }

    // the current document prefix
    private String prefix = null;

    @Override
    public Document newListMetadata(String endpointURI){

        return getDocument("FormatLists");
    }

    @Override
    public Document newListRecords(String p1, String p2){

        return getDocument("RecordLists");
    }

    @Override
    public Document newListRecords(String p1, String p2, String p3,
                                        String p4, String p5){

        return getDocument("RecordLists");
    }

    @Override
    public Document newGetRecord(String p1, String p2, String p3){

        return getDocument("Records");
    }

    @Override
    public Document newListIdentifiers (String p1, String p2){

        return getDocument("IdentifierLists");
    }

    @Override
    public Document newListIdentifiers (String p1, String p2, String p3,
                                             String p4, String p5){

        return getDocument("IdentifierLists");
    }

    /**
     * <br> Get the metadata prefixes referenced in a document <br><br>
     *
     * Note: because a document might might include multiple references,
     * assume it refers to one prefix only.
     *
     * kj: maybe only useful for envelopes
     *
     * @param document the document
     * @return the metadata prefix
     */
    private String getPrefix (Document document){

        // node in the document
        Node node = null;

        // metadata prefix
        String prefix;

        // look for the prefix in the request node
        try {
            node = (Node) xpath.evaluate(
                    "//*[parent::*[local-name()='request']]",
                    document, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        if (node == null){
            // no request node, no metadata prefix
            prefix = null;
        } else {
            // found the request node, get the prefix attribute value
            prefix = node.getAttributes().getNamedItem("metadataPrefix"
            ).getNodeValue();
        }

        return prefix;
    }

    @Override
    /**
     * <br> Get the resumption token for the current endpoint and
     * document type <br><br>
     *
     * @return the token, null if it would not make sense to file another
     *         request
     */
    public String getResumptionToken() {

        // check whether to supply a resumption token
        if (dIndex + 1 == documentList.size()) {
            // do not resume
            return null;
        } else {

            // look ahead at the next document in the list
            nextDocument = getDocument (type);

            if (nextDocument == null){
                // do not resume
                return null;
            } else {
                // check for a prefix change
                String prefix = getPrefix(nextDocument);

                // whether or not to resume depends on the prefix
                if (! prefix.equals(this.prefix)){
                    // prefix change, do not resume right now
                    return null;
                } else {
                    // mock a resumption token
                    return "resume with " + type + "document with index " + dIndex +
                            " for endpoint " + eIndex;
                }
            }
        }
    }

    /**
     * <br> A trace keeps track of the metadata the harvester package would
     * want to create. A trace represents one row in the relation between the
     * endpoints, prefixes and record identifiers.
     */
    class Trace {

        // create a trace
        public Trace (String endpointURI, String prefix, String identifier) {

            this.endpointURI = endpointURI;
            this.prefix = prefix;
            this.identifier = identifier;
        }

        // the record's endpoint URI
        String endpointURI;

        // the record's metadata prefix
        String prefix;

        // the record's identifier
        String identifier;

        @Override
        public boolean equals (Object object) {

            if (!(object instanceof Trace)) {
                return false;
            } else {
                Trace trace = (Trace) object;
                if (! trace.endpointURI.equals(this.endpointURI)){
                    return false;
                }
                if (! trace.prefix.equals(this.prefix)){
                    return false;
                }
                if (! trace.identifier.equals(this.identifier)){
                    return false;
                }
                return true;
            }
        }
    }

    @Override
    public Metadata newMetadata(Metadata metadata){

        /* Record the metadata by removing it from the table of metadata the
           test should yield.
         */
        removeFromTable(metadata);

        return metadata;
    }

    /**
     * <br> Add metadata information to the table <br><br>
     *
     * Repeatedly invoke this method from the getTraces method in order to
     * create the table with the predefined relation.
     *
     * @param prefix prefix in the trace to add to tbe table
     * @param identifier record identifier in the trace to add to the table
     */
    void addToTable(String endpointURI, String prefix,
                    String identifier) {

        Trace trace = new Trace(endpointURI, prefix, identifier);

        traces.add(trace);
    }

    // indicate success
    private boolean success = true;

    /**
     * <br> Remove metadata information from the table <br><br>
     *
     * Let the test set up Mockito to invoke this method whenever a scenario
     * in the harvesting package wants to create a metadata object. Acting as
     * a spy on a metadata constructor, this method can remove a trace from
     * the predefined table. If, in this way, the test ends up with an empty
     * table, it was successful. If and only if.
     *
     * @param metadata metadata to be removed from the table
     */
    void removeFromTable(Metadata metadata) {

        //
        if (success) {
            // kj: annotate
            if (traces.size() == 0){
                // not possible to remove
                success = false;
            } else {
                // determine the elements that make up a trace
                String endpointURI = metadata.getOrigin().getOaiUrl();
                String identifier = metadata.getId();
                String prefix = metadata.getPrefix();

                // create the trace
                Trace trace = new Trace(endpointURI, prefix, identifier);

                // remove the trace from the table
                if (! traces.remove(trace)){
                    // failed to remove the trace from the table, fail the test
                    success = false;
                }
            }
        }
    }

    /**
     * <br> Determine if the test is successful
     *
     * Assume that, before feeding the response documents to the test, the
     * table was initialised with the properties reflected by the response
     * documents.
     *
     * Suppose also, that the test has set up the removeFromTable method as a
     * spy on the metadata constructors in the harvesting package. Then, if the
     * package processes the responses correctly, at the end of the test there
     * should be no rows left in the table.
     *
     * @return true if and only if the test is successful
     */
    boolean success(){

        return success && traces.size() == 0;
    }
}
