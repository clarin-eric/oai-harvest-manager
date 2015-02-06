/*
 * Copyright (C) 2014, The Max Planck Institute for
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

package nl.mpi.oai.harvester;

import java.io.File;
import java.io.IOException;

import nl.mpi.oai.harvester.control.Util;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for the Util class. (This is an integration test since it involves
 * the file system.)
 *
 * @author Lari Lampen (MPI-PL)
 */
public class UtilTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Test of Util.ensureDirExists: folder exists.
     */
    @Test
    public void testEnsureDirExists_Noop() throws Exception {
	File dir = folder.newFolder("a");
	Util.ensureDirExists(dir.getAbsolutePath());
	assertTrue(dir.exists());
	assertTrue(dir.isDirectory());
    }

    /**
     * Test of Util.ensureDirExists: folder doesn't exist.
     */
    @Test
    public void testEnsureDirExists_Create() throws Exception {
	File dir = new File(folder.getRoot(), "b");
	assertFalse(dir.exists());
	Util.ensureDirExists(dir.getAbsolutePath());
	assertTrue(dir.exists());
	assertTrue(dir.isDirectory());
    }

    /**
     * Test of Util.ensureDirExists: file exists.
     */
    @Test(expected = IOException.class) 
    public void testEnsureDirExists_Conflict() throws Exception {
	File dir = folder.newFile("c");
	Util.ensureDirExists(dir.getAbsolutePath());
    }    

    /**
     * Test of toFileFormat with various unacceptable characters.
     */
    @Test
    public void testToFileFormat_Complex() {
	String a = "Bad characters: §± 仕方が無い ōøäö";
	String b = Util.toFileFormat(a);
	assertTrue(b.matches("^\\w*$"));
    }

    /**
     * Test of toFileFormat with null input.
     */
    @Test
    public void testToFileFormat_Null() {
	String a = Util.toFileFormat(null);
	assertNull(a);
    }
}
