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
 * <br> Access to an OAI endpoint <br><br>
 *
 * This interface presents an endpoint to the harvesting cycle. By using the
 * methods defined here, the harvesting cycle can store the harvesting state
 * of the endpoint.
 *
 * The harvesting cycle can access some endpoint attributes directly through
 * method parameters and return values. This applies for example to the URI,
 * group, and record count.
 *
 * Access to other fields is indirect, like for example the date of the most
 * recent harvest. While the interface does provide a method for getting the
 * attribute, the cycle cannot set it by means of providing a parameter. The
 * doneHarvesting method for example will determine and set it by itself.
 *
 * A third category of attribute falls outside the control of the cycle. The
 * interface does not provide for setting the value of the attribute, not even
 * indirectly. The scenario attribute for example, belongs to this category.
 *
 * A typical implementation of the Endpoint interface would be an adapter class
 * that reads from and writes to an XML file.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Endpoint {

    /**
     * <br> Get the endpoint URI <br><br>
     *
     * The URI by which the harvesting cycle will try to connect to the
     * endpoint.
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
     * <br> Find out if an endpoint is blocked <br><br>
     *
     * If blocked, the cycle will not try to harvest the endpoint, regardless
     * of any other specification. <br><br>
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
     * <br> Check if the cycle can retry harvesting the endpoint <br><br>
     *
     * Only if the cycle itself is in retry mode, it can effectively retry
     * harvesting the endpoint. <br><br>
     *
     * Note: like getURI, blocked and allowIncremental harvest, the interface
     * does not provide a method that can set the value of the retry attribute.
     *
     * @return true is a retry is allowed, false otherwise
     */
    public abstract boolean retry();

    /**
     * <br> Check if the cycle can harvest the endpoint incrementally<br><br>
     *
     * In case cycle can harvest the endpoint incrementally, the date of the
     * previous successful harvest determines which records it will be request
     * from the endpoint. <br><br>
     *
     * Note: there is no method for setting the value indicating whether or
     * not incremental harvesting is allowed. The value needs to be specified
     * elsewhere, for example in an XML file managed by a class implementing
     * this interface.
     *
     * @return true if incremental harvesting is allowed, false otherwise
     */
    public abstract boolean allowIncrementalHarvest ();

    /**
     * <br> Get the scenario for harvesting <br><br>
     *
     * A cycle applies a specific scenario for harvesting records. It can,
     * for example, first harvest a list of identifiers to metadata elements,
     * and after that, harvest the records one by one. Alternatively, it can
     * harvest the records directly.
     *
     * Note: there is no method for setting the scenario. The value needs to
     * be specified independently from the harvesting cycle.
     *
     * @return the scenario
     */
    public abstract Cycle.Scenario getScenario ();

    /**
     * <br> Return the date for incrementally harvesting <br><br>
     *
     * The date of the most recent and successful harvest cycle. A subsequent
     * cycle will use this date when harvesting the endpoint incrementally. <br><br>
     *
     * Note: a harvesting cycle needs to set the date by invoking the
     * doneHarvesting method.
     *
     * @return the date of most recent successful harvest of the endpoint
     */
    public abstract String getRecentHarvestDate();

    /**
     * <br> Indicate success or failure <br><br>
     *
     * If done equals true, the method sets the date of the most recent and
     * successful harvest to the current date. If false, it will only set the
     * most recent attempt.
     *
     * @param done true in case of success, false otherwise
     */
    public abstract void doneHarvesting(Boolean done);

    /**
     * <br> Get the record count
     *
     * @return the number of records harvested
     */
    public abstract long getCount ();

    /**
     * <br> Set record count
     *
     * @param count the number of records harvested
     */
    public abstract void setCount (long count);

    /**
     * <br> Get the record increment <br><br>
     *
     * The number of records incrementally harvested from the endpoint in the
     * most recent cycle. If the endpoint was not incrementally harvested, the
     * increment will be zero.
     *
     * @return the increment
     */
    public abstract long getIncrement ();

    /**
     * <br> Set the record increment <br><br>
     *
     * The number of records incrementally harvested from the endpoint in the
     * most recent cycle. If the endpoint was not incrementally harvested, the
     * increment will be zero.
     *
     * @param increment the increment
     */
    public abstract void setIncrement (long increment);
}
