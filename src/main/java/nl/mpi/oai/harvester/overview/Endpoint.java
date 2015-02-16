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
 * <br> Adapter class definition of an OAI endpoint <br><br>
 *
 * For harvesting, the endpoint data resides an an XML file. An endpoint element
 * contains data that you can interpret as the state of the endpoint in a
 * repeating harvesting cycle. <br><br>
 *
 * This interface can for example be implemented by an adapter class, a class
 * mediating between the code generated on the basis of the XSD. The adapter
 * class bridges the gap from the generated types to the types would ideally
 * fit the the harvesting application. Note: the XSD file referred to resides
 * in the src/xsd directory. <br><br>
 *
 * An adapter class can also bridge differences in format, for example the
 * harvesting date presented by the generated classes and the format that the
 * application would like to work on.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Endpoint {

    /**
     * <br> Check if a retry is allowed for the endpoint <br><br>
     * 
     * Only if true and if the harvesting manager is in retry mode, the 
     * records added to the endpoint after the harvested date will be eligible 
     * for a new harvesting attempt.
     * 
     * @return true or false 
     */
    public abstract boolean retry();
    
    /**
     * <br> Check if incremental harvesting is allowed <br><br>
     * 
     * Only if true, incremental harvesting, if specified at the general level,
     * will be carried out. In this case the date the the previous successful
     * harvest determines which records will be requested from the endpoint.
     * 
     * @return true or false
     */
    public abstract boolean allowIncrementalHarvest ();

    /**
     * <br> Check if the endpoint is allowed to be harvested <br><br>
     * 
     * Only if true, there will be no harvesting for the endpoint regardless of
     * any other specification. This mode can be used to temporarily block 
     * harvesting, for example in case the endpoint fails to perform correctly.
     * 
     * @return true or false
     */
    public abstract boolean doNotHarvest ();

    /**
     * <br> Return the date for incrementally harvesting <br><br>
     * 
     * Up to this date the records were harvested before. Incremental harvesting
     * means that only those records that were added to the endpoint after this 
     * date will be harvested.
     *
     * @return the date
     */
    public abstract String GetRecentHarvestDate();

    /**
     * <br> Indicate success or failure <br><br>
     * 
     * In case of success, the date for incremental harvesting will be set to 
     * the current date. Otherwise the date will not be modified. Harvesting in 
     * retry mode will consider this endpoint for harvesting again. 
     *
     * @param done true in case of success, false otherwise
     */
    public abstract void DoneHarvesting(Boolean done);
}
