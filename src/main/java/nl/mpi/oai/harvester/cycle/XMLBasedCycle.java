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
 * note: use the methods in the XMLOverview class, receive objects through the
 * Overview and Endpoint interface
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class XMLBasedCycle implements Cycle {

    /**
     *
     * @param filename
     */
    public XMLBasedCycle (String filename){

        /* Create an XMLOverview class object by using the JAXB generated types
           reflecting the OverviewType XSD
         */
        XMLOverview xmlOverview = new XMLOverview(filename);

        Overview overview;

        overview = xmlOverview.getOverview();

        // in the overview you only get the general attributes
        // use the overview interface

        xmlOverview.getEndpoint("endpointURI", "group");

        /* Note: there should be a setEndpoint also, for reflecting back
           changes to the overview file.
        */
    }

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
