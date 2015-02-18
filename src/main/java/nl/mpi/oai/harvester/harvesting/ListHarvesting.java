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

import ORG.oclc.oai.harvester2.verb.HarvesterVerb;
import nl.mpi.oai.harvester.Provider;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * <br>List oriented application of the protocol <br><br>
 *
 * This class provides a sorted list of elements. This list can be used by
 * extending classes to store characteristics of metadata returned by an
 * endpoint. Because elements can only occur once in the list, classes can for
 * example use the list to remove duplicate record identifiers. <br><br>
 *
 * Because the class does not specify any verbs itself, extending classes need
 * to provide these. This class defines two verb methods, one for a verb with two,
 * another for a verb with five parameters. An extending class will need to
 * override these methods. <br><br>
 *
 * The request method is intended to iterate over sets and prefixes. Since the
 * response and therefore the method of processing it will be different for
 * different types of verbs, this class leaves the other methods in the protocol
 * abstract. The extending class will have to implement these. <br><br>
 *
 * @author keeloo
 */
public abstract class ListHarvesting extends AbstractListHarvesting {

    private static final Logger logger = Logger.getLogger(ListHarvesting.class);
    
    /** Messages specific to extending classes */
    final static String[] message = new String [3];

    /**
     * Associate endpoint data and desired prefix
     * 
     * @param provider the endpoint to address in the request
     * @param prefixes the prefixes returned by the endpoint 
     */
    ListHarvesting(Provider provider, List<String> prefixes){
        super(provider);
        this.prefixes   = prefixes;
        pIndex          = 0;
        response        = null;
        resumptionToken = null;
        tIndex          = 0;

        // kj: response needs to be a ListRecords object
    }
    
    /**
     * Verb with two string parameters. A subclass needs to make this verb 
     * effective for example by invoking the ListRecords or ListIdentifiers 
     * request method. 
     *
     * @return the response
     */
    abstract HarvesterVerb verb2(String s1, String s2)
            throws 
            IOException,
            ParserConfigurationException,
            SAXException,
            TransformerException,
            NoSuchFieldException;

    /**
     * Verb with five string parameters. A subclass needs to make this verb 
     * effective for example by invoking the ListRecords or ListIdentifiers 
     * request method. 
     *
     * @return the response
     */
    abstract HarvesterVerb verb5(String s1, String s2, String s3, String s4,
            String s5)
            throws 
            IOException,
            ParserConfigurationException,
            SAXException,
            TransformerException,
            NoSuchFieldException;

    /**
     * Get the token indicating more data is available. Since a HarvesterVerb 
     * object does not have a method for getting the token the extending classes 
     * need to make this method effective.
     * 
     * @param  response the response
     * @return  a string containing the token
     * @throws  TransformerException
     * @throws  NoSuchFieldException 
     */
    abstract String getToken (HarvesterVerb response) throws TransformerException, 
            NoSuchFieldException;

    /**
     * Request metadata from the endpoint. Retry as often as the configuration
     * allows. A scenario should invoke the requestMore method to determine if
     * another request should be made.
     *
     *
     *
     * @return false if an error occurred, true otherwise.
     */
    @Override
    public boolean request() {

        // check for protocol errors

        if (pIndex >= prefixes.size()) {
            throw new UnsupportedOperationException("Protocol error");
        }

        if (provider.sets != null) {
            if (sIndex >= provider.sets.length) {
                throw new UnsupportedOperationException("Protocol error");
            }
        }

        // start with a fresh node list for processing the list records request
        response = null;

        // create a new node list for processing the list records request
        nIndex = 0;

        // number of requests attempted
        int i = 0;
        for (; ; ) {
            // assume request will complete successfully
            boolean done = true;

            // try the request
            try {
                // try to get a response from the endpoint
                if (!(resumptionToken == null || resumptionToken.isEmpty())) {
                    // use resumption token
                    logger.debug(message[0] + prefixes.get(pIndex));

                    response = verb2(provider.oaiUrl, resumptionToken);
                } else {
                    logger.debug(message[1] + prefixes.get(pIndex));

                    if (provider.sets == null) {
                        // no sets specified, ask for records by prefix
                        response = verb5(provider.oaiUrl, null, null,
                                null,
                                prefixes.get(pIndex));
                    } else {
                        // request targets for a new set and prefix combination
                        response = verb5(provider.oaiUrl, null, null,
                                provider.sets[sIndex],
                                prefixes.get(pIndex));
                    }
                }

                // check if more records would be available
                resumptionToken = getToken(response);

            } catch (IOException
                    | ParserConfigurationException
                    | SAXException | TransformerException | NoSuchFieldException e) {

                // invalidate the assumption that everything went fine
                done = false;

                // report
                logger.error(e.getMessage(), e);
                if (provider.sets == null) {
                    logger.info(message[2] + prefixes.get(pIndex)
                            + " records from endpoint " + provider.oaiUrl);

                } else {
                    logger.info(message[2] + prefixes.get(pIndex)
                            + " records in set " + provider.sets[sIndex]
                            + " from endpoint " + provider.oaiUrl);
                }
            }
            // tried the request

            if (done) {
                // the request completed successfully
                return true;
            } else {
                i++;
                if (i > provider.maxRetryCount) {
                    // do not retry any more, try another prefix instead
                    return false;
                }
                // retry the request once more
            }
        }
    }
}
