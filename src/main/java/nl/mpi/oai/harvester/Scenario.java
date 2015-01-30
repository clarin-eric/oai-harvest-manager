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

package nl.mpi.oai.harvester;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * kj: this will work / need to modify main
 *
 * We can now start the worker with a scenario and sequences parameter. In
 * the worker the sequences are then combined with the scenarios. Alternatively
 * the worker can be passed one single action sequence as well.
 *
 * Note: it would be nice to set the scenario in the configuration. However,
 *       the configuration still contains multiple providers, and the scenario
 *       may later come to depend on a provider.
 *
 * After that: remove the scenario code from worker
 *
 * Note: the actions are passed as a parameter to each individual scenario,
 *       unlike the provider which is accessed as a global variable. It
 *       would be better to choose one way of passing the information instead
 *       of two different ways.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 *
 */
public enum Scenario {

    GETPREFIXES     ("ListPrefixes"),
    LISTIDENTIFIERS ("ListIdentifiers"),
    LISTRECORDS     ("ListRecords");

    private static final Logger logger = Logger.getLogger(Scenario.class);

    // endpoint to request metadata from
    private Provider provider;

    // actions to perform on metadata obtained
    private ActionSequence actions;

    // list of prefixes provided by the endpoint
    List<String> prefixes = new ArrayList<>();

    // actions performed successfully or not
    private boolean done = false;

    /**
     * Set the endpoint
     *
     * @param provider the endpoint
     */
    public void setProvider (Provider provider) {
        this.provider = provider;
    }

    /**
     * Set the actions to be performed
     *
     * @param actions the actions
     */
    public void setAction (ActionSequence actions) {
        this.actions = actions;
    }

    /**
     * Check if the scenario was completed successfully
     *
     * @return true if success, false otherwise
     */
    public boolean done () {
        return done;
    }

    /**
     * Get the list of metadata prefixes supported by the endpoint<br><br>
     *
     * The list created is based on the format specified in the configuration.
     *
     * @param actions
     * @return false on parser or input output error
     */
    private boolean getPrefixes (ActionSequence actions){

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
     * Get metadata records indirectly, that is by first obtaining a list of
     * identifiers pointing to them.<br><br>
     *
     * @param actions the sequence of actions
     * @return false on parser or input output error
     */
    public boolean listIdentifiersScenario(ActionSequence actions) {

        // get the prefixes
        Scenario s = Scenario.GETPREFIXES;
        s.setAction(actions);
        s.setProvider(provider);
        if (! s.done()) {
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
            Metadata record = (Metadata) p.parseResponse();

            if (record == null) {
                // something went wrong, skip the record
            } else {
                // transform the record, no skipping
                actions.runActions(record);
            }
        }

        return true;
    }

    /**
     * Get metadata records directly, that is without first obtaining a list of
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
        Scenario s = Scenario.GETPREFIXES;
        s.setAction(actions);
        s.setProvider(provider);
        if (! s.done()) {
            return false;
        }

        /* Create the protocol elements for this scenario. Pass the indication
           whether or not to save the response to the protocol. */

        Protocol p = new ListRecordsProtocol(provider, prefixes);

        Integer id = 0;

        for (; ; ) {
            if (!p.request() || !p.processResponse()) {
                // something went wrong with the request, try the next prefix
                break;
            } else {

                if (actions.containsSaveResponse()) {
                    /* Saving the response in the list record scenario means:
                       to save a list of records enclosed in an envelope. */

                    Document response = p.getResponse();

                    // generate id: provider name and sequence number

                    Metadata records = new Metadata(
                            provider.getName() + "-" + id.toString(),
                            response, this.provider, true, true);

                    id++;

                    if (records == null) {
                        /* Something went wrong or the record has already been
                           released, either way: skip it
                         */
                    } else {
                            /* Indicate that response saving and skipping do not
                            apply to the record */

                        actions.runActions(records);
                    }
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
                            /* Indicate that response saving and skipping do not
                            apply to the record */

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
     * Associate actions with each scenario
     *
     * @param s
     */
    Scenario(String s) {

        switch (s){
            case "ListPrefixes":{
                done = getPrefixes(actions);
            }
            case "ListIdentifiers": {
                done = listIdentifiersScenario(actions);

            }
            case "ListRecords": {
                done = listRecordsScenario(actions);
            }
        }

    }
}
