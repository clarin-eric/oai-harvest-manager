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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * <br> Factory for OAI protocol objects <br><br>
 *
 * By injecting the factory into harvesting package constructors, when testing,
 * the origin of OAI response type objects can be influenced. Instead of getting
 * a real response from an OAI endpoint, a test helper can mock a response. In
 * this way a helper takes over the role of an OAI provider.
 *
 * kj: improve the annotations
 *
 * kj: client code no longer needs exception handling
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class OAIFactory {

    // an object implementing the OAI interface
    OAIInterface oaiInterface;

    /**
     * <br> Connect an object that implements the OAI interface <br><br>
     *
     * Normally, when not testing, nothing will be connected. Therefore, this
     * method returns null. By spying on the factory, when using Mockito, can
     * the test can return an object that implements the interface. Typically,
     * a test helper object would be suited: it can receive the generated
     * metadata and compare it to the predefined data.
     *
     * @return an object implementing the OAI interface
     */
    public OAIInterface connectInterface(){
        return null;
    }

   /**
     * <br> Create a list of metadata prefixes <br><br>
     *
     * @param endpointURI the endpoint URI
     * @return the OAI response
     */
    HarvesterVerb createListMetadataFormats (String endpointURI){

        // the verb response
        HarvesterVerb verb = null;

        // check if the client connected an object the interface
        if (oaiInterface == null){
            // no object connected
            try {
                return new ListMetadataFormats(endpointURI);
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            verb = oaiInterface.newOAIListRecords();
        }

        return verb;
    }

    /**
     * <br> Create a list records object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the resumption token
     * @return the OAI response
     */
    HarvesterVerb createListRecords (String p1, String p2){

        // the verb response
        HarvesterVerb verb = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null){
            // no object connected
            try {
                return new ListRecords (p1, p2);
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            verb = oaiInterface.newOAIListRecords();
        }

        return verb;

    }

    /**
     * <br> Create a list records object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the resumption token
     * @return the OAI response
     */
    HarvesterVerb createListRecords (String p1, String p2, String p3, String p4,
                                     String p5){

        // the verb response
        HarvesterVerb verb = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null){
            // no object connected
            try {
                return new ListRecords (p1, p2, p3, p4, p5);
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            verb = oaiInterface.newOAIListRecords();
        }

        return verb;

    }

    // return new GetRecord(provider.oaiUrl, identifier, prefix)

    /**
     * <br> Create a list identifiers object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the record identifier
     * @param p3 the metadata prefix
     * @return the OAI response
     */
    HarvesterVerb createGetRecord (String p1, String p2, String p3){

        // the verb response
        HarvesterVerb verb = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null){
            // no object connected
            try {
                return new GetRecord (p1, p2, p3);
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            verb = oaiInterface.newOAIListRecords();
        }

        return verb;

    }

    /**
     * <br> Create a list identifiers object <br><br>
     *
     * @param p1 endpoint URI
     * @param p2 resumption token
     * @return the OAI response
     */
    HarvesterVerb createListIdentifiers (String p1, String p2){

        // the verb response
        HarvesterVerb verb = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null){
            // no object connected
            try {
                return new ListIdentifiers (p1, p2);
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            verb = oaiInterface.newOAIListRecords();
        }

        return verb;

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
    HarvesterVerb createListIdentifiers (String p1, String p2, String p3,
                                         String p4, String p5){

        // the verb response
        HarvesterVerb verb = null;

        oaiInterface = connectInterface();

        // check if the client connected an object the interface
        if (oaiInterface == null){
            // no object connected
            try {
                return new ListIdentifiers (p1, p2, p3, p4, p5);
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException e) {
                e.printStackTrace();
            }
        } else {
            // let the object connected return the OAI response

            verb = oaiInterface.newOAIListRecords();
        }

        return verb;

    }
}
