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
 * <br> Access to endpoint attributes <br><br>
 *
 * Apart from initialising them to a default value, the following attributes
 * fall outside the governance of the harvest cycle. This leaves room for manual
 * intervention. Setting the block attribute to true, for example, will prevent
 * the cycle from harvesting an endpoint that causes trouble. <br><br>
 *
 * <table>
 * <td>
 * URI         <br>
 * group       <br>
 * block       <br>
 * retry       <br>
 * incremental <br>
 * scenario    <br>
 * </td>
 * <td>
 * cycle needs to supply it  <br>
 * cycle needs to supply it  <br>
 * defaults to false         <br>
 * defaults to false         <br>
 * defaults to false         <br>
 * defaults to 'ListRecords' <br>
 * </td>
 * </table><br>
 *
 * By using the methods defined here, the harvest cycle can track the state of
 * the endpoints.
 *
 * <table>
 * <td>
 * attempted <br>
 * harvested <br>
 * count     <br>
 * increment <br><br>
 * </td>
 * </table>
 *
 * By tracking, the cycle can obtain recent additions to an endpoint, without
 * having to harvest all the records provided by it over and over again. This
 * incremental mode of harvesting is particularly useful when the endpoint
 * provides a large number of records. <br><br>
 *
 * Note: the doneHarvesting method sets both the attempted and harvested
 * attributes. These attributes do not have a method that sets their value
 * individually. <br><br>
 *
 * A class implementing the interface should initialise the attributes. This
 * means that every individual method should, once it needs get the value of
 * an attribute that has not been defined, provide the default listed in the
 * table above. By doing this, it defines the attribute, and because of this,
 * it should record the value for later reference. <br><br>
 *
 * A typical implementation of the Endpoint interface would be an adapter class
 * that reads from and writes to an XML file.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public interface Endpoint {

    // kj: sharpen the attribute definitions

    /**
     * <br> Get the endpoint URI <br><br>
     *
     * The URI by which the harvest cycle will try to connect to the endpoint.
     *
     * @return endpoint URI
     */
    public abstract String getURI ();

    /**
     * <br> Get the group
     *
     * @return the group the endpoint belongs to
     *
     */
    public abstract String getGroup ();

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
     * Only if the harvest cycle itself is in retry mode, it can effectively
     * retry harvesting the endpoint. <br><br>
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
     * be specified independently from the harvest cycle.
     *
     * @return the scenario
     */
    public abstract CycleParam.Scenario getScenario ();

    /**
     * <br> Return the date for incrementally harvesting <br><br>
     *
     * If recorded, the method will return the date, YYYY-MM-DD, of the most
     * recent and successful harvest of the endpoint. A subsequent cycle will
     * use this date when harvesting the endpoint incrementally. <br><br>
     *
     * Note: a harvesting cycle needs to set the date by invoking the
     * doneHarvesting method.
     *
     * @return the empty string if not recorded, otherwise the date
     */
    public abstract String getRecentHarvestDate();

    /**
     * <br> Indicate success or failure <br><br>
     *
     * Regardless of success, the method sets the attempted attribute to the
     * current date. If done, the method also updates the harvested attribute.
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
     * The number of records incrementally harvested from the endpoint in
     * the most recent harvest cycle. If the endpoint was not incrementally
     * harvested, the increment will be zero.
     *
     * @return the increment
     */
    public abstract long getIncrement ();

    /**
     * <br> Set the record increment <br><br>
     *
     * The number of records incrementally harvested from the endpoint in the
     * most recent harvest cycle. If the endpoint was not incrementally
     * harvested, the increment will be zero.
     *
     * @param increment the increment
     */
    public abstract void setIncrement (long increment);
}
