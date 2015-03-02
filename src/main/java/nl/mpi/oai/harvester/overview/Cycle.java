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
 * <br> Access to general harvest cycle attributes <br><br>
 *
 * kj: refactor
 * Cycle, CycleAdapter -> Restriction, RestrictionAdapter
 *
 * A harvest cycle visits OAI endpoints with the intention to obtain metadata
 * records. The cycle can use the cycle and the endpoint interface to query the
 * general harvest cycle characteristics and state of the endpoint in order to
 * decide whether or not to harvest and to determine OAI verb and parameters.
 *
 * General characteristics of cycle include the mode of harvesting, the date
 * specified in a incremental or selective harvesting request, and the intended
 * harvest scenario.
 *
 * <table>
 * <td>
 * mode     <br>
 * date     <br>
 * scenario <br><br>
 * </td>
 * <td>
 * defaults to 'normal' <br>
 * defaults to '1970-01-01' <br>
 * defaults to 'ListRecords' <br><br>
 * </td>
 * </table>
 *
 * These are the elements conveyed through the Cycle interface. The elements
 * listed are optional, the methods use default values, and reflect these
 * through the interface.
 *
 * While the interface specifies methods for getting the attributes, it does
 * not specify methods for setting them. The general cycle attributes fall
 * outside the governance of the harvesting cycle. This means that a class that
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
     * The harvest scenario the cycle needs to apply <br> <br><br><br>
     *
     * A harvest scenario is a particular implementation and application of the
     * methods declared in the harvesting interface. A class implements a method
     * by invoking methods that establish primitives in the OAI protocol. <br><br>
     *
     * Applying a particular scenario, the cycle can, for example, first harvest
     * the identifiers of metadata records and request the identified records,
     * or alternatively, harvest the records directly.
     */
    public enum Scenario {

        /**
         * Query the metadata prefixes the endpoint supports. The cycle uses
         * metadata formats it associates with the endpoint to identify the
         * prefixes.
         */
        ListPrefixes,

        /**
         * In this scenario, by selecting a prefix, the cycle first obtains a
         * list of identifiers of metadata elements the endpoint provides. Next,
         * it gets the records the list identifies.
         */
        ListIdentifiers,

        /**
         * In this scenario, instead of getting a list of identifiers first,
         * the cycle can also select a prefix and query the endpoint for
         * matching records without first obtaining a list of their identifiers.
         */
        ListRecords
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
     * Only records added to the endpoint after this data will be harvested. By
     * returning the epoch zero date the method indicated that there was no
     * previous harvesting attempt.
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
    public abstract Scenario getScenario ();
}
