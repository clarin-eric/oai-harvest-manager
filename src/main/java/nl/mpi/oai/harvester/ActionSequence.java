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

import org.apache.log4j.Logger;

/**
 * This class represents a sequence (or pipeline) of actions on metadata,
 * comprising an input format specification followed by a list of actions
 * to be performed sequentially.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class ActionSequence {
    private static final Logger logger = Logger.getLogger(ActionSequence.class);

    /**
     * The input format that must be available for this sequence
     * to be applicable.
     */
    private final MetadataFormat inputFormat;

    /** The actions, in order. */
    private final Action[] actions;

    /**
     * Create a new action sequence.
     * 
     * @param inputFormat acceptable format for harvesting the source
     * @param actions sequence of actions to take, in order
     */
    public ActionSequence(MetadataFormat inputFormat, Action[] actions) {
	this.inputFormat = inputFormat;
	this.actions = actions;
    }

    /**
     * Get the input format used in this sequence.
     */
    public MetadataFormat getInputFormat() {
	return inputFormat;
    }

    /**
     * Perform the set of actions on a single metadata record.
     *
     * This method does not return a value. The results of the actions should
     * be accessed using other actions within the sequence.
     * 
     * @param record metadata record
     */
    public void runActions(MetadataRecord record) {
	for (Action act : actions) {
	    if (!act.perform(record)) {
		logger.error("Action " + act + " failed, terminating sequence");
		return;
	    }
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder("read input (");
	sb.append(inputFormat);
	sb.append(")");
	if (actions != null) {
	    for (Action act : actions) {
		sb.append(" --> ").append(act);
	    }
	}
	return sb.toString();
    }
}
