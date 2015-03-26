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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * <br> Tests targeting the cycle package <br><br>
 *
 * The methods in this class intend to check if the cycle properties and
 * endpoint adapter classes correctly substitute defaults for elements missing
 * from the overview file.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 *
 */
public class AdapterTest {

    // remember the new value when reading back from the updated overview
    DateTime newAttempted, newHarvested;
    Long newCount, newIncrement;

    @Test
    public void CyclePropertiesTest (){
        // kj: determine what to include in this test
    }

    @Test
    /**
     * <br> Test adapter methods in the EndpointAdapter class. Also test the
     * establishment of default values. Test if the adapter correctly reflects
     * the changes induced by the test.
     *
     * Note: the test only covers the methods in the endpoint interface that
     * the methods in the CycleTest class do not cover. These methods typically
     * support the decision whether or not to harvest the endpoint. Since the
     * doHarvest method in the Cycle class invokes the adapter's constructor,
     * the EndpointAdapterTest does not need to test cover it.
     */
    public void EndpointAdapterTest (){

        // create a CycleFactory
        CycleFactory factory = new CycleFactory();

        // get a cycle based on the test file
        Cycle cycle = factory.createCycle(TestHelper.getFilename(
                "/OverviewNormalMode.xml"));

        // kj: work on a copy of the overview file instead, check read back

        // first endpoint
        Endpoint endpoint = cycle.next();

        // check the endpoint URI
        assertEquals("http://www.endpoint1.org", endpoint.getURI());

        // check the group the endpoint belongs to
        assertEquals("group1", endpoint.getGroup());

        /* Test the doneHarvesting method. First get both the attempted and
           harvested date.
         */
        DateTime attempted = endpoint.getAttemptedDate();
        DateTime harvested = endpoint.getHarvestedDate();

        // indicate harvest failure
        endpoint.doneHarvesting(false);

        // this should only have updated the attempted date
        assertEquals(harvested, endpoint.getHarvestedDate());

        // the original attempted date should precede the current date
        if (! attempted.isBefore(endpoint.getAttemptedDate())){
            fail();
        }

        // now indicate success
        endpoint.doneHarvesting(true);

        newAttempted = endpoint.getAttemptedDate();
        newHarvested = endpoint.getHarvestedDate();

        // both dates should now be the same
        assertEquals(newAttempted, newHarvested);

        // either one should succeed the original dates
        if (! attempted.isBefore(newAttempted)){
            fail();
        }

        // by default, the record count should be zero
        assertEquals(endpoint.getCount(), 0);

        // set the count, and compare it to the count reported
        endpoint.setCount(314L);
        newCount = endpoint.getCount();
        assertEquals((Long)314L, newCount);

        // by default, the increment should also be zero
        assertEquals(endpoint.getIncrement(), 0);

        // set the increment, and compare it to the increment reported
        endpoint.setIncrement(27L);
        newCount = endpoint.getIncrement();
        assertEquals((Long)27L, newIncrement);
    }

}


