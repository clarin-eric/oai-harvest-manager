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

import java.util.*;

import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.metadata.MetadataFormat;
import nl.mpi.oai.harvester.control.ResourcePool;
import org.apache.log4j.Logger;

/**
 * This class represents a sequence (or pipeline) of actions on metadata,
 * comprising an input format specification followed by a list of actions
 * to be performed sequentially.
 * 
 * @author Lari Lampen (MPI-PL), Kees Jan van de Looij (MPI-PL)
 */
public class ActionSequence {
    private static final Logger logger = Logger.getLogger(ActionSequence.class);

    /**
     * All action sequences share this set of resource pools and reuse its
     * contents where possible. So, for example, there should only be one
     * resource pool of strip actions. Every action sequence that contains a
     * strip action then holds a reference to that pool.
     */
    private static final Map<Action, ResourcePool<Action>> pooledActions =
			new HashMap<>();

    /**
     * The input format that must be available for this sequence
     * to be applicable.
     */
    private final MetadataFormat inputFormat;

    /* The actions, in order. */
    private final List<ResourcePool<Action>> actions;

    /* Remember if the sequence contains actions that are intended to save or
    strip the envelope */
    private final boolean save;
    private final boolean strip;

    /**
     * Create a new action sequence.
     * 
     * @param inputFormat acceptable format for harvesting the source
     * @param theActions sequence of actions to take, in order
     * @param resourcePoolSize the number of resources in the pool
     * @param save iff true, the sequence is intended to save the envelope
     * @param strip iff true, the sequence is intended to strip the envelope
     *
     */
    public ActionSequence(MetadataFormat inputFormat, Action[] theActions,
	    int resourcePoolSize, boolean save, boolean strip) {
	this.inputFormat = inputFormat;
	this.save        = save;
	this.strip       = strip;

	actions = new ArrayList<>();

	for (Action act : theActions) {
	    actions.add(getPool(act, resourcePoolSize));
	}
    }

    /**
     * Get resource pool for this action (either previously initialized or
     * created on this call).
     */
    private synchronized static ResourcePool<Action> getPool(Action action,
	    int size) {
    	if (!pooledActions.containsKey(action)) {
	    Action[] acts = new Action[size];
	    for (int i = 0; i < size; i++) {
		acts[i] = action.clone();
	    }
	    ResourcePool<Action> pool = new ResourcePool<>(acts);
	    pooledActions.put(action, pool);
	}
	return pooledActions.get(action);
    }

	/**
	 * Get the input format used in this sequence <br><br>
	 *
	 * @return the input format
	 */
    public MetadataFormat getInputFormat() {
	return inputFormat;
    }

    /**
     * Perform the actions specified in the configuration<br><br>
     *
     * Note. Not every action might be applicable to metadata at the various
     * stages of a scenario. For example, when listing records without using
     * identifiers, an OAI envelope will contain multiple records. While it
     * makes sense to save the envelope, it does not make sense to try to strip
     * individual metadata records. This means that performing an action on
     * metadata depends on <br><br>
     *
     * - an envelope being there or not, and <br><br>
     * - single or multiple records being present. <br><br>
     *
     * Note. This method does not return a value. The results of the actions
     * should be accessed using other actions within the sequence.
     *
     * @param metadata a single metadata record or a list of metadata records
     */
    public void runActions(Metadata metadata) {

	// keep track of whether or not the action is the first in the sequence
	boolean firstAction = true;

        for (ResourcePool<Action> actPool : actions) {
                // claim an action in the pool
                Action action = actPool.get();

                // assume the action cannot be performed, investigate the opposite
                boolean performAction = false;
                
                // reason why the action cannot be performed
                String reason = "an unspecified reason";

                if (action instanceof SaveOAIAction) {
                        performAction = true;
                } else if (action instanceof SaveAction) {

                        if (firstAction) {

                                if (metadata.isEnvelope()) {
                                        // one record, or a list, save it
                                        performAction = true;
                                } else
                                    reason = "the input has no envelope";

                        } else {
                                // no envelope
                                if (metadata.isList()) {
                                        // do not save a list that is not in an envelope
                                        reason = "the input is a list";
                                } else {
                                        // unpacked metadata, save it
                                        performAction = true;
                                }
                        }

                } else {
                        if (action instanceof StripAction) {

                                if (metadata.isEnvelope() && ! metadata.isList()) {
                                        // single record in envelope, strip it
                                        performAction = true;
                                } else
                                    reason = "the input is a list or not an envelope";

                        } else {
                                // transform action

//                                if (metadata.isEnvelope() || metadata.isList()) {
//                                        // transformation not possible
//                                        reason = "the input is a list or an envelope";
                                //if (metadata.isList()) {
                                //        // transformation not possible
                                //        reason = "the input is a list";
                                //} else {
                                        performAction = true;
                                //}
                        }
                }

                if (performAction) {
                        boolean done = action.perform(metadata);

                        actPool.release(action);
                        if (!done) {
                                logger.error("Action " + action + " failed, terminating" +
                                                " sequence");
                                return;
                        } else
                                logger.debug("Action " + action + " was performed");
                            
                } else
                        logger.warn("Action " + action + " isn't performed, because "+reason+"!");

                actPool.release(action);
                
                if (firstAction)
                    firstAction = false;
        }
    }

    /**
     * Check if the response should be saved
     *
     * @return true iff the sequence contains an action intended to save the response
     */
    public boolean containsSaveResponse() {
            return save;
    }

    /**
     * Check if the response should be stripped
     *
     * @return true iff the sequence contains an action intended to strip the response
     */
    public boolean containsStripResponse(){
            return strip;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder("read input (");
	sb.append(inputFormat);
	sb.append(")");
	if (actions != null) {
	    for (ResourcePool<Action> actPool : actions) {
		Action act = actPool.get();
		sb.append(" --> ").append(act);
		actPool.release(act);
	    }
	}
	return sb.toString();
    }
}
