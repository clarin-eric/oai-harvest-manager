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
import nl.mpi.oai.harvester.metadata.MetadataFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

/**
 * <br> A request method in a list based harvesting protocol <br><br>
 *
 * This class adds a request method of the protocol defined by the harvesting
 * interface. While the method does not invoke a particular OAI primitive, it
 * expects these primitives to be list related, like for example ListRecords or
 * ListIdentifiers. This class defines two verb methods, one for a verb with
 * two, another for a verb with five parameters. <br><br>
 *
 * The request iterates over sets and prefixes. Since the response and therefore
 * the method of processing it will be different for different types of verbs,
 * this class leaves the other methods in the protocol abstract. <br><br>
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public abstract class ListHarvesting extends AbstractListHarvesting implements
        Harvesting {

    private static final Logger logger = Logger.getLogger(ListHarvesting.class);

    /**
     * Messages specific to extending classes
     */
    final static String[] message = new String [3];

    /**
     * Associate endpoint data and desired prefix
     * 
     * @param oaiFactory the OAI factory
     * @param provider the endpoint to address in the request
     * @param prefixes the prefixes returned by the endpoint 
     * @param metadataFactory the metadata factory
     */
    ListHarvesting(OAIFactory oaiFactory, Provider provider,
                   List<String> prefixes, MetadataFactory metadataFactory){

        super(oaiFactory, provider, metadataFactory);
        this.prefixes   = prefixes;
        document        = null;
        resumptionToken = null;
        tIndex          = 0;

        // check for protocol errors
        if (prefixes == null){
            throw new HarvestingException();
        }
    }
    
    /**
     * Verb with two string parameters. A subclass needs to make this verb 
     * effective for example by creating a ListRecords or ListIdentifiers
     * class object.
     *
     * @param s1 string one
     * @param s2 string two
     * @return the response
     * @throws IOException IO problem
     * @throws ParserConfigurationException configuration problem
     * @throws SAXException XML problem
     * @throws TransformerException XSL problem
     * @throws NoSuchFieldException introspection problem
     */
    abstract Document verb2(String s1, String s2)
            throws 
            IOException,
            ParserConfigurationException,
            SAXException,
            TransformerException,
            NoSuchFieldException;

    /**
     * Verb with five string parameters. A subclass needs to make this verb 
     * effective for example by creating a ListRecords or ListIdentifiers
     * class method.
     *
     * @param s1 string one
     * @param s2 string two
     * @param s3 string three
     * @param s4 string four
     * @param s5 string five
     * @return the response
     * @throws IOException IO problem
     * @throws ParserConfigurationException configuration problem
     * @throws SAXException XML problem
     * @throws TransformerException XSL problem
     * @throws NoSuchFieldException introspection problem
     */
    abstract Document verb5(String s1, String s2, String s3, String s4,
            String s5)
            throws 
            IOException,
            ParserConfigurationException,
            SAXException,
            TransformerException,
            NoSuchFieldException;

    /**
     * Get the token indicating more data is available. Since a HarvesterVerb 
     * object does not have a method for getting the token itself, the extending
     * classes need to make this method effective.
     * 
     * @return a string containing the token
     * @throws TransformerException XSL problem
     * @throws NoSuchFieldException introspection problem
     */
    abstract String getToken () throws TransformerException,
            NoSuchFieldException;

    /**
     * Request metadata from the endpoint. Retry as often as the configuration
     * allows. A scenario should invoke the requestMore method to determine if
     * another request should be made.
     *
     * @return false if an error occurred, true otherwise.
     */
    @Override
    public boolean request() {

        /* Check for protocol errors. Note that because of the constructor,
           the prefixes and the provider are in place.
         */
        if (pIndex >= prefixes.size()) {
            throw new HarvestingException();
        }
        if (provider.sets != null) {
            // if sets have been defined, the sIndex should be pointing to one
            if (sIndex >= provider.sets.length) {
                throw new HarvestingException();
            }
        }

        // create a new node list for processing the list records request
        nIndex = 0;

        // number of requests attempted
        int i = 0;
        for (; ; ) {
            // assume request will complete successfully
            boolean done = true;

            // try the request
            try {
                /* Try to get a response from the endpoint. Because of the
                   check for protocol errors, pIndex points to an element in
                   the list.
                 */
                if (!(resumptionToken == null || resumptionToken.isEmpty())) {
                    // use resumption token
                    logger.debug(message[0] + prefixes.get(pIndex));

                    document = verb2(provider.oaiUrl, resumptionToken);
                } else {
                    logger.debug(message[1] + prefixes.get(pIndex));

                    if (provider.sets == null) {
                        // no sets specified, ask for records by prefix
                        document = verb5(provider.oaiUrl, null, null,
                                null,
                                prefixes.get(pIndex));
                    } else {
                        // request targets for a new set and prefix combination
                        document = verb5(provider.oaiUrl, null, null,
                                provider.sets[sIndex],
                                prefixes.get(pIndex));
                    }
                }

                // check if more records would be available
                resumptionToken = getToken();

            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException
                    | NoSuchFieldException e) {

                // invalidate the assumption that everything went fine
                done = false;

                // report
                logger.error("ListHarvesting["+this+"]["+provider+"] request try["+(i+1)+"/"+provider.maxRetryCount+"] failed!");
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
                if (i == provider.maxRetryCount) {
                    // do not retry any more, try another prefix instead
                    return false;
                }
                // retry the request once more
                if (provider.retryDelay > 0) {
                    try {
                    Thread.sleep(provider.retryDelay);
                    } catch(InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }                
            }
        }
    }

    /**
     * <br> Get the response <br><br>
     *
     * @return the response
     */
    @Override
    public Document getResponse() {

        // check for protocol error
        if (document == null){
            throw new HarvestingException();
        }

        return document;
    }
}
