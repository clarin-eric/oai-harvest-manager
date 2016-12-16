
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
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <br> Tests targeting the cycle package <br><br>
 *
 * The methods in this class should check if the cycle class correctly reflects
 * the data gathered in the overview, data collected during previous harvest
 * attempts. The methods should also check if the Cycle class methods correctly
 * decide on if and when to harvest an endpoint.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class CycleTest {

    // zero epoch time in the UTC zone
    final DateTime zeroUTC = new DateTime ("1970-01-01T00:00:00.000+00:00",
            DateTimeZone.UTC);

    @Test
    /**
     * Test reflecting and deciding based on an endpoint overview stored in
     * an XML file.
     */
    public void testNormalMode (){

        // create a CycleFactory
        CycleFactory factory = new CycleFactory();

        // get a cycle based on the test file
        Cycle cycle = factory.createCycle(TestHelper.getFile(
                "/OverviewNormalMode.xml"));

        // first endpoint
        Endpoint endpoint = cycle.next();

        // check the endpoint URI
        assertEquals("http://www.endpoint1.org", endpoint.getURI());
        /* While the endpoint allows incremental harvesting, the client has
           not attempted the endpoint before, so it should now try to harvest
           it from the beginning.
         */
        assertEquals(zeroUTC, cycle.getRequestDate(endpoint));

        // second endpoint
        endpoint = cycle.next();

        // third endpoint
        endpoint = cycle.next();
        // because of a block, the client should not harvest this endpoint
        assertFalse(cycle.doHarvest(endpoint));

        // fourth endpoint
        endpoint = cycle.next();
        // the endpoint does not support incremental harvesting
        assertTrue(cycle.doHarvest(endpoint));
        assertEquals(zeroUTC, cycle.getRequestDate(endpoint));

        DateTime dateTime = new DateTime("2014-07-19T00:00:00.000Z",
                DateTimeZone.UTC);

        // fifth endpoint
        endpoint = cycle.next();
        // the client should harvest this endpoint
        assertTrue(cycle.doHarvest(endpoint));
        assertEquals(dateTime, cycle.getRequestDate(endpoint));
    }

    @Test
    /**
     * Test reflecting and deciding based on an endpoint overview stored in
     * an XML file.
     */
    public void testRetryMode() {

        // create a CycleFactory
        CycleFactory factory = new CycleFactory();

        // get a cycle based on the test file
        Cycle cycle = factory.createCycle(TestHelper.getFile(
                "/OverviewRetryMode.xml"));

        // first endpoint
        Endpoint endpoint = cycle.next();

        // assert some element values
        assertEquals("http://www.endpoint1.org", endpoint.getURI());
        assertFalse(endpoint.blocked());
        assertTrue(endpoint.retry());
        // with no date in the overview, this endpoint should be harvested
        assertTrue(cycle.doHarvest(endpoint));

        DateTime dateTime = new DateTime("2014-07-21T00:00:00.000Z",
                DateTimeZone.UTC);

        // second endpoint
        endpoint = cycle.next();
        // according to the overview, the endpoint needs another attempt
        assertTrue(cycle.doHarvest(endpoint));
        /* Assert the date to use in the request, note that endpoint does not
           allow for incremental harvesting.
         */
        assertEquals(dateTime, cycle.getRequestDate(endpoint));

        dateTime = new DateTime("2014-12-11T00:00:00.000Z", DateTimeZone.UTC);

        // third endpoint
        endpoint = cycle.next();
        // this endpoint does allow incremental harvesting
        assertEquals(dateTime, cycle.getRequestDate(endpoint));

        // fourth endpoint
        endpoint = cycle.next();
        /* Since it does not allow incremental harvesting, the client should
           harvest the endpoint from the beginning.
         */
        assertEquals(zeroUTC, cycle.getRequestDate(endpoint));

        // fifth endpoint
        endpoint = cycle.next();
        /* When it would allow a retry, the client would need to harvest it
           from the beginning.
         */
        assertEquals(zeroUTC, cycle.getRequestDate(endpoint));
        // however, the endpoint does not allow a retry
        assertFalse(endpoint.retry());
    }

    @Test
    /**
     * Test reflecting and deciding based on an endpoint overview stored in
     * an XML file.
     */
    public void testRefreshMode (){

        // create a CycleFactory
        CycleFactory factory = new CycleFactory();

        // get a cycle based on the test file
        Cycle cycle = factory.createCycle(TestHelper.getFile(
                "/OverviewRefreshMode.xml"));

        // walk over the elements in the file, and assert some of its values

        // first endpoint
        Endpoint endpoint = cycle.next();

        // as it has not tried it before, the client should harvest the endpoint
        assertTrue(cycle.doHarvest(endpoint));
        // from the beginning
        assertEquals(zeroUTC, cycle.getRequestDate(endpoint));

        // second endpoint
        endpoint = cycle.next();

        // check the endpoint URI
        assertEquals("http://www.endpoint2.org", endpoint.getURI());
        /* While the endpoint allows incremental harvesting, in refresh mode the
           client should be led to disregarding this.
         */
        assertTrue(cycle.doHarvest(endpoint));
        assertEquals(zeroUTC, cycle.getRequestDate(endpoint));

        // third endpoint
        endpoint = cycle.next();
        // the client should not refresh a blocked endpoint
        assertFalse(cycle.doHarvest(endpoint));

        // fourth endpoint
        endpoint = cycle.next();
        /* According to the overview the client has not successfully harvested
           the endpoint before.
         */
        assertTrue(cycle.doHarvest(endpoint));
        assertEquals(zeroUTC, cycle.getRequestDate(endpoint));
    }
}
