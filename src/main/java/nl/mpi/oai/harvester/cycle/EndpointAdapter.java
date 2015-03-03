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
import nl.mpi.oai.harvester.generated.ObjectFactory;
import nl.mpi.oai.harvester.generated.OverviewType;
import nl.mpi.oai.harvester.generated.ScenarioType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <br> Access to endpoint attributes <br><br>
 *
 * An endpoint adapter is an object providing access to endpoint attributes
 * stored as XML elements. To access a desired attribute, invoke the designated
 * method on the adapter object. <br><br>
 *
 * This class depends on JAXB to generate classes representing the XML harvest
 * cycle file. When an adapter method needs to obtain an endpoint attribute,
 * it will invoke a corresponding method on the EndpointType object. The class
 * also depends on the JAXB factory for creating endpoint elements and the
 * elements enclosed in them.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
class EndpointAdapter implements Endpoint {

    // the JAXB representation of the harvest cycle
    private final OverviewType overviewType;

    // the endpoint referenced by the URI supplied to the constructor
    private EndpointType endpointType;

    // the JAXB factory, needed to create a default endpoint
    private final ObjectFactory factory;

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
        endpointType = factory.createEndpointType();

        // create the endpoint fields, and set them to default values
        endpointType.setBlock(Boolean.FALSE);
        endpointType.setIncremental(Boolean.TRUE);
        endpointType.setURI(endpointURI);
        endpointType.setGroup(group);

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
     * Precondition: endpointURI, group, overviewType and factory are not null <br><br>
     *
     * In case the constructor cannot find the endpoint URI specified in the
     * cycle elements, it will create a new endpoint and add to the endpoints
     * already present in the cycle. <br><br>
     *
     * @param endpointURI  the URI of the endpoint the cycle should attempt to
     *                     harvest
     * @param group        the group the endpoint belongs to
     * @param overviewType the JAXB representation of the harvest cycle
     *                     element
     * @param factory      the JAXB factory for harvest cycle elements
     *
     */
     EndpointAdapter(String endpointURI, String group,
                     OverviewType overviewType, ObjectFactory factory) {

        // remember the cycle, remember the factory
        this.overviewType = overviewType;
        this.factory      = factory;

        // look for the endpoint in the cycle
        endpointType = FindEndpoint(endpointURI);

        if (endpointType == null) {
            // if it is not in the cycle, create a default endpoint
            endpointType = CreateDefault(endpointURI, group);

            // and add it to the cycle
            overviewType.getEndpoint().add(endpointType);
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
    public Overview.Scenario getScenario() {

        // try to get attribute
        ScenarioType scenarioType = endpointType.getScenario();

        if (scenarioType == null) {
            // attribute not XML cycle element, add it to it
            endpointType.setScenario(ScenarioType.LIST_RECORDS);
            return Overview.Scenario.ListRecords;
        } else {
            switch (scenarioType) {
                case LIST_PREFIXES:
                    return Overview.Scenario.ListPrefixes;
                case LIST_IDENTIFIERS:
                    return Overview.Scenario.ListIdentifiers;
                default:
                    return Overview.Scenario.ListRecords;
            }
        }
    }

    @Override
    public String getRecentHarvestDate() {

        XMLGregorianCalendar XMLDate;
        XMLDate = endpointType.getHarvested();

        if (XMLDate == null){
            return "";
        } else {
            // convert XMLGregorianCalendar to string
            return XMLDate.toString();
        }
    }

    @Override
    public void doneHarvesting(Boolean done) {

        // get the current date
        XMLGregorianCalendar XMLDate;

        try {
            XMLDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();

            Calendar c = Calendar.getInstance();
            c.getTime();
            XMLDate.setDay(c.get(Calendar.DAY_OF_MONTH));
            XMLDate.setMonth(c.get(Calendar.MONTH) + 1);
            XMLDate.setYear(c.get(Calendar.YEAR));

            // initialise the attribute representing the date of the attempt
            endpointType.setAttempted(XMLDate);

            if (done) {
                // successful attempt, also set attribute representing this
                endpointType.setHarvested(XMLDate);
            }

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

        endpointType.setCount(count);
    }

    @Override
    public long getIncrement() {

        // try to get attribute, use long reference type to check for null
        Long increment = endpointType.getIncrement();

        if (increment == null){
            // attribute not XML cycle element, add it to it
            endpointType.setIncrement((long) 0);
            return 0;
        } else {
            return increment;
        }
    }

    @Override
    public void setIncrement(long increment) {

        endpointType.setIncrement(increment);
    }
}
