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

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.apache.log4j.helpers.Loader.getResource;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <br> Helper methods for testing <br><br>
 *
 * create a getFile method at a higher level, for all the tests to use
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
class TestHelper {

    /**
     * <br> Get the resource as a File type object <br><br>
     *
     * Note: the file name does not include a parent path specification
     *
     * @param resourceName file name of the resource
     * @return a File type object reference to the resource
     */
    public static File getFile(String resourceName){

        // get the URL of the test file in the resources directory
        URL url = TestHelper.class.getResource(resourceName);

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

        return new File (uri.getPath());
    }

    /**
     * <br> Copy a file to a temporary folder <br><br>
     *
     * Note: by creating a copy of a file in the resources folder, tests can
     * change the contents of a file without consequences.
     *
     * Note: there might be easier ways to copy a file. By creating a copy
     * of the overview by invoking the save method on an overview object, the
     * helper method is a test in itself.
     *
     * @param temporaryFolder folder in which to create the new file
     * @param originalFile the original file
     * @param newFileName file name, no path
     * @return the new file as a file type object
     */
    static File copyToTemporary (TemporaryFolder temporaryFolder,
                                 File originalFile, String newFileName) {

        // get the overview from an existing test XML overview file
        final XMLOverview xmlOverview = new XMLOverview(originalFile);

        // try to save the overview under another, new name
        File newFile = null;
        try {
            // create a new temporary file
            newFile = temporaryFolder.newFile(newFileName);

            // save the overview in the temporary file, creating a copy
            xmlOverview.save(newFile);
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }

        return newFile;
    }
}
