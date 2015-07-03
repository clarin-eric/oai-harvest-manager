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

import org.w3c.dom.Document;

/**
 * <br> A protocol for processing metadata elements <br><br>
 *
 * The methods specified in this interface constitute an protocol for
 * processing metadata elements obtained by an application of the OAI protocol.

 * The request method is intended to cover the various primitives in
 * the OAI protocol, like ListIdentifiers, ListRecords, and ListPrefixes. The
 * the other methods in the interface concern the handling of the response or
 * responses received.
 *
 * A particular application of the protocol defined in this class is called a
 * 'scenario'. An example of a scenario would be: to harvest metadata records
 * by listing records. In this case the the request method should create a
 * request based on this verb. The processResponse method should create a list
 * of metadata records, and the parseResponse should return the records one by
 * one. <br><br>
 *
 * Another example of a scenario would be to harvest metadata records by first
 * obtaining their identifiers. In this case, the request method should create
 * a request based on the ListIdentifiers verb. The, processResponse method
 * should create the list of identifiers, and the parseResponse method should
 * use the GetRecord verb to get the records one by one.
 *
 * Note: for more information on the OAI protocol, please refer to
 * <a href="http://www.openarchives.org/OAI/openarchivesprotocol.htm">
 *     its specification</a>.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public interface Harvesting {
    
    /**
     * <br> Request to the endpoint
     *
     * This method issues a OAI request. It also receives the response to the
     * request and stores for further processing
     *
     * @return  false if there was an error, true otherwise
     */
    public boolean request ();

    /**
     * <br> Get the current endpoint response <br><br>
     *
     * After a request has been issued, this method returns the response
     * received.
     *
     * @return null if an error occurred, otherwise the response to the request
     */
    public Document getResponse ();
    
    /**
     * <br> Find out if it would be sensible to make another request <br><br>
     *
     * It depends on the endpoint if, after a request has been issued, it can
     * respond to another request for metadata. If all data has been delivered,
     * the endpoint will not be able to fulfill a subsequent request.
     *
     * @return  true if the endpoint can respond to another request, false
     * otherwise
     *
     */
    public boolean requestMore ();

    /**
     * <br> Create metadata elements from the response <br><br>
     *
     * This method is concerned with setting up the data conveyed by the
     * response into a structure suitable for further processing. The choice
     * of this structure is up to the implementing class or classes.
     *
     * The internal structure serves as a temporary store for the metadata
     * received. To illustrate this: sometimes all responses need to be
     * inspected before the data can be handed over to a client class. Think,
     * for example, of duplicate records that need to be removed.
     *
     * Please note that after every request, this method needs to be invoked.
     *
     * @param document response
     * @return  false if there was an error, true otherwise
     */
    public boolean processResponse (Document document);
    
    /**
     * <br> Return the next metadata element <br><br>
     *
     * Release a metadata element to the client. Note that the data can belong
     * to a single response. It might also be collected from all the responses
     * given by the endpoint. In the first case, invoke parseResponse after
     * processResponse, in the second case, start invoking parseResponse after
     * requestMore returns false.
     *
     * @return  null if an error occurred, otherwise the next element
     */
    public Object parseResponse ();
    
    /**
     * <br> Check if all metadata elements stored have been parsed <br><br>
     * 
     * @return  true if it is, false otherwise
     */
    public boolean fullyParsed ();

}
