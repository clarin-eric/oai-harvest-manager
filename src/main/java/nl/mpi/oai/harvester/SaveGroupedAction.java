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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the action of saving a record onto the file system
 * with a directory structure grouped by originating provider.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class SaveGroupedAction extends SaveAction implements Action {
    /**
     * The subdirectories are stored in a hash indexed by provider.
     */
    private final Map<Provider, OutputDirectory> locations;

    public SaveGroupedAction(OutputDirectory dir) {
	super(dir);
	locations = Collections.synchronizedMap(new HashMap<Provider, OutputDirectory>());
    }

    /**
     * Copy constructor that makes a SHALLOW copy. Thus the copy shares the
     * set of subdirectories with the given action.
     */
    private SaveGroupedAction(SaveGroupedAction sga) {
	super(sga.dir);
	locations = sga.locations;
    }

    @Override
    protected Path chooseLocation(MetadataRecord record) throws IOException {
	Provider prov = record.getOrigin();
	if (!locations.containsKey(prov)) {
	    OutputDirectory provDir = dir.makeSubdirectory(prov.getName());
	    locations.put(prov, provDir);
	}
	return locations.get(prov).placeNewFile(Util.toFileFormat(record.getId()));
    }

    @Override
    public String toString() {
	return "save to " + dir + " grouped by provider";
    }

    // Save actions are equal iff the directories are the same (but
    // grouping is a distinguishing factor).
    @Override
    public int hashCode() {
	return dir.hashCode() + 13;
    }
    @Override
    public boolean equals(Object o) {
	if (o instanceof SaveGroupedAction) {
	    SaveGroupedAction a = (SaveGroupedAction)o;
	    return dir.equals(a.dir);
	}
	return false;
    }

    @Override
    public Action clone() {
	// This is a shallow copy, resulting in multiple references to
	// a single OutputDirectory, which is as intended.
	return new SaveGroupedAction(this);
    }
}
