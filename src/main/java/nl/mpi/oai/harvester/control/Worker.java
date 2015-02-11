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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import nl.mpi.oai.harvester.StaticProvider;
import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.harvesting.*;
import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.Provider;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class represents a single processing thread in the harvesting actions
 * workflow. In practice one worker takes care of one provider. The worker
 * applies a scenario for harvesting: first get record identifiers, after that
 * get the records individually. Alternatively, in a second scenario, it gets
 * multiple records per OAI request directly.
 *
 * @author Lari Lampen (MPI-PL), extensions by Kees Jan van de Looij (MPI-PL).
 */
class Worker implements Runnable {
    
    private static final Logger logger = Logger.getLogger(Worker.class);
    
    /** A standard semaphore is used to track the number of running threads. */
    private static Semaphore semaphore;

    /** The provider this worker deals with. */
    private final Provider provider;

    /** List of actionSequences to be applied to the harvested metadata. */
    private final List<ActionSequence> actionSequences;

    /* Harvesting scenario to be applied. ListIdentifiers: first, based on
       endpoint data and prefix, get a list of identifiers, and after that
       retrieve each record in the list individually. ListRecords: skip the
       list, retrieve multiple records per request.
     */
    private final String scenario;

    // list of prefixes provided by the endpoint
    private final List<String> prefixes = new ArrayList<>();

    /**
     * Set the maximum number of concurrent worker threads.
     * 
     * @param num number of running threads that may not be exceeded
     */
    public static void setConcurrentLimit(int num) {
	semaphore = new Semaphore(num);
    }

    /**
     * Associate a provider and action actionSequences with a scenario
     *
     * @param provider OAI-PMH provider that this thread will harvest
     * @param actionSequences list of actions to take on harvested metadata
     * @param scenario the scenario to be applied
     *
     */
    public Worker(Provider provider, List<ActionSequence> actionSequences,
                  String scenario) {
	this.provider  = provider;
	this.actionSequences = actionSequences;
    this.scenario  = scenario;
    }

