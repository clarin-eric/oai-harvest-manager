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

import nl.mpi.oai.harvester.cycle.CycleProperties.Mode;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

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
    long newCount, newIncrement;

    // setup a temporary folder for the test, use the junit rule for it
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    /**
     * Test the methods in the CycleProperties adapter class.
     */
    public void CyclePropertiesTest (){

        // get an overview file from the resources folder
        File overviewFile = TestHelper.getFile("/OverviewNormalMode.xml");

        // get the overview from an existing test XML overview file
        final XMLOverview xmlOverview = new XMLOverview(overviewFile);

        // check if the CycleProperties adapter returns the expected values
        Mode mode;

        mode = xmlOverview.getCycleProperties().getHarvestMode();

        // compare the object values against those defined in the overview file
        assertEquals(mode.toString(), "normal");
    }

    @Test
    /**
     * <br> Test methods in the EndpointAdapter class: check if the methods
     * provide the correct default values, and check if the methods correctly
     * reflect test induced changes back to the overview file. <br><br>
     *
     * Note: the test only covers the methods in the endpoint interface that
     * the methods in the CycleTest class do not cover. These methods typically
     * support the decision whether or not to harvest the endpoint. Since the
     * doHarvest method in the Cycle class invokes the adapter's constructor,
     * the EndpointAdapterTest does not need to test cover it.
     */
    public void EndpointAdapterTest (){

        // get an overview file from the resources folder
        File overview = TestHelper.getFile("/OverviewNormalMode.xml");

        // copy it to the temporary folder
        File copyOfOverview = TestHelper.copyToTemporary(temporaryFolder,
                overview, "copy.xml");

        // create a CycleFactory
        CycleFactory factory = new CycleFactory();

        // work on the temporary copy of a resources overview file
        Cycle cycle = factory.createCycle(copyOfOverview);

        // look at the first endpoint
        Endpoint endpoint = cycle.next();

        // check the endpoint URI
        assertEquals("http://www.endpoint1.org", endpoint.getURI());

        // check the group the endpoint belongs to
        assertEquals("group1", endpoint.getGroup());

        // Test the doneHarvesting method. First get both the attempted and
        // harvested date.
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

        // remember the date of the attempt en harvest
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
        assertEquals(314L, newCount);

        // by default, the increment should also be zero
        assertEquals(endpoint.getIncrement(), 0);

        // set the increment, and compare it to the increment reported
        endpoint.setIncrement(27L);
        newIncrement = endpoint.getIncrement();
        assertEquals(27L, newIncrement);

        // create a new cycle based on the changed file
        cycle = factory.createCycle(copyOfOverview);

        // look at the first endpoint again
        endpoint = cycle.next();

        // read back the new values, and check if they have indeed been changed

        assertEquals(endpoint.getAttemptedDate(), newAttempted);
        assertEquals(endpoint.getHarvestedDate(), newHarvested);
        assertEquals(endpoint.getCount(), newCount);
        assertEquals(endpoint.getIncrement(), newIncrement);
    }
}


