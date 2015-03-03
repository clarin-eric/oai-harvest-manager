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
 * <br> Create cycle type object <br><br>
 *
 * The factory returns a Overview type object. Different types of endpoint stores
 * could be supported, for the moment, there is only XML cycle.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class CycleFactory {

    public Cycle createCycle(String Overview){
        Cycle cycle = new XMLBasedCycle();

        // kj: do something to create the cycle

        return cycle;
    };
}
