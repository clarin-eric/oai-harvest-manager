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
 * - definition of the harvesting cycle
 *
 * - general characteristics of the harvesting cycle
 *   * mode of harvesting
 *   method for getting it
 *   no method for setting it, outside the governance of the cycle
 *   depending on the implementation, could be manually
 *   * date used for incremental harvesting
 *   same thing here

 * - adapter class implementing the interface could bridge the gap from XML
 *   could mediate between the code generated on the basis of the XSD
 *   together with endpoint states the XML file presents an overview of
 *   harvesting
 *
 * Note: for a description of the role of adapter classes, please refer to the
 * description in the Endpoint interface.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Harvesting {

    /**
     * Mode of harvesting
     */
    public enum Mode {

        /**
         * In normal mode harvesting is, in principle, incremental. However,
         * it will effectively only be so if the endpoint it would apply to
         * would allow for it.
         */
        normal,
        
        /**
         * Harvest those endpoint that gave rise to errors.
         */
        retry, 
        
        /**
         * Harvest records that were added to the endpoint after a specific date.
         */
        refresh
    }

    /**
     * <br> Return the mode in which harvesting is to be carried out
     *
     * @return the harvesting mode
     */
    public abstract Harvesting.Mode HarvestMode();

    /**
     * <br> Returns the date to use when refreshing <br<br>
     * 
     * Only records added to the endpoint after this data will be harvested. An 
     * epoch zero date on return means that there was no previous harvesting  
     * attempt.
     *
     * @return the date
     */
    public abstract String HarvestFromDate();

    /**
     * <br> Get the harvesting scenario <br><br>
     *
     * @return the scenario
     */
    public abstract String getScenario ();
}
