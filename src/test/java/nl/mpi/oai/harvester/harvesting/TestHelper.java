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
 * <br> Help mocking the OAI protocol by supplying the XML document part of
 * responses
 *
 * While the tests themselves should create the metadata format, and the action
 * sequences, and should set up mocking and spying, the helper provides a fixed
 * set of XML documents that can mock part of real OAI responses. <br><br>
 *
 * A TestHelper object contains a table for keeping track of the relation
 * prefix and record identifier. By initialising the table with rows that are
 * true to the predefined response documents, removing each row identifying a
 * harvested record, should, in the end, leave the test with an empty table. <br><br>
 *
 * The class constructor creates the table of rows that match the predefined
 * documents in the test resources responses directory. This directory contains
 * four subdirectories: one with documents mocking metadata formats, one
 * mocking lists with identifiers, one mocking lists of records and one mocking
 * single records. <br><br>
 *
 * Within all four directories, for each endpoint in the test, there is one
 * subdirectory that actually holds the responses. Both endpoints and responses
 * are enumerated.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public abstract class TestHelper {

    /**
     * <br> Get the URIs of the endpoints involved in the test
     *
     * @return the array of endpoint
     */
    abstract String[] getEndpointURIs();

    /**
     * <br> Get the traces specific to a test
     *
     * @return the traces needed to run the test
     */
    abstract ArrayList<Trace> getTraces();

    /**
     * <br> Get the name of the resources folder
     *
     * @return the name of the designated resources folder
     */
    abstract String getTestName();

    // factory for creating XML documents
    private DocumentBuilder db;

    // index pointing to the current endpoint
    private int eIndex;

    // the endpoints involved in the test
    String[] endpointURIs;

    // the relation between prefixes and record identifiers
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
    public TestHelper(){

        this.testName = testName;

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
    public Provider getFirstEndpoint() {

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
    public Provider getNextEndpoint(){

        Provider endpoint;

        if (eIndex == endpointURIs.length){
            // done
            return null;
        } else {

            // later on, create a new document list
            dIndex = -1;

            // try to create a new endpoint object
            try {
                eIndex++;
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

    // document type
    private String type;

    /**
     * <br> Get an XML document from a file <br><br>
     *
     * The document can serve as a part of a mocked response.
     *
     * @param fileName the file to get the document from
     * @return the document
     */
    public Document getDocumentFromFile(String fileName) {

        // create a file reference
        File file = getFile(fileName);

        if (!file.exists()) {
            return null;
        } else {
            // the XML document to obtain from the file
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
     * <br> Get the documents for the current endpoint, for the type indicated
     *
     * @param type string FormatLists, IdentifierLists, Records, RecordLists
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

            // try to get the document
            Document document = getDocumentFromFile(fileName);

            if (document == null) {
                // no documents left
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
     * <br> Get the next test document of the current type for the current
     * endpoint <br><br>
     *
     * @return a document or null if there are no more documents
     */
    public Document getDocument() {

        // check if we need a new list
        if (dIndex == -1) {
            getDocumentList(type);
        }

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
     * @return the token, null if there are no document left to request
     */
    public String getResumptionToken() {

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
     * want to create <br><br>
     *
     * A trace represents one row in the relation between prefix and record
     * identifier.
     */
    class Trace {

        // create a trace
        public  Trace (String endpointURI, String prefix, String identifier) {
            this.identifier = identifier;
            this.prefix = prefix;
        }

        // the record's metadata prefix
        String endpointURI;

        // the record's metadata prefix
        String prefix;

        // the record's identifier
        String identifier;
    }

    /**
     * <br> Add metadata information to a table <br><br>
     *
     * As an alternative to static initialisation of the table, repeatedly
     * invoke this method.
     *
     * @param prefix prefix in the trace to add to tbe table
     * @param identifier record identifier in the trace to add to the table
     */
    private void addToTable(String endpointURI, String prefix,
                            String identifier) {

        Trace trace = new Trace(endpointURI, prefix, identifier);

        traces.add(trace);
    }

    /**
     * <br> Remove metadata information from the table <br><br>
     *
     * Let the test set up Mockito to invoke this method whenever a scenario
     * in the harvesting package wants to create a metadata object. When this
     * method acts like a spy on a metadata constructor, it can create a trace
     * and remove it from the table. In this way, the test should end up with
     * an empty table.
     *
     * @param metadata metadata to be removed from the table
     */
    private void removeFromTable(Metadata metadata) {

        // determine the elements that make up a trace
        String endpointURI = metadata.getOrigin().getOaiUrl();
        String identifier = metadata.getId();
        String prefix = metadata.getDoc().getPrefix();

        // create the trace
        Trace trace = new Trace(endpointURI, prefix, identifier);

        // remove the trace from the table
        traces.remove(trace);
    }

    // compare the tables
    public boolean success() {
        return traces.size() == 0;
    }
}
