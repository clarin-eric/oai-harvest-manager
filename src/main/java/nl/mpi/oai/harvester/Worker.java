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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class represents a single processing thread in the harvesting actions
 * workflow. In practice one worker takes care of one provider.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class Worker implements Runnable {
    
    private static final Logger logger = Logger.getLogger(Worker.class);
    
    /** A standard semaphore is used to track the number of running threads. */
    private static Semaphore semaphore;

    /** The provider this worker deals with. */
    private final Provider provider;

    /** List of sequences to be applied to the harvested Metadata. */
    private final List<ActionSequence> sequences;

    /* If direct, obtain records by following the list records scenario,
       otherwise, obtain identifiers first, and based on those, obtain the
       records.
     */
    private final boolean direct;

    /**
     * Set the maximum number of concurrent worker threads.
     * 
     * @param num number of running threads that may not be exceeded
     */
    public static void setConcurrentLimit(int num) {
	semaphore = new Semaphore(num);
    }

    /**
     * Create a Worker object.
     * 
     * @param provider OAI-PMH provider that this thread will harvest
     * @param sequences list of actions to take on harvested Metadata
     * @param direct kj: need to review this parameter
     */
    public Worker(Provider provider, List<ActionSequence> sequences,
                  boolean direct) {
	this.provider  = provider;
	this.sequences = sequences;
    this.direct    = direct;
    }
    
    // kj: this needs to be moved
    List<String> prefixes = new ArrayList<>();
    
    /**
     * Get the list of Metadata prefixes supported by the endpoint<br><br>
     *
     * The list created is based on the format specified in the configuration.
     *
     * @param actions
     * @return false on parser or input output error
     */
    public boolean getPrefixesScenario(ActionSequence actions){
        
        Protocol p = new ListPrefixesProtocol (provider, actions);
        
        if (!p.request() || !p.processResponse()) {
            // something went wrong, no prefixes for this endpoint
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
        // endpoint responded with at least one prefix
        
        return true;
    }
    
    /**
     * Get Metadata records indirectly, that is by first obtaining a list of
     * identifiers pointing to them.<br><br>
     *
     * @param actions the sequence of actions
     * @return false on parser or input output error
     */
    public boolean listIdentifiersScenario(ActionSequence actions) {

        // get the prefixes 
        if (!getPrefixesScenario(actions)) {
            return false;
        }
        
        Protocol p = new ListIdentifiersProtocol(provider, prefixes);

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

        // the list of pairs, get the records they point to
        for (;;) {
            if (p.fullyParsed()) {
                break;
            }
            MetadataRecord record = (MetadataRecord) p.parseResponse();

            if (record == null) {
                // something went wrong, skip the record
            } else {
                // transform the record, no skipping
                actions.runActions(record, false);
            }
        }
        
        return true;
    }
    
    /**
     * Get Metadata records directly, that is without first obtaining a list of
     * identifiers pointing to them.<br><br>
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

        Protocol p = new ListRecordsProtocol(provider, prefixes);

        for (; ; ) {
            if (!p.request() || !p.processResponse()) {
                // something went wrong with the request, try the next prefix
                break;
            } else {

                if (actions.containsSaveResponse()) {
                    /* Saving the response in the list record scenario means:
                       to save the

                    Document response = p.getResponse();

                    // generate id: sequence number

                    MetadataRecord record = new MetadataRecord(id, response, this.provider);

                    */
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
                        MetadataRecord record = (MetadataRecord) p.parseResponse();

                        if (record == null) {
                        /* Something went wrong or the record has already been
                           released, either way: skip it
                         */
                        } else {
                            /* Indicate that response saving and skipping do not
                            apply to the record */

                            actions.runActions(record, true);
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
     * Start this worker thread. This method will block for as long as
     * necessary until a thread can be started without violating the limit.
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
        
        logger.info("Processing provider " + provider);
        for (ActionSequence as : sequences) {
            
            // Break the inner loop after the first successful completion
            // of an action sequence.
            
            if (direct) {
                if (listRecordsScenario(as)) {
                    break;
                }
            } else {
                // if (provider.performActions(as)) {
                if (listIdentifiersScenario(as)) {
                    break;
                }
            }
        }
	logger.info("Processing finished for " + provider);
	semaphore.release();
    }
}
