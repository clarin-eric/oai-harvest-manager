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

package nl.mpi.oai.harvester.overview;

/**
 * <br> Iterate over endpoints <br><br>
 *
 * You can iterate by supplying the name, or by iterating over the endpoints
 * in the overview. The methods on a Cycle type of object return
 * endpoints, either by a URI specified externally, or by the endpoints stored
 * in the overview.
 *
 * A method could also return the parameters defined in the overview. If the
 * client to the package, the worker, the harvesting package need the general
 * parameters, they can be made available through the cycle object. Otherwise,
 * the parameters could be made private to the package. They will then only be
 * available to classes inside the package. kj: need to decide this
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Cycle {

    /**
     * Get next endpoint by externally supplied URI
     * @param URI reference to the endpoint
     * @return the endpoint
     */
    public Endpoint next (String URI);

    /**
     * Get the next endpoint in the overview
     *
     * Note:
     *
     * @return
     */
    public Endpoint next ();

    /**
     * Indicate whether all the endpoints in the overview have been visited
     *
     * @return
     */
    public boolean doneHarvesting ();

    /**
     * Retry the endpoints that gave rise to errors
     */
    public void retry ();
}
