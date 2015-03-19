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

import org.joda.time.DateTime;

/**
 * <br> The way to iterate over OAI endpoints <br><br>
 *
 * This is the main interface of the cycle package. A client to the package
 * can invoke methods specified in this interface to iterate over the endpoints
 * in an overview. They return endpoint after endpoint, or give an indication
 * whether or not the endpoint should be harvested. In case it should, the
 * client can invoke a method to obtain the date to build an OAI request on.
 *
 * A harvesting cycle is a guide to the client, a path along the endpoints
 * defined in the overview. A client can iterate over the endpoints already
 * present in the overview, or it can ask for a specific endpoint by supplying
 * its URI. <br><br>
 *
 * By interpreting the properties recorded in the overview, the client can
 * decide if it needs to harvest the endpoint, and also, which method of
 * harvesting it should apply. <br><br>
 *
 * Note: whenever the client changes an attribute, the change will be reflected
 * back to the XML file. The endpoint adapter will perform this task. <br><br>
 *
 * Note: the interface does not make available general cycle properties. To
 * determine whether or not to harvest an endpoint, the cycle should invoke
 * the doHarvest methods.
 *
 * Note: the XMLBasedCycle class included in the package is an example of an
 * implementation of the interface.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Cycle {

    /**
     * <br> Get the next endpoint by an externally supplied URI <br><br>
     *
     * By invoking next on a Cycle type object, a client receives the URI
     * identified endpoint. The cycle will remove the endpoint from the list
     * of endpoints in the overview that are elligable for harvesting.
     *
     * @param URI reference to the endpoint
     * @param group the group the endpoint belongs to
     * @return the endpoint
     */
    public Endpoint next (String URI, String group);

    /**
     * <br> Get the next residual endpoint in the cycle <br><br>
     *
     * Next to endpoints the client identifies, there might be endpoints in
     * the overview that have, in the current cycle, for some reason, not been
     * identified yet. When the client invokes the next method on the cycle
     * object without a URI, the method will return one of these endpoints and
     * remove it from the list of endpoints available for harvesting.
     *
     * @return an endpoint not visited in the current cycle
     */
    public Endpoint next ();

    /**
     * <br> Check if the endpoint should be harvested <br><br>
     *
     * When it decides whether or not the endpoint given should be harvested,
     * the method will consider both the general cycle properties as well as
     * the endpoint properties. <br><br>
     *
     * If the cycle is in normal mode, the method will allow the client to
     * harvest the endpoint if endpoint's block attribute equals false. It will
     * allow selective harvest if on top of that, the incremental attribute
     * equals true. In that case the client should use the harvested attribute
     * to set the OAI request date parameter. <br><br>
     *
     * If the cycle is in retry mode, the same applies as in the case of the
     * the normal mode, except that method will return true if the endpoint's
     * retry attribute is equal to true. <br><br>
     *
     * If the cycle is in refresh mode, again, the same applies as in the case
     * of the normal mode, except that the date will now be determined by the
     * overview's date attribute takes the place of the endpoint's harvested
     * attribute.
     *
     * @param endpoint the endpoint considered
     * @return true if and only if the endpoint should be harvested
     */
    public boolean doHarvest (Endpoint endpoint);

    /**
     * <br> Check if the endpoint indicated by the URI should be harvested <br><br>
     *
     * For this method, the same applies as in the case of the doHarvest method
     * with an EndpointType parameter.
     *
     * @param URI the endpoint considered
     * @return true if and only if the endpoint should be harvested
     */
    public boolean doHarvest (String URI);

    /**
     * <br> Get the date needed when issuing the OAI request <br><br>
     *
     * The date returned depends both on the mode of the cycle as well as on
     * the properties of the endpoint.
     *
     * In normal mode, it will return the date for harvesting, provided the
     * endpoint has not been blocked. In retry mode, it will return the date,
     * only if the endpoint allows a retry. In refresh mode, the method will
     * always return the epoch date.
     *
     * Note: before issuing an OAI request, the cycle should first find out if
     * it should harvest the endpoint or not. After that, it can determine the
     * date it should use for building the request.
     */
    public DateTime getRequestDate (Endpoint endPoint);

}
