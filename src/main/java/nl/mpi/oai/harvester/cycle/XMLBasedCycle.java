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

package nl.mpi.oai.harvester.cycle;

import nl.mpi.oai.harvester.generated.EndpointType;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;

/**
 * <br> Implement Cycle based on XML properties <br><br>
 *
 * note: use the methods in the XMLOverview class, receive objects through the
 * Properties and Endpoint interface
 *
 * note: this class relies on JAXB generated types.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class XMLBasedCycle implements Cycle {

    // overview marshalling object
    private final XMLOverview xmlOverview;

    // the general properties defined by the XML file
    private final Properties properties;

    // the endpoint URIs returned to the client in the current cycle
    private static ArrayList<String> endpointsCycled = new ArrayList<>();

    /**
     * Associate the cycle with the XML file defining the properties
     *
     * @param filename name of the XML file defining the properties
     */
    public XMLBasedCycle (String filename){

        // create an properties marshalling object
        xmlOverview = new XMLOverview(filename);

        properties = xmlOverview.getOverview();
    }

    @Override
    /**
     * Note: the method needs synchronisation because endpoints might be
     * harvested in parallel.
     */
    public synchronized Endpoint next(String URI, String group) {

        // get the endpoint from the overview
        return xmlOverview.getEndpoint(URI, group);
    }

    @Override
    /**
     * <br> Get the next residual endpoint in the cycle <br><br>
     *
     * Since the cycle supports parallel endpoint harvesting, by adding
     * endpoint URIs to a list, this method checks if a particular endpoint not
     * yet marked as having been attempted, is currently being attempted. In
     * the method ensures that every endpoint is returned to the client at most
     * once.
     *
     * @return the next endpoint elligable for harvesting, null if all
     *         endpoints have been cycled over.
     */
    public synchronized Endpoint next() {

        int endpointCount = xmlOverview.overviewType.getEndpoint().size();

        // find an endpoint not yet returned in this cycle
        for (int i = 0; i < endpointCount; i++) {

            EndpointType endpointType =
                    xmlOverview.overviewType.getEndpoint().get(i);

            // get today's date
            Date date = new Date ();
            // prepare it for ISO8601 representation
            DateTime dateTime = new DateTime(date);

            // get the date of the most recent attempt
            String attemptedDate = endpointType.getAttempted().toString();

            if (attemptedDate.equals(dateTime.toString())){
                // endpoint was attempted today, skip it
            } else {
                if (endpointsCycled.contains(endpointType.getURI())){
                    // endpoint is being attempted, skip it
                } else {
                    // add the endpoint to the list of endpoints attempted
                    endpointsCycled.add(endpointType.getURI());
                    /* Return an adapter for the endpoint by supplying the
                       generated type.
                     */
                    return xmlOverview.getEndpoint(endpointType);
                }
            }
        }

        // no residual endpoint found
        return null;
    }

    @Override
    public boolean doHarvest(Endpoint endpoint) {

        // decide whether or not the endpoint should be harvested

        switch (properties.getHarvestMode()){

            case normal:
                if (endpoint.blocked()){
                    return false;
                } else {
                    return true;
                }

            case retry:
                DateTime attempted, harvested;

                attempted = endpoint.getAttemptedDate();
                harvested = endpoint.getHarvestedDate();

                if (attempted.equals(harvested)) {
                    return false;
                } else {
                    return true;
                }

            case refresh:
                if (endpoint.blocked()){
                    return false;
                } else {
                    return true;
                }
        }

        return false;
    }

    @Override
    public boolean doHarvest(String URI) {

        Endpoint endpoint = null;

        // kj: implement the interface from here

        // find the endpoint

        // invoke the doHarvest method, now send it the endpoint
        return doHarvest(endpoint);
    }

    @Override
    public DateTime getRequestDate(Endpoint endpoint) {

        // if incremental harvesting, compare cycle and endpoint property

        if (endpoint.allowIncrementalHarvest()){

            DateTime cycleDate = properties.getHarvestFromDate();
            DateTime endpointDate = endpoint.getHarvestedDate();

            if (endpointDate.isBefore(cycleDate)){
                return (cycleDate);
            } else {
                return (new DateTime());
            }
        } else {
            // use today
            DateTime today = new DateTime (new Date());
            return today;
        }
    }

    @Override
    public synchronized boolean doneHarvesting() {
        return false;
    }

    @Override
    public void retry() {

    }
}
