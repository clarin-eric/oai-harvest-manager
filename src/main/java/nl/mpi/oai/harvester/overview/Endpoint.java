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
 * For harvesting, the endpoint data resides an an XML file. An endpoint
 * element contains data that you can interpret as the state of the endpoint
 * in a repeating harvesting cycle. <br><br>
 *
 * kj: XML should serve as an example
 *
 * kj: default values need to be defined in the HarvestingOverview class
 *
 * kj: explain that what 'harvesting' means depends on the mode of the cycle
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
     * <br> Get the endpoint URI <br><br>
     *
     * @return endpoint URI
     */
    public abstract String getURI ();

    /**
     * <br> Set the endpoint URI <br><br>
     *
     * The URI by which the harvesting cycle will try to connect to the
     * endpoint. Setting the value of the URI only makes sense in case the
     * cycle was given the URI of an endpoint not known to this interface. In
     * that case, a new endpoint,
     *
     * @param URI the URI of the endpoint
     */
    public abstract void setURI (String URI);

    /**
     * <br> Find out if an endpoint is blocked <br><br>
     *
     * If blocked, the cycle will not try to harvest the endpoint, regardless
     * of any other specification.
     *
     * Note: there is no method for blocking the endpoint. The decision to
     * block an endpoint is not part of the harvesting lifecycle itself. It
     * could be taken, for example, in case the endpoint fails to perform
     * correctly.
     *
     * @return true if the endpoint should be skipped, false otherwise
     */
    public abstract boolean blocked ();

    /**
     * <br> Check if the cycle can harvest the endpoint incrementally<br><br>
     *
     * In case cycle can harvest the endpoint incrementally, the date of the
     * previous successful harvest determines which records it will be request
     * from the endpoint.
     *
     * Note: there is no method for setting the value indicating whether or
     * not incremental harvesting is allowed. The value needs to be specified
     * elsewhere, for example in an XML file managed by a class implementing
     * this interface.
     *
     * kj: default false
     *
     * @return true if incremental harvesting is allowed, false otherwise
     */
    public abstract boolean allowIncrementalHarvest ();

    /**
     * <br> Check if the cycle can retry harvesting the endpoint <br><br>
     *
     * Only if the cycle itself is in retry mode, it can effectively retry
     * harvesting the endpoint.
     *
     * Note: like getURI, blocked and allowIncremental harvest, the interface
     * does not provide a method that can set the value of the retry attribute.
     *
     * @return true is a retry is allowed, false otherwise
     */
    public abstract boolean retry();
    
    /**
     * <br> Return the date for incrementally harvesting <br><br>
     *
     * The date used in the most recent and successful incremental harvest
     * attempt. The date indicates that all records provided by the endpoint
     * and carrying a date up to and including the date returned, have been
     * harvested without a problem.
     *
     * A harvesting cycle needs to set the date by invoking the doneHarvesting
     * method.
     *
     * @return the date of most recent successful harvest of the endpoint
     */
    public abstract String getRecentHarvestDate();

    /**
     * <br> Indicate success or failure <br><br>
     * 
     * In case of success, this method sets the date for incremental harvesting
     * to the current date. Otherwise it will not modify the date. A cycle in
     * retry mode will consider this endpoint for harvesting again.
     *
     * @param done true in case of success, false otherwise
     */
    public abstract void doneHarvesting(Boolean done);

    /**
     * <br> Get the group
     *
     * @return the group the endpoint belongs to
     *
     */
    public abstract String getGroup ();

    /**
     * <br> Set the group
     *
     * @param group the group the endpoint belongs to
     */
    public abstract void setGroup (String group);

    /**
     * <br> Get the record count
     *
     * @return the number of records harvested
     */
    public int getCount ();

    /**
     * <br> Set record count
     *
     * @param count the number of records harvested
     */
    public void setCount (int count);
}
