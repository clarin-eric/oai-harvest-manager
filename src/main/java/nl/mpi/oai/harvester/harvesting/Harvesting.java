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
 * <br> Abstract view on the OAI protocol<br><br>
 *
 * A class (indirectly) implementing this interface intends to provide those
 * primitives in the OAI protocol that play a rol in a particular scenario of
 * harvesting. <br><br>
 *
 * If a scenario would be to harvest metadata records by listing records,
 * the request method should create a request based on this verb. The
 * processResponse method should create a list of metadata records, and the
 * parseResponse should return the records one by one. <br><br>
 *
 * Another example of a scenario would be to harvest metadata records by first
 * obtaining their identifiers. In this case, the request method should create
 * a request based on the ListIdentifiers verb. The, processResponse method
 * should create the list of identifiers, and the parseResponse method should
 * use the GetRecord verb to get the records one by one. <br><br>
 *
 * Please note that an implementation can target different parts of the
 * protocol. Next to classes that implement record harvesting, there could be a
 * class supplying the primitives for obtaining prefixes or a class that
 * implements static harvesting.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Harvesting {
    
    /**
     * <br> Request to the endpoint
     *
     * @return  false if there was an error, true otherwise
     */
    public boolean request ();

    /**
     * <br> Get the response from the endpoint<br><br>
     *
     * @return null if an error occurred, otherwise the response to the request
     */
    public Document getResponse ();
    
    /**
     * <br> Find out if it would be sensible to make another request<br><br>
     * 
     * @return  true if it might be, false otherwise
     */
    public boolean requestMore ();
    

    /**
     * <br> Create metadata elements from the response<br><br>
     * 
     * @return  false if there was an error, true otherwise
     */
    public boolean processResponse ();
    
    /**
     * <br> Return the next metadata element <br><br>
     *
     * @return  null if an error occurred, otherwise the next element
     */
    public Object parseResponse ();
    
    /**
     * <br> Check if all metadata elements have been parsed<br><br>
     * 
     * @return  true if it is, false otherwise
     */
    public boolean fullyParsed ();

}
