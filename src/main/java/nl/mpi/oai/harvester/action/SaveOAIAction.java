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

package nl.mpi.oai.harvester.action;

import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.control.OutputDirectory;
import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.control.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;

/**
 * This class represents the action of saving a record onto the file system
 * with a directory structure grouped by originating provider.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class SaveOAIAction extends SaveGroupedAction implements Action {
    /**
     * The subdirectories are stored in a hash indexed by provider.
     */
    private final Map<Provider, OutputDirectory> locations;

    /**
     * Create a new save action where output files are grouped in directories
     * per provider.
     * 
     * @param dir output directory to save to
     * @param suffix suffix to be added to identifier to generate filename
     */
    public SaveOAIAction(OutputDirectory dir, String suffix) {
	super(dir, suffix);
	locations = Collections.synchronizedMap(new HashMap<Provider, OutputDirectory>());
    }

    /**
     * Copy constructor that makes a SHALLOW copy. Thus the copy shares the
     * set of subdirectories with the given action.
     */
    private SaveOAIAction(SaveOAIAction sga) {
	super(sga.dir, sga.suffix);
	locations = sga.locations;
    }

    @Override
    protected Path chooseLocation(Metadata metadata) throws IOException {
	Provider prov = metadata.getOrigin();
	if (!locations.containsKey(prov)) {
	    OutputDirectory provDir = dir.makeSubdirectory(Util.toFileFormat(prov.getName()));
	    locations.put(prov, provDir);
	}
	return locations.get(prov).placeNewFile(Util.toFileFormat(
//                prov.getName() +
//                "-" +
//                (metadata.isList()?getDocument(metadata).hashCode():metadata.getId()) +
                metadata.getId() +
		suffix));
    }
    
    @Override
    public Document getDocument(Metadata metadata) {
        return metadata.getResponse();
    }


    @Override
    public String toString() {
	return super.toString() + " grouped by provider (OAI)";
    }

    // Save actions are equal iff the directories are the same (but
    // grouping is a distinguishing factor).
    @Override
    public int hashCode() {
	return dir.hashCode() + 29 * suffix.hashCode() + 13;
    }
    @Override
    public boolean equals(Object o) {
	if (o instanceof SaveOAIAction) {
	    SaveOAIAction a = (SaveOAIAction)o;
	    return dir.equals(a.dir) && suffix.equals(a.suffix);
	}
	return false;
    }

    @Override
    public Action clone() {
	// This is a shallow copy, resulting in multiple references to
	// a single OutputDirectory, which is as intended.
	return new SaveOAIAction(this);
    }
}
