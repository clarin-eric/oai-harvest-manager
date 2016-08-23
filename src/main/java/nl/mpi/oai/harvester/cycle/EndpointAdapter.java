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

package nl.mpi.oai.harvester.cycle;

import nl.mpi.oai.harvester.generated.EndpointType;
import nl.mpi.oai.harvester.generated.OverviewType;
import nl.mpi.oai.harvester.generated.ScenarioType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <br> Access to endpoint properties <br><br>
 *
 * An endpoint adapter is an object providing access to endpoint properties
 * stored as XML elements. To access a desired attribute, invoke the designated
 * method on the adapter object. <br><br>
 *
 * This class depends on JAXB to generate types representing the XML harvest
 * cycle file. When an adapter method needs to obtain an endpoint attribute,
 * it will invoke a corresponding method on the EndpointType object. The class
 * also depends on the JAXB factory for creating endpoint elements and the
 * elements enclosed in them.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
class EndpointAdapter implements Endpoint {

    // the endpoint referenced by the URI supplied to the constructor
    private EndpointType endpointType;

    // overview marshalling object
    private XMLOverview xmlOverview;

    /**
     * Create a default endpoint
     *
     * @param endpointURI the URI identifying the endpoint
     * @param group       the group the endpoint belongs to
     */
    private EndpointType CreateDefault(String endpointURI, String group) {

        /* Because of the constructor, the factory is in place. Ask it to
           create a new endpoint.
         */
        endpointType = xmlOverview.factory.createEndpointType();

        // create the endpoint fields, and set them to default values
        endpointType.setBlock(Boolean.FALSE);
        endpointType.setIncremental(Boolean.TRUE);
        endpointType.setURI(endpointURI);
        endpointType.setGroup(group);

        // save the newly created endpoint to the overview
        xmlOverview.save();

        return endpointType;
    }

    /**
     * Look for the endpoint in the cycle, use an URI as the as the key
     *
     * @param endpointURI the URI identifying the endpoint
     * @return            null if the cycle does not contain the endpoint,
     *                    the intended endpoint otherwise
     */
    private EndpointType FindEndpoint(String endpointURI) {

        // assume the endpoint is not there
        endpointType = null;

        // iterate over the elements in the harvested element
        Boolean found = false;

        // JAXB representation of the overview
        OverviewType overviewType = xmlOverview.overviewType;

        for (int i = 0; i < overviewType.getEndpoint().size() && !found; i++) {
            endpointType = overviewType.getEndpoint().get(i);
            if (endpointType.getURI().compareTo(endpointURI) == 0) {
                found = true;
            }
        }

        if (found) {
            return endpointType;
        } else {
            return null;
        }
    }

    /**
     * Associate the adapter with a URI, a group, an cycle, and a factory <br><br>
     *
     * Precondition: endpointURI, group, xmlOverview fields are in place <br><br>
     *
     * In case the constructor cannot find the endpoint URI specified in the
     * cycle elements, it will create a new endpoint and add to the endpoints
     * already present in the cycle. <br><br>
     *
     * @param endpointURI  the URI of the endpoint the cycle should attempt to
     *                     harvest
     * @param group        the group the endpoint belongs to
     * @param xmlOverview  overview marshalling object
     *
     */
     EndpointAdapter(String endpointURI, String group, String scenario,
             XMLOverview xmlOverview) {

        // remember the cycle, remember the factory
        this.xmlOverview = xmlOverview;

        // look for the endpoint in the cycle
        endpointType = FindEndpoint(endpointURI);

        if (endpointType == null) {
            // if it is not in the cycle, create a default endpoint
            endpointType = CreateDefault(endpointURI, group);

            // and add it to the cycle
            xmlOverview.overviewType.getEndpoint().add(endpointType);
        }
        
        if (scenario!=null) {
            if (scenario.equals("ListPrefixes"))
                endpointType.setScenario(ScenarioType.LIST_PREFIXES);
            else if (scenario.equals("ListIdentifiers"))
                endpointType.setScenario(ScenarioType.LIST_IDENTIFIERS);
            else if (scenario.equals("ListRecords"))
                endpointType.setScenario(ScenarioType.LIST_RECORDS);
        }
    }

    @Override
    public String getURI() {

        // the endpoint URI is in place because of the constructor precondition
        return endpointType.getURI();
    }

    @Override
    public String getGroup() {

        // try to get attribute, use boolean reference type to check for null
        String group = endpointType.getGroup();
        if (group == null){
            // set default group, the empty string
            endpointType.setGroup("");
            return "";
        } else {
            return group;
        }
    }

    @Override
    public boolean blocked() {

        // try to get attribute, use boolean reference type to check for null
        Boolean blocked = endpointType.isBlock();

        if (blocked == null){
            // attribute not XML cycle element, add it to it
            endpointType.setBlock(false);
            return false;
        } else {
            return blocked;
        }
    }

    @Override
    public boolean retry() {

        // try to get attribute, use boolean reference type to check for null
        Boolean retry = endpointType.isRetry();

        if (retry == null){
            // attribute not XML cycle element, add it to it
            endpointType.setRetry(false);
            return false;
        } else {
            return retry;
        }

    }

