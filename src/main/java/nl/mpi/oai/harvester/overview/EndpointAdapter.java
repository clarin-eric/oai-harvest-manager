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

import nl.mpi.oai.harvester.generated.EndpointType;
import nl.mpi.oai.harvester.generated.ObjectFactory;
import nl.mpi.oai.harvester.generated.OverviewType;

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
 * overview file. When an adapter method needs to obtain an endpoint attribute,
 * it will invoke a corresponding method on the EndpointType object. The class
 * also depends on the JAXB factory for creating endpoint elements and the
 * elements enclosed in them.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
class EndpointAdapter implements Endpoint {

    // the JAXB representation of the harvest overview
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
     * Look for the endpoint in the overview, use an URI as the as the key
     *
     * @param endpointURI the URI identifying the endpoint
     * @return            null if the overview does not contain the endpoint,
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
     * Associate the adapter with a URI, a group, an overview, and a factory <br><br>
     *
     * In case the constructor cannot find the endpoint URI specified in the
     * overview elements, it will create a new endpoint and add to the endpoints
     * already present in the overview. <br><br>
     *
     * @param endpointURI  the URI of the endpoint the cycle should attempt to
     *                     harvest
     * @param group        the group the endpoint belongs to
     * @param overviewType the JAXB representation of the harvest overview
     *                     element
     * @param factory      the JAXB factory for harvest overview elements
     */
     EndpointAdapter(String endpointURI, String group,
                     OverviewType overviewType, ObjectFactory factory) {

        // remember the overview, remember the factory
        this.overviewType = overviewType;
        this.factory      = factory;

        // look for the endpoint in the overview
        endpointType = FindEndpoint(endpointURI);

        if (endpointType == null) {
            // if it is not in the overview, create a default endpoint
            endpointType = CreateDefault(endpointURI, group);

            // and add it to the overview
            overviewType.getEndpoint().add(endpointType);
        }
    }

    @Override
    public String getURI() {
        // the endpoint URI is in place, refer to the OverviewXML class
        return endpointType.getURI();
    }

    /**
     * Set the endpoint URI <br><br>
     *
     * The URI by which the harvesting cycle will try to connect to the
     * endpoint.
     *
     * @param URI the endpoint URI
     */
    private void setURI(String URI) {

        endpointType.setURI(URI);
    }

    @Override
    public String getGroup() {

        String group = endpointType.getGroup();
        if (group == null){
            endpointType.setGroup("");
            return "";
        } else {
            return group;
        }
    }

    /**
     * <br> Set the group
     *
     * @param group the group the endpoint belongs to
     */
    private void setGroup(String group) {

        endpointType.setGroup(group);
    }

    // kj: assign defaults and record defaults

    @Override
    public boolean blocked() {

        boolean blocked = endpointType.isBlock();

        return blocked;
    }

    @Override
    public boolean retry() {

        return endpointType.isRetry();
    }

    @Override
    public boolean allowIncrementalHarvest() {

        return endpointType.isIncremental();
    }

    @Override
    public Cycle.Scenario getScenario() {

        Cycle.Scenario scenario;

        switch (endpointType.getScenario()) {

            case LIST_PREFIXES:
                scenario = Cycle.Scenario.ListPrefixes;
                break;
            case LIST_IDENTIFIERS:
                scenario = Cycle.Scenario.ListIdentifiers;
                break;
            case LIST_RECORDS:
                scenario = Cycle.Scenario.ListRecords;
                break;
            default:
                scenario = Cycle.Scenario.ListRecords;
        }
        return scenario;
    }

    @Override
    public String getRecentHarvestDate() {

        // convert XMLGregorianCalendar to string

        XMLGregorianCalendar XMLDate;
        XMLDate = endpointType.getHarvested();

        return XMLDate.toString();
    }

    @Override
    public void doneHarvesting(Boolean done) {

        // try to get the current date

        XMLGregorianCalendar XMLDate;

        try {
            XMLDate = DatatypeFactory.newInstance().newXMLGregorianCalendar();

            Calendar c = Calendar.getInstance();
            c.getTime();
            XMLDate.setDay(c.get(Calendar.DAY_OF_MONTH));
            XMLDate.setMonth(c.get(Calendar.MONTH) + 1);
            XMLDate.setYear(c.get(Calendar.YEAR));

            // in any case, at this date an attempt was made
            endpointType.setAttempted(XMLDate);

            if (done) {

                // set a new date for incremental harvesting
                endpointType.setHarvested(XMLDate);
            }

        } catch (DatatypeConfigurationException ex) {

            Logger.getLogger(EndpointAdapter.class.getName()).log(
                    Level.SEVERE, null, endpointType);
        }
    }

    @Override
    public long getCount() {

        return endpointType.getCount();
    }

    @Override
    public void setCount(long count) {

        endpointType.setCount(count);
    }

    @Override
    public long getIncrement() {

        return endpointType.getIncrement();
    }

    @Override
    public void setIncrement(long increment) {
        endpointType.setIncrement(increment);
    }
}
