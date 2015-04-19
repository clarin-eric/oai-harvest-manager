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
 * <br> OAI protocol interface objects <br><br>
 *
 * By returning an OAI object, a method implementing one of the methods in
 * the interface, can provide an OAI response. If a test helper implements
 * the interface, it can provide mocked OAI response, facilitating in a test
 * that does not need a real OAI endpoint.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public interface OAIInterface {

    /**
     * <br> Create a list of metadata prefixes <br><br>
     *
     * @param endpointURI the endpoint URI
     * @return the OAI response
     */
    Document newListMetadata(String endpointURI);

    /**
     * <br> Create a list records object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the resumption token
     * @return the OAI response
     */
    Document newListRecords(String p1, String p2);

    /**
     * <br> Create a list records object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the resumption token
     * @return the OAI response
     */
    Document newListRecords(String p1, String p2, String p3, String p4,
                            String p5);

    /**
     * <br> Create a get record object <br><br>
     *
     * @param p1 the endpoint URI
     * @param p2 the record identifier
     * @param p3 the metadata prefix
     * @return the OAI response
     */
    Document newGetRecord(String p1, String p2, String p3);

    /**
     * <br> Create a list identifiers object <br><br>
     *
     * @param p1 endpoint URI
     * @param p2 resumption token
     * @return the OAI response
     */
    Document newListIdentifiers (String p1, String p2);

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
    Document newListIdentifiers (String p1, String p2, String p3, String p4,
                                 String p5);

    /**
     * <br> The list records and list identifier verbs return a resumption
     * token <br><br>
     *
     * @return the resumption token
     */
    String getResumptionToken ();
}