    @Override
    public boolean allowIncrementalHarvest() {

        // try to get attribute, use boolean reference type to check for null
        Boolean allow = endpointType.isIncremental();

        if (allow == null){
            // attribute not XML cycle element, add it to it
            endpointType.setIncremental(false);
            return false;
        } else {
            return allow;
        }
    }

    @Override
    public boolean allowRefresh() {

        // try to get attribute, use boolean reference type to check for null
        Boolean allow = endpointType.isRefresh();

        if (allow == null){
            // attribute not XML cycle element, add it to it
            endpointType.setRefresh(false);
            return false;
        } else {
            return allow;
        }
    }

    @Override
    public CycleProperties.Scenario getScenario() {

        // try to get attribute
        ScenarioType scenarioType = endpointType.getScenario();

        if (scenarioType == null) {
            // look for a global scenario
            scenarioType = this.xmlOverview.overviewType.getScenario();
        }
        if (scenarioType == null) {
            // fall back to default
            endpointType.setScenario(ScenarioType.LIST_IDENTIFIERS);
            return CycleProperties.Scenario.ListIdentifiers;
        } else {
            switch (scenarioType) {
                case LIST_PREFIXES:
                    return CycleProperties.Scenario.ListPrefixes;
                case LIST_IDENTIFIERS:
                    return CycleProperties.Scenario.ListIdentifiers;
                default:
                    return CycleProperties.Scenario.ListRecords;
            }
        }
    }

    // zero epoch time in the UTC zone
    final DateTime zeroUTC = new DateTime ("1970-01-01T00:00:00.000+00:00",
            DateTimeZone.UTC);

    @Override
    public DateTime getAttemptedDate() {

        XMLGregorianCalendar XMLDate;
        XMLDate = endpointType.getAttempted();

        if (XMLDate == null){
            /* Since there is no default value for this property, there is no
               need to set the date in the overview now. Return the zero epoch
               date in the UTC zone.
             */
            return zeroUTC;
        } else {
            // convert XMLGregorianCalendar to DateTime
            return new DateTime(XMLDate.toString(), DateTimeZone.UTC);
        }
    }

    @Override
    public DateTime getHarvestedDate() {

        XMLGregorianCalendar XMLDate;
        XMLDate = endpointType.getHarvested();

        if (XMLDate == null){
            /* Since there is no default value for this property, there is no
               need to set the date in the overview now. Return the zero epoch
               date in the UTC zone.
             */
            return zeroUTC;
        } else {
            // convert XMLGregorianCalendar to DateTime
            return new DateTime(XMLDate.toString(), DateTimeZone.UTC);
        }
    }

    @Override
    public void doneHarvesting(Boolean done) {

        /* Store the current date in a XMLGregorianCalendar object. Note: at
           the XML level, the date will be represented in ISO8601 format.
         */
        XMLGregorianCalendar xmlGregorianCalendar;

        try {
            // get current time in the UTC zone
            DateTime dateTime = new DateTime (DateTimeZone.UTC);

            // create XML calendar
            xmlGregorianCalendar =
                    DatatypeFactory.newInstance().newXMLGregorianCalendar();

            // set the date related fields
            xmlGregorianCalendar.setDay(dateTime.getDayOfMonth());
            xmlGregorianCalendar.setMonth(dateTime.getMonthOfYear());
            xmlGregorianCalendar.setYear(dateTime.getYear());

            // set the calendar to UTC, this zone sets off 0 minutes from UTC
            xmlGregorianCalendar.setTimezone(0);

            // set the time related fields
            xmlGregorianCalendar.setHour(dateTime.getHourOfDay());
            xmlGregorianCalendar.setMinute(dateTime.getMinuteOfHour());
            xmlGregorianCalendar.setSecond(dateTime.getSecondOfMinute());

            // represent milliseconds as a fraction of a second
            BigDecimal s = BigDecimal.valueOf(dateTime.getMillisOfSecond());
            s = s.divide(BigDecimal.valueOf(1000));

            xmlGregorianCalendar.setFractionalSecond(s);

            // set the property representing the date of the attempt
            endpointType.setAttempted(xmlGregorianCalendar);

            if (done) {
                // successful attempt, also set attribute representing this
                endpointType.setHarvested(xmlGregorianCalendar);
            }

            xmlOverview.save();

        } catch (DatatypeConfigurationException e) {
            // report the error, we cannot continue
            Logger.getLogger(EndpointAdapter.class.getName()).log(
                    Level.SEVERE, null, endpointType);
        }
    }

    @Override
    public long getCount() {

        // try to get attribute, use long reference type to check for null
        Long count = endpointType.getCount();

        if (count == null) {
            // attribute not XML cycle element, add it to it
            endpointType.setCount((long) 0);
            return 0;
        } else {
            return count;
        }
    }

    @Override
    public void setCount(long count) {

        // update the count
        endpointType.setCount(count);
        // update the overview
        xmlOverview.save();
    }

    @Override
    public long getIncrement() {

        // try to get attribute, use long reference type to check for null
        Long increment = endpointType.getIncrement();

        if (increment == null){
            // attribute not XML cycle element, add it to it
            endpointType.setIncrement(0l);
            return 0;
        } else {
            return increment;
        }
    }

    @Override
    public void setIncrement(long increment) {

        // update the increment
        endpointType.setIncrement(increment);
        // update the overview
        xmlOverview.save();
    }
}
