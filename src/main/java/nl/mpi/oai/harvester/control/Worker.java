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

import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.harvesting.Harvesting;
import nl.mpi.oai.harvester.harvesting.IdentifierListHarvesting;
import nl.mpi.oai.harvester.harvesting.PrefixHarvesting;
import nl.mpi.oai.harvester.harvesting.RecordListHarvesting;
import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.metadata.Provider;
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
    private boolean getPrefixesScenario(ActionSequence actions){
        
        Harvesting p = new PrefixHarvesting(provider, actions);
        
        if (!p.request() || !p.processResponse()) {
            // something went wrong, or no prefixes for this endpoint
            return false;
        } else {
            // received response 
            if (p.fullyParsed()) {
                // no matches
                logger.info("No matching prefixes for format "
                        + actions.getInputFormat());
                return false;
            }
            // get the prefixes
            for (;;){
                if (p.fullyParsed()) break;
                String prefix = (String) p.parseResponse();
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

        // get the prefixes 
        if (!getPrefixesScenario(actions)) {
            return false;
        }
        
        Harvesting p = new IdentifierListHarvesting(provider, prefixes);

        for (;;) {// request a list of identifier and prefix pairs
            if (!p.request() || !p.processResponse()) {
                // something went wrong, no identifiers for this endpoint
                return false;
            } else {
                // received response 
                if (!p.requestMore()) {
                    // finished requesting
                    break;
                }
            }
        }

        /* Iterate over the list of pairs, for each pair, get the record it
           identifies.
         */
        for (;;) {
            if (p.fullyParsed()) {
                break;
            }
            Metadata record = (Metadata) p.parseResponse();

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

        // check if the endpoint supports the formats specified with the actions
        if (!getPrefixesScenario(actions)) {
            return false;
        }

        /* Create the protocol elements for this scenario. Pass the indication
           whether or not to save the response to the protocol. */

        Harvesting p = new RecordListHarvesting(provider, prefixes);

        Integer n = 0;

        for (; ; ) {
            if (!p.request() || !p.processResponse()) {
                // something went wrong with the request, try the next prefix
                break;
            } else {

                if (actions.containsSaveResponse()) {
                    /* Saving the response in the list record scenario means:
                       to save a list of records enclosed in an envelope. */

                    Document response = p.getResponse();

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
                        if (p.fullyParsed()) {
                            break;
                        }
                        Metadata record = (Metadata) p.parseResponse();

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
                if (!p.requestMore()) {
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
        for (ActionSequence actionSequence : actionSequences) {

            /* kj: static providers pass here

               Currently, non-static scenarios will be applied to them. Tying
               the harvesting modes to them, at configuration level fixes this.

               Also, for static harvesting both the list scenarios are one and
               the same. Chosen to implement static harvesting in the form of the
               list identifiers scenario. Move the scenario to the provider level
               and force 'ListIdentifiers' while configuring.

             */

            // break after an action sequence has completed successfully

            switch (scenario) {

                // note: the GetPrefixes scenario cannot be invoked by configuration

                case "ListIdentifiers": {
                    if (listIdentifiersScenario(actionSequence)) {
                        // success: done
                        done = true;
                        break;
                    }
                }

                case "ListRecords": {
                    if (listRecordsScenario(actionSequence)) {
                        // success: done
                        done = true;
                        break;
                    }
                }

                default: {
                    if (listRecordsScenario(actionSequence)) {
                        // success: done
                        done = true;
                        break;
                    }
                }
            }

            if (done) break;
        }
        logger.info("Processing finished for " + provider);
        semaphore.release();
    }
}
