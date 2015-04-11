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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static nl.mpi.oai.harvester.cycle.TestHelper.getFile;

/**
 * <br> Help mocking the OAI protocol by supplying the XML document part of OAI
 * responses
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
 * in the following form:
 *
 * /resources/testName/endpoint0001/FormatLists/resp0001.xml
 *
 * In this example
 *
 * resp0001.xml
 *
 * document contains the XML part of the OAI response. 'FormatLists' identifies
 * the type of document. The helper supports these types of responses:
 *
 * FormatLists, IdentifierLists, Records, RecordLists
 *
 * A test can visit multiple endpoints. Like the documents, the helper
 * enumerates the endpoints. By extending the helper, the endpoint URIs should
 * be defined. The helper's constructor will invoke the
 *
 * getEndpointURIs
 *
 * method to obtain the URIs. Like loading the URI's the constructor will also
 * invoke the
 *
 * getTraces
 *
 * method to load the table constituting the endpoint, prefix and record
 * identifier relation. Finally, by including a test name in the path to the
 * XML files, the helper supports multiple tests. By creating a helper for each
 * extension, a test can follow multiple scenarios. It could, for example, first
 * test record list harvesting by creating a
 *
 * ListRecordsTestHelper
 *
 * object, and after that, create a
 *
 * ListIdentifiersTestHelper
 *
 * object to follow the list identifiers scenario.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
abstract class TestHelper {

    /**
     * <br> Get the URIs of the endpoints involved in the test
     *
     * @return the array of endpoint URIs
     */
    abstract String[] getEndpointURIs();

    /**
     * <br> Get the traces specific to a test
     *
     * @return the traces needed to run the test
     */
    abstract ArrayList<Trace> getTraces();

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

        // load the endpoint URIs
        endpointURIs = getEndpointURIs(); eIndex = 0;

        // load the predefined traces
        traces = getTraces();
    }

    /**
     * <br> Return the first endpoint for testing <br><br>
     *
     * @return a real endpoint object, or null no endpoint is left
     */
    Provider getFirstEndpoint() {

        // reset the endpoint index
        eIndex = 0;
        // get the endpoint
        return getNextEndpoint();
    }

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

        if (eIndex == endpointURIs.length){
            // no endpoints left for testing
            return null;
        } else {
            // later on, create a new document list
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

    // index pointing to the next document for the current endpoint
    private int dIndex;

    // list of documents of the current type
    private ArrayList<Document> documentList = new ArrayList<>();

    /* The type of document. On of: 'FormatLists', 'IdentifierLists', 'Records',
       or 'RecordLists'.
     */
    private String type;

    /**
     * <br> Get an XML document from a file <br><br>
     *
     * Because the test is not intended to use the OAI protocol, only the XML
     * part of what is carried over the protocol needs to be represented. This
     * method obtains the predefined 'responses' from XML files in the resources
     * directory.
     *
     * @param fileName the file to get the document from
     * @return the document
     */
    Document getDocumentFromFile(String fileName) {

        // create a file reference
        File file = getFile(fileName);

        if (!file.exists()) {
            // the response has not been defined
            return null;
        } else {
            // the document containing the 'response'
            Document document;

            // try to return the document contained in the file
            try {
                document = db.parse(getClass().getResourceAsStream(
                        file.getAbsolutePath()));
            } catch (SAXException |
                    IOException e) {
                document = null;
                e.printStackTrace();
            }

            return document;
        }
    }

    /**
     * <br> Get a list of response documents for the current endpoint, of the
     * type indicated
     *
     * @param type string, one of: 'FormatLists', 'IdentifierLists', 'Records',
     *             or 'RecordLists'
     */
    private void getDocumentList(String type) {

        // remember the type
        this.type = type;

        // create a string representing the current endpoint
        String endpointIndex = String.format("%04d", eIndex);

        // point to the first document of the type indicated
        dIndex = 0;
        for (;;) {

            // create a string representing the document
            String responseIndex = String.format("%04d", dIndex); dIndex ++;

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
                dIndex ++;
            }
        }
    }

    /**
     * <br> Get a response document for the current endpoint, of the type
     * indicated
     *
     * @return a response document or null if there are no more documents
     */
    Document getDocument() {

        // check if we need a new list
        if (dIndex == -1) {
            getDocumentList(type);
        }

        // the document that will contain the 'response'
        Document document;

        // check if there is a document available
        if (dIndex < documentList.size()) {
            document = documentList.get(dIndex);
        } else {
            // reset the document index
            dIndex = -1;
            document = null;
        }

        return document;
    }

    /**
     * <br> Get the resumption token for the current endpoint and
     * document type <br><br>
     *
     * @return the token, null if it would not make sense to file another
     * request
     */
    String getResumptionToken() {

        if (dIndex == documentList.size()) {
            // do not resume
            return null;
        } else {
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
        public  Trace (String endpointURI, String prefix, String identifier) {
            this.identifier = identifier;
            this.prefix = prefix;
        }

        // the record's endpoint URI
        String endpointURI;

        // the record's metadata prefix
        String prefix;

        // the record's identifier
        String identifier;
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

        // determine the elements that make up a trace
        String endpointURI = metadata.getOrigin().getOaiUrl();
        String identifier = metadata.getId();
        String prefix = metadata.getDoc().getPrefix();

        // create the trace
        Trace trace = new Trace(endpointURI, prefix, identifier);

        // remove the trace from the table
        traces.remove(trace);
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
    boolean success() {
        return traces.size() == 0;
    }
}
