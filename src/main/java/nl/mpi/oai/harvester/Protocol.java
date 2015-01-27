/*
 * Copyright (C) 2014, The Max Planck Institute for
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

/** 
 * A form of access to the OAI protocol. Implementations have access to a 
 * part of the OAI protocol. A request could take the form of the ListRecords
 * verb, in which case the processing will concentrate on parsing, separating,
 * records from the response.
 * 
 * By making requests to the endpoint, an object of an implementing class 
 * creates a list of response elements to be used within the client.
 * 
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Protocol {
    
    /**
     * Get a response from the endpoint
     * 
     * @return  false if there was an error, true otherwise
     */
    public boolean request ();
    
    /**
     * Find out if it would be sensible to make another request
     * 
     * @return  true if it might be, false otherwise
     */
    public boolean requestMore ();
    

    /**
     * Create a list of elements from the response
     * 
     * @return  false if there was an error, true otherwise
     */
    public boolean processResponse ();
    
    /**
     * Return the next element in the list 
     * 
     * Could be parsedProcessed due to adapt to ListIdentifiers
     * 
     * @return  null if an error occurred, otherwise the next element
     */
    public Object parseResponse ();
    
    /**
     * Check if the list is fully parsed
     * 
     * @return  true if it is, false otherwise
     */
    public boolean fullyParsed ();

}
