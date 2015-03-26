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
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
class TestHelper {

    /**
     * Get the path and filename of a resource
     *
     * @param resourceName the resource
     * @return the path and filename of the resource
     */
    static String getFilename (String resourceName){

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

        return filename;
    }

    /**
     * kj: add specification
     *
     * @param temporaryFolder
     * @param fileName
     * @param newFileName
     */
    static void copyToTemporary (TemporaryFolder temporaryFolder,
                                 String fileName, String newFileName) {

        // get the overview from an existing test XML overview file
        final XMLOverview xmlOverview = new XMLOverview(TestHelper.getFilename(
                fileName));

        // get the file containing the overview
        final File originalFile = new File(TestHelper.getFilename(fileName));

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
    }
}