    /**
     * <br>Get the list of metadata prefixes supported by the endpoint<br><br>
     *
     * The list created is based on the format specified in the configuration.
     *
     * @param actions sequence of actions that should be performed
     * @return false on parser or input output error
     */
    private boolean prefixesScenario(Provider provider, ActionSequence actions){
        
        if (!provider.prefixHarvesting.request() ||
            !provider.prefixHarvesting.processResponse()) {
            // something went wrong, or no prefixes for this endpoint
            return false;
        } else {
            // received response 
            if (provider.prefixHarvesting.fullyParsed()) {
                // no matches
                logger.info("No matching prefixes for format "
                        + actions.getInputFormat());
                return false;
            }
            // get the prefixes
            for (;;){
                if (provider.prefixHarvesting.fullyParsed()) break;
                String prefix = (String) provider.prefixHarvesting.parseResponse();
                if (prefix != null) {
                    prefixes.add(prefix);
                }
            }
        }

        /* If there are no matches, return false. In this case the
           action sequence needs to be terminated. A succeeding action
           sequence could then provide a match.
         */
        if (prefixes.size() == 0) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Get metadata records indirectly, that is by first obtaining a list of
     * identifiers pointing to them <br><br>
     *
     * @param actions the sequence of actions
     * @return false on parser or input output error
     */
    private boolean listIdentifiersScenario(ActionSequence actions) {

        for (;;) {// request a list of identifier and prefix pairs
            if (!provider.metadataHarvesting.request() ||
                !provider.metadataHarvesting.processResponse()) {
                // something went wrong, no identifiers for this endpoint
                return false;
            } else {
                // received response 
                if (!provider.metadataHarvesting.requestMore()) {
                    // finished requesting
                    break;
                }
            }
        }

        /* Iterate over the list of pairs, for each pair, get the record it
           identifies.
         */
        for (;;) {
            if (provider.metadataHarvesting.fullyParsed()) {
                break;
            }
            Metadata record = (Metadata)
                    provider.metadataHarvesting.parseResponse();

            if (record == null) {
                // something went wrong, skip the record
            } else {
                // apply the action sequence to the record
                actions.runActions(record);
            }
        }
        
        return true;
    }
    
    /**
     * <br>Get metadata records directly, that is without first obtaining a list
     * of identifiers pointing to them <br><br>
     *
     * In this scenario, a save action specified before a strip action is
     * interpreted to apply the the response of the GetRecords verb. Also, the
     * presence of a strip action in the sequence, is interpreted to apply to
     * the response also. Since the sequence of actions will be applied to an
     * individual record, in the sequence both will be disabled.

     * @param actions the sequence of actions
     * @return false on parser or input output error
     */
    private boolean listRecordsScenario(ActionSequence actions) {

        Integer n = 0;

        for (; ; ) {
            if (!provider.metadataHarvesting.request() ||
                !provider.metadataHarvesting.processResponse()) {
                // something went wrong with the request, try the next prefix
                break;
            } else {

                if (actions.containsSaveResponse()) {
                    /* Saving the response in the list record scenario means:
                       to save a list of records enclosed in an envelope. */

                    Document response = provider.metadataHarvesting.getResponse();

                    // generate id: sequence number, provide leading zeros

                    String id;
                    id = String.format("%07d", n);

                    Metadata records = new Metadata(
                            provider.getName() + "-" + id,
                            response, this.provider, true, true);

                    n++;

                    // apply the action sequence to the record
                    actions.runActions(records);
                }

                if (actions.containsStripResponse()) {

                    /* Stripping in the list record scenario means: processing
                       each and every record in the response. Skip to the next
                       request is no strip action is demanded.
                     */
                    for (; ; ) {
                        if (provider.metadataHarvesting.fullyParsed()) {
                            break;
                        }
                        Metadata record = (Metadata)
                                provider.metadataHarvesting.parseResponse();

                        if (record == null) {
                        /* Something went wrong or the record has already been
                           released, either way: skip it
                         */
                        } else {
                            // apply the action sequence to the record
                            actions.runActions(record);
                        }
                    }
                }

                // check if in principle another response would be available
                if (!provider.metadataHarvesting.requestMore()) {
                    break;
                }
            }
        }

        return true;
    }
    
    /**
     * <br>Start this worker thread <br><br>
     *
     * This method will block for as long as necessary until a thread can be
     * started without violating the limit.
     */
    public void startWorker() {
	for (;;) {
	    try {
		semaphore.acquire();
		break;
	    } catch (InterruptedException e) { }
	}
	Thread t = new Thread(this);
	t.start();
    }

    @Override
    public void run() {
        provider.init();

        boolean done = false;

        logger.info("Processing provider " + provider);
        for (final ActionSequence actionSequence : actionSequences) {

            // check if harvesting of static content applies

            if (provider instanceof StaticProvider) {
                provider.prefixHarvesting = new StaticPrefixHarvesting (
                        (StaticProvider)provider, actionSequence);

            } else {
                provider.prefixHarvesting= new PrefixHarvesting(provider,
                        actionSequence);
            }

            // check if the endpoint supports the formats specified with the actions

            if (prefixesScenario(provider, actionSequence)) {

                if (provider instanceof StaticProvider){
                    provider.metadataHarvesting = new StaticRecordListHarvesting(
                            (StaticProvider) provider, prefixes);
                } else {
                    provider.metadataHarvesting = new RecordListHarvesting(
                            provider, prefixes);
                }

                if (scenario.equals("ListIdentifiers")) {
                    done = listIdentifiersScenario(actionSequence);
                } else {
                    done = listRecordsScenario(actionSequence);
                }
            }

            // break after an action sequence has completed successfully

            if (done) break;

        }
        logger.info("Processing finished for " + provider);
        semaphore.release();
    }
}
