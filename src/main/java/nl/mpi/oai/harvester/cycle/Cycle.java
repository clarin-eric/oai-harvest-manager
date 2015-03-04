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

/**
 * <br> Iterate over endpoints <br><br>
 *
 * A harvesting cycle is a guide to the client, a path along the endpoints
 * defined in the overview. A client can iterate over the endpoints already
 * present in the overview, or it can ask for a specific endpoint by supplying
 * its URI. <br><br>
 *
 * By interpreting the attributes recorded in the overview, the client can
 * decide if it needs to harvest the endpoint, and also, which method of
 * harvesting it should apply. <br><br>
 *
 * kj: where to put this
 * Whenever the client changes an attribute, the change will be reflected
 * back to the XML file. The adapter will perform this task. <br><br>
 *
 * Note: the interface does not make available general cycle attributes. To
 * determine whether or not to harvest an endpoint, the cycle should invoke
 * the doHarvest methods.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Cycle {

    /**
     * Get the next endpoint by externally supplied URI <br><br>
     *
     * By invoking next on a Cycle type object, a client receives the URI
     * identified endpoint. The cycle will remove the endpoint from the list
     * of endpoints in the overview that are elligable for harvesting.
     *
     * @param URI reference to the endpoint
     * @return the endpoint
     */
    public Endpoint next (String URI);

    /**
     * Get the next endpoint in the cycle <br><br>
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
     * Check if the endpoint should be harvested <br><br>
     *
     * When it decides whether or not the endpoint given should be harvested,
     * the method will consider both the general cycle attributes as well as
     * and endpoint attributes. <br><br>
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
     * Check if the endpoint indicated by the URI should be harvested <br><br>
     *
     * For this method, the same applies as in the case of the doHarvest method
     * with an EndpointType parameter.
     *
     * @param URI the endpoint considered
     * @return true if and only if the endpoint should be harvested
     */
    public boolean doHarvest (String URI);

    /**
     * Indicate whether all the endpoints in the overview were returned for
     * harvesting <br><br>
     *
     * In deciding whether or not the end of the cycle has been reached, the
     * method considers the endpoints stored in the overview. It cannot know
     * about endpoints the client would present the cycle with by passing an
     * identification to the next method.
     *
     * @return true if the end of the cycle has been reached, false otherwise
     */
    public boolean doneHarvesting ();

    /**
     * Retry the endpoints that gave rise to errors <br><br>
     *
     * When a client invokes the retry method on the cycle object, it restarts
     * the harvest cycle. The cycle will, from that moment on, only consider
     * endpoints that gave rise to errors. It will only consider an endpoint
     * which harvested and attempted attributes differ.
     */
    public void retry ();
}
