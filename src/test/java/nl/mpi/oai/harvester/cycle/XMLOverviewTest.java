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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.*;
import java.util.Iterator;

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

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    /**
     * Test saving an overview in a file different from the original one
     */
    public void testAlternativeOverview () {

        // get the overview from an existing test XML overview file
        XMLOverview xmlOverview = new XMLOverview(TestHelper.getFilename(
                "/OverviewNormalMode.xml"));

        File originalFile = new File(TestHelper.getFilename(
                "/OverviewNormalMode.xml"));

        // save the overview under another name
        try {
            final File newFile = temporaryFolder.newFile(
                    "CopyOfNormalModeFile.xml");

            // save the overview in the temporary file, creating a copy
            xmlOverview.save(newFile.getPath()+newFile.getName());

            // compare the copy to the original file
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
        XMLOverview xmlOverview = new XMLOverview(TestHelper.getFilename(
                "/OverviewNormalMode.xml"));

        // first save a copy of the overview in a temporary folder

        try {
            final File newFile = temporaryFolder.newFile(
                    "CopyOfNormalModeFile.xml");

            // save the overview in the temporary file, creating a copy
            xmlOverview.save(newFile.getPath()+newFile.getName());

        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }

        // rotate the copy, it will remain in the temporary directory
        xmlOverview.rotateAndSave();

        // iterate over the temporary files

        String[] extensions = new String[] {"xml"};
        IOFileFilter filter = new SuffixFileFilter(extensions,
                IOCase.SENSITIVE);
        Iterator iterator = FileUtils.iterateFiles(temporaryFolder.getRoot(),
                filter, null);

        File file1 = (File) iterator.next();
        File file2 = (File) iterator.next();

        if (iterator.hasNext()){
            // there should only be two files in the temporary folder
            fail();
        }

        // both files should be equal
        try {
            assertTrue(FileUtils.contentEquals(file1, file2));
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

}
