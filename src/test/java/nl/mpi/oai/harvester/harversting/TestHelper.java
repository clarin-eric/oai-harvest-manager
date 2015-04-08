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

package nl.mpi.oai.harvester.harversting;

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

/**
 * Help a test in mocking OAI protocol elements in the OCLC library.
 *
 * kj: provide support for resumption tokens, improve descriptions
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class TestHelper {

    /**
     * Keep track of the metadata the harvester package would want to create
     */
    class Trace {

        public  Trace (String prefix, String identifier) {
            this.identifier = identifier;
            this.prefix = prefix;
        }

        // the record's metadata prefix
        String prefix;

        // the record's identifier
        String identifier;
    }

    /* A table to store the provider id relation. The class constructor will
       create the table. It contains all combinations of prefixes and
       identifiers that the harvester package would have to be create from the
       documents assigned to the provider.

       When a spy on the metadata constructor removes the subsequent combination
       from the table, the test should end up with an empty table.
     */
    ArrayList<Trace> traces = new ArrayList<>();

    // factory for creating documents
    DocumentBuilder db;

    /**
     * <br> Get a mocked response from a file <br><br>
     *
     * Next, use the document to mock an OAI response. That is: not the
     * response itself, but the result of the getMetadataFormats method.
     *
     * @param file containing and endpoint response in XML form
     * @return the response in document form
     */
    public Document getDocumentFromFile(File file) {

        //
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

    /**
     * <br> Create a helper <br><br>
     *
     * Note: the test should create the metadata format, the action sequences,
     * and set up mocking or spying. The helper only provides the reponses.
     */
    public TestHelper (){

        // set up a factory for the document builders
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // set up a document builder
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


    }

    // index pointing to the current provider
    int pIndex = -1;

    // table of provider URIs
    final static String[] providerURIs;

    static {
        providerURIs = new String[]{
                "http://www.endpoint1.org",
                "http://www.endpoint2.org",
                "http://www.endpoint3.org"};
    }

    /**
     * <br> Return the next provider for testing <br><br>
     *
     * @return a real provider object, or null no provider is left
     */
    public Provider nextProvider(){

        Provider provider;

        if (pIndex == providerURIs.length){
            // done
            return null;
        } else {

            // reset the document index
            dIndex = 0;

            // try to create a new provider object
            try {
                pIndex++;
                provider = new Provider(providerURIs[pIndex], 0);
            } catch (ParserConfigurationException e) {
                provider = null;
                e.printStackTrace();
            }

            return provider;
        }
    }

    // index pointing to the next document for the provider
    int dIndex = 0;

    /**
     * <br> Get the next test document for the current provider <br><br>
     *
     * @return the next document for the provider
     */
    public Document nextDocument() {

        // get the index to the endpoint and provider as part of the filename
        String endpointId = String.format("%02d", pIndex );
        String responseId = String.format("%02d", dIndex); dIndex ++;

        // create a filename, it should look like /endpoint01/response01.xml
        String fileName =
                "/endpoint" + endpointId + "/response" + responseId + ".xml";

        // get the file containing response
        File file = nl.mpi.oai.harvester.cycle.TestHelper.getFile(fileName);

        // get response from the file
        Document document = getDocumentFromFile(file);

        // kj: register the document as a trace
        // this could be done manually, by looking at the resources folder,
        // or automatically, that is, by xpath parsing

        return document;
    }

    /**
     * Add metadata information to a table <br><br>
     *
     * @param prefix  prefix to remember
     * @param identifier identifier to remember
     */
    private void addToTable (String prefix, String identifier) {

        Trace trace = new Trace(prefix, identifier);

        traces.add(trace);
    }

    /**
     * Remove metadata information from the table <br><br>
     *
     * Let mockito invoke this method whenever the harvesting package wants to
     * create a metadata object. Only record the identifier, prefix, and
     * provider.
     *
     * @param metadata metadata to be removed from the table
     */
    private void removeFromTable (Metadata metadata) {

        String identifier = metadata.getId();

        Trace trace = new Trace("", identifier);

        // find the trace in the table
        long index = traces.lastIndexOf(trace);

        // remove the trace from the table
        traces.remove(trace);
    }

    // compare the provider id tables
    public boolean success() {
        return traces.size() == 0;
    }
}
