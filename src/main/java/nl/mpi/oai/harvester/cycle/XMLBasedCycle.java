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
 * <br> Implement Cycle based on XML overview <br><br>
 *
 * kj: implement the interface
 *
 * Note: some methods need to be synchronised.
 *
 * Note: use the adapters to access the attributes defined in the XML file.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class XMLBasedCycle implements Cycle {

    @Override
    public synchronized Endpoint next(String URI) {
        return null;
    }

    @Override
    public synchronized Endpoint next() {
        return null;
    }

    @Override
    public boolean doHarvest(Endpoint endpoint) {
        return false;
    }

    @Override
    public boolean doHarvest(String URI) {
        return false;
    }

    @Override
    public synchronized boolean doneHarvesting() {
        return false;
    }

    @Override
    public void retry() {

    }
}
