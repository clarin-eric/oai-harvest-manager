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
import nl.mpi.oai.harvester.generated.OverviewType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * <br> Implement Cycle based on XML overview <br><br>
 *
 * note: use the methods in the XMLOverview class, receive objects through the
 * Overview and Endpoint interface
 *
 * note: this class relies on JAXB generated types.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class XMLBasedCycle implements Cycle {

    // overview marshalling object
    private final XMLOverview xmlOverview;

    // the general attributes defined by the XML file
    private final Overview overview;

    // the endpoint URIs returned to the client in the current cycle
    private ArrayList<String> endpointsCycled;

    /**
     * Associate the cycle with the XML file defining the overview
     *
     * @param filename name of the XML file defining the overview
     */
    public XMLBasedCycle (String filename){

        // create an overview marshalling object
        xmlOverview = new XMLOverview(filename);

        overview = xmlOverview.getOverview();
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
     * Note: the method needs synchronisation because endpoints might be
     * harvested in parallel.
     *
     * kj: this method might return null
     * This could be prevented by invoking doneHarvesting()
     */
    public synchronized Endpoint next() {

        // JAXB generated type
        OverviewType overviewType = xmlOverview.overviewType;

        EndpointType endpointType = null;

        boolean found = false;

        /* Only respond with particular endpoint once. In order to decide
           compare today's date to the date of the last attempt. This date
           will be set when the harvest attempt is done, either successful
           or not. So we have to keep track of the endpoints returned by
           remembering their URI's
         */

        for (int i = 0; i < overviewType.getEndpoint().size() && !found; i++) {
            endpointType = overviewType.getEndpoint().get(i);

            // kj: consider joda time
            // get today's date in ISO 8601 format
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            String today = dateFormat.format(calendar.getTime());

            // get the date of the most recent attempt in XML format
            String attemptedDate = endpointType.getAttempted().toString();

            if (attemptedDate.equals(today)){
                // skip the endpoint
            } else {
                found = true;
                // add the endpoint to the list
                endpointsCycled.add(endpointType.getURI());
                return xmlOverview.getEndpoint(endpointType.getURI(), endpointType.getGroup());
            }
        }

        return null;
    }

    // kj: implement the interface

    @Override
    public boolean doHarvest(Endpoint endpoint) {

        // decide whether or not the endpoint should be harvested

        if (overview.getHarvestFromDate().toString().equals("")) {
            // this is an example
        } else {
        }

        return false;
    }

    @Override
    public boolean doHarvest(String URI) {

        Endpoint endpoint = null;

        // find the endpoint

        // invoke the doHarvest method, now send it the endpoint
        return doHarvest(endpoint);
    }

    @Override
    public synchronized boolean doneHarvesting() {
        return false;
    }

    @Override
    public void retry() {

    }
}
