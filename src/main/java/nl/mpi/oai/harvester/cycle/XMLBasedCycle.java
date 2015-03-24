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
 * <br> A harvest cycle implementation based on XML properties <br><br>
 *
 * A client to the cycle package can invoke the methods in this class to cycle
 * over the endpoints given by an overview. This class implements a cycle based
 * on endpoints represented in an XML file.
 *
 * The client will need a CycleFactory object to create an XMLBasedCycle. After
 * receiving a cycle from it, the client can invoke methods on this object that
 * return the next endpoint in the cycle.
 *
 * The doHarvest methods will inform the client whether it should harvest the
 * endpoint or not, and also, in case of incremental harvesting, what the date
 * needed to create an OAI request would be.
 *
 * Note: this class relies on JAXB generated types directly. Its methods return
 * adapters to the endpoint stored in the overview.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class XMLBasedCycle implements Cycle {

    // overview marshalling object
    private final XMLOverview xmlOverview;

    // the general properties defined by the XML file
    private final CycleProperties cycleProperties;

    // the endpoint URIs returned to the client in the current cycle
    private static ArrayList<String> endpointsCycled = new ArrayList<>();

    /**
     * Associate the cycle with the XML file defining the cycle and endpoint
     * properties
     *
     * @param filename name of the XML file defining the properties
     */
    public XMLBasedCycle (String filename){

        // create an cycleProperties marshalling object
        xmlOverview = new XMLOverview(filename);

        cycleProperties = xmlOverview.getCycleProperties();
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
     * As long as an endpoint recorded in the overview has not been returned
     * to the client by the next method that accepts an externally supplied URI
     * and group, consider it 'residual'. This method will return the residual
     * endpoints. <br><br>
     *
     * Note: only consider a cycle 'finished' if there are no residual
     * endpoints. <br><br>
     *
     * Since the cycle supports parallel endpoint harvesting, by adding
     * endpoint URIs to a list, this method checks if a particular endpoint not
     * yet marked as having been attempted, is currently being attempted. In
     * the method ensures that every endpoint is returned to the client at most
     * once. <br><br>
     *
     * In deciding whether or not the end of the cycle has been reached, the
     * method considers the endpoints stored in the overview. It cannot know
     * about endpoints the client would present the cycle with by passing an
     * identification to the next method. <br><br>
     *
     * @return the next endpoint elligable for harvesting, null if all
     *         endpoints have been cycled over.
     */
    public synchronized Endpoint next() {

        int endpointCount = xmlOverview.overviewType.getEndpoint().size();

        // find an endpoint not yet returned in this cycle
        for (int i = 0; i < endpointCount; i++) {

            // get the next endpoint in the overview
            EndpointType endpointType =
                    xmlOverview.overviewType.getEndpoint().get(i);
            // get the endpoint's adapter
            Endpoint endpoint = xmlOverview.getEndpoint(endpointType);

            // get today's date
            Date date = new Date ();
            // prepare it for ISO8601 representation
            DateTime dateTime = new DateTime(date);

            // get the date the endpoint was attempted
            DateTime attemptedDate = endpoint.getAttemptedDate();

            // check if the endpoint was attempted today
            if (attemptedDate.toString().equals(dateTime.toString())) {
                // endpoint was attempted today, skip it
            } else {
                if (endpointsCycled.contains(endpointType.getURI())) {
                    // endpoint is being attempted, skip it
                } else {
                    // add the endpoint to the list of endpoints attempted
                    endpointsCycled.add(endpointType.getURI());
                    // like before, return an adapter
                    return endpoint;
                }
            }
        }

        // no residual endpoint found
        return null;
    }

    @Override
    public boolean doHarvest(Endpoint endpoint) {

        // decide whether or not the endpoint should be harvested

        switch (cycleProperties.getHarvestMode()){

            case normal:
                if (endpoint.blocked()){
                    // endpoint has been (temporarily) removed from the cycle
                    return false;
                } else {
                    return true;
                }

            case retry:
                DateTime attempted, harvested;

                if (! endpoint.retry()){
                    // endpoint should not be retried
                    return false;
                } else {
                    attempted = endpoint.getAttemptedDate();
                    harvested = endpoint.getHarvestedDate();

                    if (attempted.equals(harvested)) {
                        // check if anything has happened
                        if (attempted.equals(new DateTime(0))){
                            // apparently not, do harvest
                            return true;
                        } else {
                            /* At some point in time the cycle tried and
                               harvested the endpoint. Therefore, there is no
                               need for it to retry.
                             */
                            return false;
                        }
                    } else {
                        if (attempted.isBefore(harvested)){
                            // this will not happen normally
                            return false;
                        } else {
                            /* After the most recent success, the cycle
                               attempted to harvest the endpoint but did not
                               succeed. It can therefore retry.
                            */
                            return true;
                        }
                    }
                }

            case refresh:
                if (endpoint.blocked()){
                    // at the moment, the cycle cannot harvest the endpoint
                    return false;
                } else {
                    return true;
                }
        }

        return false;
    }

    @Override
    public boolean doHarvest(String URI) {

        int endpointCount = xmlOverview.overviewType.getEndpoint().size();

        // find an endpoint
        for (int i = 0; i < endpointCount; i++) {

            EndpointType endpointType =
                    xmlOverview.overviewType.getEndpoint().get(i);

            if (endpointType.getURI().equals(URI)) {
                /* Found the endpoint, use adapter to return the endpoint that
                   corresponds to endpointType.
                 */

                return doHarvest(xmlOverview.getEndpoint(endpointType));
            }
        }

        /* The URI does not match the URI of any of the endpoints in the
           overview
         */
        return false;
    }

    @Override
    public DateTime getRequestDate(Endpoint endpoint) {

        // decide on a date the cycle can use when issuing an OAI request

        switch (cycleProperties.getHarvestMode()){

            case normal:
                if (endpoint.blocked()){
                    /* Since the cycle should not harvest the endpoint, it
                       does not need a date.
                     */
                    return new DateTime(0);
                } else {
                    // consider a selective harvest
                    if (! endpoint.allowIncrementalHarvest()){
                        // again, the cycle does not need a date
                        return new DateTime(0);
                    } else {
                        /* The cycle should use the date of the most recent
                           successful attempt
                         */
                        return new DateTime(endpoint.getHarvestedDate());
                    }
                }

            case retry:
                DateTime attempted, harvested;

                if (! endpoint.retry()){
                    // the cycle should not retry, so it does not need a date
                    return new DateTime(0);
                } else {
                    attempted = endpoint.getAttemptedDate();
                    harvested = endpoint.getHarvestedDate();

                    if (attempted.equals(harvested)) {
                        /* At some point in time the cycle tried and harvested
                           the endpoint. Therefore, there is no need for it to
                           retry.
                         */
                        return new DateTime(0);
                    } else {
                        /* After the most recent success, the cycle attempted
                           to harvest the endpoint but did not succeed. It can
                           therefore retry.
                         */
                        return new DateTime (endpoint.getHarvestedDate());
                    }
                }

            case refresh:

                /* No matter what the state of the endpoint is, for refreshing
                   it, the cycle can do without a date. Return the epoch date.
                 */

                return new DateTime(0);

            default:
                // all the members of the mode should be covered
                throw new Exception();
        }
    }
}
