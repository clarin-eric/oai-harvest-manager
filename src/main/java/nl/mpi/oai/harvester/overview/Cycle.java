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

package nl.mpi.oai.harvester.overview;

/**
 * <br> Adapter class definition of the harvesting cycle <br><br>
 *
 * A harvesting cycle traverses OAI endpoints with the intention to obtain
 * metadata records. The cycle initiates a request for records by issuing a
 * command created from an OAI verb and parameters identifying a particular
 * list of records.
 *
 * The cycle can use the harvesting and endpoint interface to query the
 * general harvesting characteristics and state of the endpoint in order to
 * to decide whether or not to harvest and to determine OAI verb and
 * parameters.
 *
 * After the cycle has issued the command, it needs to interpret the result
 * and update the endpoint state.
 *
 * By tracking the state of th endpoint, it can obtain recent additions to it,
 * without having to harvest all the records provided once again. This mode of
 * harvesting is referred to as 'incremental harvesting' and can be regarded as
 * a form of selective harvesting as described in the definition of the OAI
 * protocol. Incremental harvesting is particularly useful when the endpoint
 * provides a large number of records.
 *
 * General characteristics of cycle include the mode of harvesting, the date
 * specified in a incremental or selective harvesting request, and the intended
 * harvesting scenario. A scenario defines the way in which the cycle will
 * apply the OAI protocol primitives. It can, for example, first harvest the
 * identifiers of  metadata records and request the identified records, or
 * alternatively, harvest the records directly.
 *
 * While the interface specifies methods for obtaining general information, it
 * does not specify methods for setting the mode of harvesting, the date used
 * for selective harvesting, and the scenario for harvesting. Attributes like
 * these fall outside the governance of the cycle. This means that a class that
 * implements the interface can leave room for manual specification.
 *
 * Note: for a description of the role of adapter classes, please refer to the
 * description in the Endpoint interface.
 *
 * Note: for more information on the OAI protocol, please refer to
 * <a href="http://www.openarchives.org/OAI/openarchivesprotocol.htm">
 *     its specification</a>.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Cycle {

    /**
     * Mode of harvesting
     */
    public enum Mode {

        /**
         * The cycle will, in normal mode, harvest incrementally. However, only
         * if the endpoint's interface allowIncrementalHarvest method indicates
         * that the endpoint currently supports harvesting in that mode. If it
         * does not, the cycle will try to harvest all the records the endpoint
         * provides.
         */
        normal,
        
        /**
         * In this mode the cycle will try to harvest those endpoints that gave
         * rise to errors once again. If the date of the most recent completed
         * attempt differs from the date of the most recent attempt, the cycle
         * assumes that harvesting the endpoint was not successful.
         */
        retry, 
        
        /**
         * When refreshing, the cycle will harvest records that were added to
         * the endpoint after a specific date. The cycle will use the general
         * date attribute returned by the getHarvestFromDate method when it
         * constructs a harvesting request. If the endpoint is not blocked, the
         * cycle will try to harvest it selectively, taking the date specified
         * into account.
         */
        refresh
    }

    /**
     * <br> Return the mode in which the cycle will in principle harvest the
     * endpoints
     *
     * @return the harvesting mode
     */
    public abstract Mode getHarvestMode();

    /**
     * <br> Returns the date to use when refreshing <br<br>
     * 
     * Only records added to the endpoint after this data will be harvested. An 
     * epoch zero date on return means that there was no previous harvesting  
     * attempt.
     *
     * @return the date
     */
    public abstract String getHarvestFromDate();

    /**
     * <br> Get the harvesting scenario <br><br>
     *
     * Note: the range of valid scenarios depends on the capabilities of the
     * current implementation of the harvesting cycle.
     *
     * @return the scenario
     */
    public abstract String getScenario ();
}
