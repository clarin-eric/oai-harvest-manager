
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

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * <br> Tests targeting cycle package <br><br>
 *
 * The methods in this class should check if the cycle package correctly
 * reflects the data gathered in the overview, data collected during previous
 * harvest attempts. The methods should also check if the Cycle class methods
 * correctly decide on if and when to harvest an endpoint.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class CycleTest {

    @Test
    /**
     * Test reflecting and deciding based on an endpoint overview stored in
     * an XML file.
     */
    public void overviewToCycle () {

        // get the URL of the test file in the resources directory
        URL url = CycleTest.class.getResource("/overview.xml");

        if (url == null){
            // fail the test
            fail();
        }

        // convert it to a URI to be able to convert the escaped path component
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            uri = null;
            e.printStackTrace();
        }

        if (uri == null){
            // fail the test
            fail();
        }

        String filename;
        // get the path without escape characters as needed by the CycleFactory
        filename = uri.getPath();

        // create a CycleFactory
        CycleFactory factory = new CycleFactory();

        // get a cycle based on the test file
        Cycle cycle = factory.createCycle(filename);

        // walk over the elements in the file, and assert some of its values

        // first endpoint
        Endpoint endpoint = cycle.next();

        // assert the element values
        assertEquals("http://www.endpoint1.org", endpoint.getURI());
        assertFalse(endpoint.blocked());
        assertTrue(endpoint.retry());
        assertTrue(endpoint.allowIncrementalHarvest());
        // with no date in the overview, this endpoint should be harvested
        assertTrue(cycle.doHarvest(endpoint));

        // second endpoint
        endpoint = cycle.next();
        // according to the overview, the endpoint needs another attempt
        assertTrue(cycle.doHarvest(endpoint));
        /* Assert the date to use in the request, note that endpoint does not
           allow for incremental harvesting
         */
        assertEquals("2014-07-21T00:00:00.000+02:00",
                cycle.getRequestDate(endpoint).toString());

        // third endpoint
        endpoint = cycle.next();
        // this endpoint does allow incremental harvesting
        assertEquals("2014-12-11T00:00:00.000+01:00",
                cycle.getRequestDate(endpoint).toString());

        // fourth endpoint
        endpoint = cycle.next();
        /* Since it does not allow incremental harvesting, the endpoint should
           be harvested from the beginning.
         */
        assertEquals("1970-01-01T01:00:00.000+01:00",
                cycle.getRequestDate(endpoint).toString());

        // fifth endpoint
        endpoint = cycle.next();
        /* When it would allow a retry, then endpoint would need to be
           harvested from the beginning
         */
        assertEquals("1970-01-01T01:00:00.000+01:00",
                cycle.getRequestDate(endpoint).toString());
        // however, the endpoint does not allow a retry
        assertFalse(endpoint.retry());
    }
}
