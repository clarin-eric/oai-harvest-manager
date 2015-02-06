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

package nl.mpi.oai.harvester.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

/**
 * A directory used to save files, which may enforce rules on the file
 * structure below it. (In practice: it may require files to be
 * distributed in subdirectories to keep the number of files in one
 * directory below a set threshold.)
 *
 * @author Lari Lampen (MPI-PL)
 */
public class OutputDirectory {
    private static final Logger logger = Logger.getLogger(OutputDirectory.class);

    /** The maximum number of files in a directory, or 0 for no limit. */
    private final int limit;

    /** The base directory path. */
    private final Path base;

    // These are used to keep count of the current subdirectory and
    // the number of files in it.
    private int dirCounter = 0;
    private Path currentDir = null;
    private int fileCounter = 0;

    /**
     * Create a new instance with the specified base directory path
     * and no limit on the number of files on the top level of the
     * directory.
     */
    public OutputDirectory(Path base) throws IOException {
	this(base, 0);
    }

    /**
     * Create a new instance with the specified base directory path
     * and given limit (maximum number of files within a
     * subdirectory). If limit=0, no limit is enforced.
     */
    public OutputDirectory(Path base, int limit) throws IOException {
	this.base = base;
	this.limit = limit;

	// Start off by making sure the base directory actually exists.
	Util.ensureDirExists(base);

	if (limit > 0) {
	    // If we're using subdirectories and there already are
	    // some, skip the existing ones.
	    do {
		nextCurrentDir();
	    } while (Files.exists(currentDir));
	}
    }

    /**
     * Create a new subdirectory under this one which has the same file limit
     * constraint as this one.
     * 
     * @param name name of the new directory
     */
    public OutputDirectory makeSubdirectory(String name) throws IOException {
	return new OutputDirectory(base.resolve(name), limit);
    }

    /**
     * Given filename (without path), return a full canonical path to
     * a suitable location for that file, chosen in such a way that
     * the constraints placed on this directory are not breached.
     */
    public synchronized Path placeNewFile(String file) throws IOException {
	if (limit == 0) {
	    return base.resolve(file);
	}
	Util.ensureDirExists(currentDir);
	if (fileCounter < limit) {
	    fileCounter++;
	} else {
	    fileCounter = 1;
	    nextCurrentDir();
	    Util.ensureDirExists(currentDir);
	}
	return currentDir.resolve(file);
    }

    /**
     * Increment dirCounter and currentDir to point to the next
     * subdirectory, which may or may not exist.
     */
    private void nextCurrentDir() {
	dirCounter++;
	currentDir = base.resolve(String.format("%04d", dirCounter));
    }

    @Override
    public String toString() {
	if (limit > 0)
	    return base.toString() + " [limit " + limit + "]";
	return base.toString();
    }

    @Override
    public int hashCode() {
	return base.hashCode() + 29 * limit;
    }
    @Override
    public boolean equals(Object o) {
	if (o instanceof OutputDirectory) {
	    OutputDirectory od = (OutputDirectory)o;
	    return (base.equals(od.base) && limit == od.limit);
	}
	return false;
    }
}
