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

import ORG.oclc.oai.harvester2.verb.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * <br> Factory for OAI protocol objects <br><br>
 * <p/>
 * By injecting the factory into harvesting package constructors, when testing,
 * the origin of OAI response type objects can be influenced. Instead of getting
 * a real response from an OAI endpoint, a test helper can mock a response. In
 * this way a helper takes over the role of an OAI provider.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class OAIFactory {

    // an object implementing the OAI interface
    OAIInterface oaiInterface;

    // for some verbs, remember the resumption token
    private String resumptionToken = null;

    /**
     * <br> Connect an object that implements the OAI interface <br><br>
     * <p/>
     * Normally, when not testing, nothing will be connected. Therefore, this
     * method returns null. By spying on the factory, when using Mockito, can
     * the test can return an object that implements the interface. Typically,
     * a test helper object would be suited: it can receive the generated
     * metadata and compare it to the predefined data.
     *
     * @return an object implementing the OAI interface
     */
    public OAIInterface connectInterface() {
        return null;
    }

    /**
     * <br> Create a list of metadata prefixes <br><br>
     *
     * @param endpointURI the endpoint URI
     * @return the OAI response
     */
    Document createListMetadataFormats(String endpointURI) {

        // the verb response
        Document response = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null) {
            // no object connected
            try {
                HarvesterVerb verb = new ListMetadataFormats(endpointURI);
                response = verb.getDocument();
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            response = oaiInterface.newListMetadata(endpointURI);
        }

        return response;
    }

    /**
     * <br> Create a list records object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the resumption token
     * @return the OAI response
     */
    Document createListRecords(String p1, String p2) {

        // the verb response
        Document response = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null) {
            // no object connected
            try {
                HarvesterVerb verb = new ListRecords(p1, p2);
                response = verb.getDocument();
                resumptionToken = ((ListRecords) verb).getResumptionToken();
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException
                    | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            response = oaiInterface.newListRecords(p1, p2);
        }

        return response;
    }

    /**
     * <br> Create a list records object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the start of the date window on the records
     * @param p3 the end of the date window on the records
     * @param p4 the set the records should be in
     * @param p5 the metadata prefix the records should have
     * @return the OAI response
     */
    Document createListRecords(String p1, String p2, String p3, String p4,
                               String p5) {

        // the verb response
        Document response = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null) {
            // no object connected
            try {
                HarvesterVerb verb = new ListRecords(p1, p2, p3, p4, p5);
                response = verb.getDocument();
                resumptionToken = ((ListRecords) verb).getResumptionToken();
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException
                    | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            return oaiInterface.newListRecords(p1, p2, p3, p4, p5);
        }

        return response;
    }

    /**
     * <br> Create a get record object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the record identifier
     * @param p3 the metadata prefix
     * @return the OAI response
     */
    Document createGetRecord(String p1, String p2, String p3) {

        // the verb response
        Document response = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null) {
            // no object connected
            try {
                HarvesterVerb verb = new GetRecord(p1, p2, p3);
                response = verb.getDocument();
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            response = oaiInterface.newGetRecord(p1, p2, p3);
        }

        return response;
    }

    /**
     * <br> Create a list identifiers object <br><br>
     *
     * @param p1 endpoint URI
     * @param p2 resumption token
     * @return the OAI response
     */
    Document createListIdentifiers(String p1, String p2) {

        // the verb response
        Document response = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null) {
            // no object connected
            try {
                HarvesterVerb verb = new ListIdentifiers(p1, p2);
                response = verb.getDocument();
                resumptionToken = ((ListIdentifiers) verb).getResumptionToken();
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException
                    | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            response = oaiInterface.newListIdentifiers(p1, p2);
        }

        return response;
    }

    /**
     * <br> Create a list identifiers object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the start of the date window on the records
     * @param p3 the end of the date window on the records
     * @param p4 the set the records should be in
     * @param p5 the metadata prefix the records should have
     * @return the OAI response
     */
    Document createListIdentifiers(String p1, String p2, String p3,
                                   String p4, String p5) {

        // the verb response
        Document response = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null) {
            // no object connected
            try {
                HarvesterVerb verb = new ListIdentifiers(p1, p2, p3, p4, p5);
                response = verb.getDocument();
                resumptionToken = ((ListIdentifiers) verb).getResumptionToken();
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException
                    | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            response = oaiInterface.newListIdentifiers(p1, p2, p3, p4, p5);
        }

        return response;
    }

    /**
     * <br> Get the resumption token
     *
     * @return the resumption token
     */
    public String getResumptionToken() {
        return resumptionToken;

    }
}
