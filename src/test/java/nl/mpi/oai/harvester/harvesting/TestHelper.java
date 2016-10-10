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
import nl.mpi.oai.harvester.utils.DocumentSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
 * format, and the action sequences, and set up mocking and spying, a helper
 * can provide a fixed set of XML documents that mock part of real OAI
 * responses. <br><br>
 *
 * A helper represents the relation between endpoints, prefixes, and record
 * identifiers as a list of traces. A trace identifies one metadata record. By
 * adding traces in the beginning of the test, and by removing a trace each
 * time a scenario would create a record, the test should end up with an empty
 * list. A test based on this helper is successful if and only if at the end of
 * it, the list is empty. <br><br>
 *
 * By invoking the <br><br>
 *
 * addTraces <br><br>
 *
 * method, the the class constructor will create a list of traces match the
 * predefined documents in the test resources responses directory. The test
 * package contains documents named like: <br><br>
 *
 * /resources/testName/endpoint0000/FormatLists/resp0000.xml <br><br>
 *
 * In this case, the <br><br>
 *
 * resp0000.xml <br><br>
 *
 * document contains the XML document representing the response to the format
 * list verb. The helper supports these types of responses: <br><br>
 *
 * FormatLists, IdentifierLists, Records, RecordLists <br><br>
 *
 * Typically, in the case of identifier list, records, and record list, the
 * helper will generate a resumption token if and only if it can find another
 * file that contains a response pertaining to the same metadata prefix. If in
 * enumeration of responses, the metadata prefix changes, it will not generate
 * a resumption token. This allows the harvesting scenario to switch prefixes
 * by issuing a new request. <br><br>
 *
 * A test helper will invoke the <br><br>
 *
 * getTestName <br><br>
 *
 * method to determine which directory to load the endpoint URIs and a list of
 * metadata formats from. To create a helper to a specific test, it suffices to
 * implement the abstract methods in this class, and to populate the resources
 * directory with suitable responses. Please note that the list records and list
 * identifier test helper classes serve as examples of extensions.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
abstract class TestHelper implements OAIInterface, MetadataInterface {

    // name of the designated resources folder
    String testName;

    /**
     * <br> Indicate the name of the test <br><br>
     *
     * Note: the name of the test determines from which resources directory
     * the helper will retrieve and build the mocked responses.
     *
     * @return top level directory associated with the test
     */
    abstract String getTestName();


    // the endpoints involved in the test
    String[] endpointURIs;

    /**
     * <br> Indicate the endpoint URIs involved in the
     *
     * @return the array of endpoint URIs
     */
    abstract String[] getEndpointURIs();

    /**
     * <br> Indicate the metadata format for the test
     *
     * @return the metadata format
     */
    abstract MetadataFormat getMetadataFormats();

    // the relation between endpoints, prefixes and record identifiers
    ArrayList<Trace> traces = new ArrayList<>();

    /**
     * <br> Add traces <br><br>
     *
     * A list of traces represents a test fixture: each trace represents a
     * metadata record that the test should yield. By letting the helper remove
     * a trace as soon as a scenario creates the metadata, at the end of the
     * test the list should be empty. <br><br>
     *
     * Note: the helper looks for the responses involved in the <br><br>
     *
     * /resources/testName <br><br>
     *
     * directories.
     */
    abstract void addTraces();

    // factory for creating XML documents
    private DocumentBuilder db;

    // index pointing to the current endpoint
    private int eIndex;

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

        // get the name of the test
        testName = getTestName();

        // load the endpoint URIs
        endpointURIs = getEndpointURIs(); eIndex = 0;

        // load the predefined traces
        addTraces();
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

    // index pointing to the current document for the current endpoint
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

        if (eIndex + 1 == endpointURIs.length){
            // no endpoints left for testing
            return null;
        } else {
            // reset the document index
            dIndex = 0;

            // try to create a new endpoint object
            try {
                eIndex++;
                /* Since the test does not use the protocol, it will not
                   need to retry. Use zero for the maximum number of retries.
                 */
                endpoint = new Provider(endpointURIs[eIndex], 0, new int[]{0});
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
     * <br> Get a list of response documents for the current endpoint and
     * current type
     */
    private void getDocumentList() {

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
                i++;
            }
        }
    }

    /**
     * <br> Get a response document of the indicated type for the current
     * endpoint
     *
     * @param type one of: 'FormatLists', 'IdentifierLists', 'Records',
     *             or 'RecordLists'
     *
     * @return a response document or null if there are no more documents
     */
    DocumentSource getDocumentSource(String type) {
        // check for a change in document type
        if (this.type == null || ! this.type.equals(type)){

            // switch type
            this.type = type;

            // first document type or switch in document type, create a new list
            documentList = new ArrayList<>();

            // create a new list of documents
            getDocumentList();

            dIndex = 0;
        }

        // document to return
        Document nextDocument;

        if (dIndex < documentList.size()) {
            // get the next document from the list
            nextDocument = documentList.get(dIndex);
            dIndex++;
        } else {
            // no documents left in the list
            nextDocument = null;
        }

        return new DocumentSource(nextDocument);
    }

    @Override
    public DocumentSource newListMetadata(String endpointURI){

        return getDocumentSource("FormatLists");
    }

    @Override
    public DocumentSource newListRecords(String p1, String p2){

        return getDocumentSource("RecordLists");
    }

    @Override
    public DocumentSource newListRecords(String p1, String p2, String p3,
                                        String p4, String p5){

        return getDocumentSource("RecordLists");
    }

    @Override
    public DocumentSource newGetRecord(String p1, String p2, String p3){

        return getDocumentSource("Records");
    }

    @Override
    public DocumentSource newListIdentifiers (String p1, String p2){

        return getDocumentSource("IdentifierLists");
    }

    @Override
    public DocumentSource newListIdentifiers (String p1, String p2, String p3,
                                             String p4, String p5){

        return getDocumentSource("IdentifierLists");
    }

    String prefix = null;

    @Override
    /**
     * <br> Get the resumption token for the current endpoint and
     * document type <br><br>
     *
     * @return the token, null if it would not make sense to file another
     *         request
     */
    public String getResumptionToken() {

        Document nextDocument;

        // check whether to supply a resumption token
        if (dIndex == documentList.size()) {
            // do not resume
            return null;
        } else {

            // check for a prefix change
            nextDocument = documentList.get(dIndex);
            String prefix = OAIHelper.getPrefix(new DocumentSource(nextDocument));

            if (this.prefix != null && ! prefix.equals(this.prefix)){
                /* Since the document's prefix differs from the current prefix,
                   trigger a new request.
                 */
                return null;
            }

            // remember the prefix
            this.prefix = prefix;

            // mock a resumption token
            return "resume with " + type + "document with index " + dIndex +
                        " for endpoint " + eIndex;
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
                return trace.identifier.equals(this.identifier);
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
     * Repeatedly invoke this method from the addTraces method in order to
     * create the table with the predefined relation.
     *
     * @param prefix prefix in the trace to add to tbe table
     * @param identifier record identifier in the trace to add to the table
     */
    void addToList(String endpointURI, String prefix,
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

        // only try to invalidate if successful up til now
        if (success) {
            // check the table
            if (traces.size() == 0){
                // not possible to remove
                success = false;
            } else {
                // determine the elements that make up a trace
                String endpointURI = metadata.getOrigin().getOaiUrl();
                String prefix = metadata.getPrefix();
                String identifier = metadata.getId();
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
