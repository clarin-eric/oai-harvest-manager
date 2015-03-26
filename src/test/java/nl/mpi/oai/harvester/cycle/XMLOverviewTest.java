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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import static org.apache.log4j.helpers.Loader.getResource;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <br> Tests targeting the cycle package <br><br>
 *
 * The methods in this class should check if the XMLOverview class correctly
 * saves an overview, saves it under a name different from the name of the
 * original file, and finally, if it correctly rotates overview files.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class XMLOverviewTest {

    // setup a temporary folder for the test, use the junit rule for it
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    /**
     * Test saving a harvest overview in a file different from the original one
     */
    public void testAlternativeOverview () {

        // kj: invoke the copyToTemporary method instead

        // get the overview from an existing test XML overview file
        final XMLOverview xmlOverview = new XMLOverview(TestHelper.getFilename(
                "/OverviewNormalMode.xml"));

        // get the file containing the overview
        final File originalFile = new File(TestHelper.getFilename(
                "/OverviewNormalMode.xml"));

        // try to save the overview under another name
        try {
            // create a new temporary file
            final File newFile = temporaryFolder.newFile(
                    "CopyOfNormalModeFile.xml");

            // save the overview in the temporary file, creating a copy
            xmlOverview.save(newFile);

            // try to compare the copy to the original file
            try {
                assertTrue(FileUtils.contentEquals(originalFile, newFile));
            } catch (IOException e) {
                fail();
                e.printStackTrace();
            }
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    /**
     * Test rotating overview files
     */
    public void testOverviewRotate (){

        // get the overview from a test XML overview file
        final XMLOverview xmlOverview = new XMLOverview(TestHelper.getFilename(
                "/OverviewNormalMode.xml"));

        /* Instead of rotating the overview file itself, test by rotating a
           copy of this file. Therefore, try to save a copy of the overview
           in a temporary folder first.
         */
        try {
            // create a new temporary file
            final File newFile = temporaryFolder.newFile(
                    "CopyOfNormalModeFile.xml");

            // save the overview in the temporary file, creating a copy
            xmlOverview.save(newFile);

        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }


        /* Now rotate the copy. This will create a file with a date and
           timestamp, and a new file containing the overview.
         */
        xmlOverview.rotateAndSave();


        // create a filter for finding the overview XML files
        String[] allowedExtensions = new String[] {"xml"};
        IOFileFilter filter = new SuffixFileFilter(allowedExtensions,
                IOCase.SENSITIVE);

        // create an iterator based on the filter
        Iterator iterator = FileUtils.iterateFiles(temporaryFolder.getRoot(),
                filter, null);

        // iterate over the temporary files
        File file1 = (File) iterator.next();
        File file2 = (File) iterator.next();

        if (iterator.hasNext()){
            // there should only be two files in the temporary folder
            fail();
        }

        // determine which file is the rotated file and which is the new file
        File rotatedFile, newFile;

        int atIndex = file1.getPath().lastIndexOf(" at ");

        if (atIndex < 0){
            // did not find it in this file
            atIndex = file2.getPath().lastIndexOf(" at ");
            if (atIndex < 0){
                // did not find it in this file either
                rotatedFile = null;
                newFile = null;
                fail();
            } else {
                rotatedFile = file2;
                newFile = file1;
            }
        } else {
            rotatedFile = file1;
            newFile = file2;
        }

        // determine the index of the first character in the timestamp
        int first = atIndex + 4;

        // determine the index of the last character in the timestamp
        int last = rotatedFile.getPath().lastIndexOf("xml") - 1;

        // get the timestamp from the rotated file
        String timeStamp = rotatedFile.getPath().substring(first, last);

        // the timestamp should be before now
        DateTime rotatedAt = new DateTime(timeStamp);
        assertTrue(rotatedAt.isBeforeNow());

        // the path of the files up to the timestamp should be equal
        String partOfRotated = rotatedFile.getPath().substring(0, atIndex);
        String partOfNew = newFile.getPath().substring(0, atIndex);
        assertTrue(partOfRotated.equals(partOfNew));

        // both files should have equal content
        try {
            assertTrue(FileUtils.contentEquals(rotatedFile, newFile));
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

}
