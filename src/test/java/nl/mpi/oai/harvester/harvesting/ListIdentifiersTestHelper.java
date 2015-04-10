
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

package nl.mpi.oai.harvester.harvesting;

import java.util.ArrayList;

/**
 * <br> Define the traces for a test targeting the list identifiers scenario <br><br>
 *
 * A trace definition needs to be added for every record the scenario for
 * harvesting yields. By letting the addToTable method spy on the metadata
 * constructor invoked each time a scenario creates a record, in the end the
 * test should end up with an empty list of traces.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class ListIdentifiersTestHelper extends TestHelper {

    @Override
    ArrayList<Trace> getTraces() {

        ArrayList<Trace> traces= new ArrayList<>();

        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0000"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0001"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0002"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0003"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0004"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0005"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0006"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0007"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0008"));
        traces.add(new Trace ("http://www.endpoint1.org", "cmdi", "0009"));

        return traces;
    }

    @Override
    String[] getEndpointURIs() {
        // a table of endpoint URIs
        final String[] endpointURIs;

        endpointURIs = new String[]{
                "http://www.endpoint1.org",
                "http://www.endpoint2.org",
                "http://www.endpoint3.org"};

        return endpointURIs;
    }

    @Override
    String getTestName() {

        return "ListIdentifiers";
    }
}
