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

/**
 * kj: this was taken from the cycle interface, try to adapt it
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
 * Note: the XMLBasedCycle class included in the package is an example of an
 * implementation of the interface.
 *
 * <p><IMG SRC="doc-files/package overview - cycle.svg">
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
package nl.mpi.oai.harvester.cycle;