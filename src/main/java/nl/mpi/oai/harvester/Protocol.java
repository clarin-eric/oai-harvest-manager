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

package nl.mpi.oai.harvester;

import org.w3c.dom.Document;

/**
 * Abstract view on the OAI protocol <br><br>
 *
 * A class implementing this interface is intended to provide those primitives
 * of the OAI protocol that play a rol in a particular scenario of harvesting. <br><br>
 *
 * If the scenario, for example, would be to harvest metadata records using
 * the ListRecords verb, the request method implements a request based on this
 * verb. In this scenario, the processResponse method would create a list of
 * metadata records included in the response to the verb. Finally, the
 * parseResponse method would return the records one by one. <br><br>
 *
 * Another example of a class used by a scenario would be harvesting using
 * the ListIdentifiers verb. The request method would obtain responses to this
 * verb, the processResponse method would create the list of identifiers, and
 * the parseResponse method would use the GetRecord verb to get the records one
 * by one. <br><br>
 *
 * Different implementations of this interface allow for different scenarios
 * to be implemented uniformly. Also, a particular scenario could be implemented
 * by similar but different classes implementing this interface. Finally, an
 * implementation can target different parts of the protocol. Next to classes
 * that implement record harvesting, there could be a class supplying the
 * primitives for obtaining prefixes. <br><br>
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Protocol {
    
    /**
     * Request to the endpoint<br><br>
     *
     * @return  false if there was an error, true otherwise
     */
    public boolean request ();

    /**
     * Get the response from the endpoint<br><br>
     *
     * @return null if an error occurred, otherwise the response to the request
     */
    public Document getResponse ();
    
    /**
     * Find out if it would be sensible to make another request<br><br>
     * 
     * @return  true if it might be, false otherwise
     */
    public boolean requestMore ();
    

    /**
     * Create a list of metadata elements from the response<br><br>
     * 
     * @return  false if there was an error, true otherwise
     */
    public boolean processResponse ();
    
    /**
     * Return the next metadata element in the list<br><br>
     * 
     * Could be parsedProcessed due to adapt to ListIdentifiers
     * 
     * @return  null if an error occurred, otherwise the next element
     */
    public Object parseResponse ();
    
    /**
     * Check if the list is fully parsed<br><br>
     * 
     * @return  true if it is, false otherwise
     */
    public boolean fullyParsed ();

}
