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

import nl.mpi.oai.harvester.generated.CycleType;
import nl.mpi.oai.harvester.generated.EndpointType;
import nl.mpi.oai.harvester.generated.ObjectFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <br>Make available endpoint type attributes <br><br>
 *
 * First, an EndpointAdaptor object associates itself with a CycleType
 * object. After that, it looks for the endpoint. If it finds it, it remembers
 * it. Otherwise it will ask the generated JAXB factory to create a endpoint,
 * and set the fields to default values. <br><br>
 *
 * When an adapter method needs to obtain a harvest cycle attribute, it will
 * invoke a corresponding method on the CycleType object, either found or
 * created. <br><br>
 *
 * This class depends on JAXB to generate classes representing the XML file. It
 * also depends on the JAXB factory for creating the elements used in the XML
 * file.
 *
 * kj: check defaults and obligatory / optional elements
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class EndpointAdapter implements Endpoint {

    // the JAXB created object representing elements from the XML file
    private final CycleType cycleType;

    // the JAXB created and URI referenced endpoint
    private EndpointType endpointType;

    // the JAXB factory needed to create a default endpoint
    private final ObjectFactory factory;

    /**
     * Create default EndpointType object
     *
     * @param endpointURI the URI identifying the endpoint
     */
    private EndpointType CreateDefault(String endpointURI) {

        /* The factory has been initialised, refer to the constructor. Ask it
           to create a new endpoint.
        */
        endpointType = factory.createEndpointType();

        // set endpoint fields to default values
        endpointType.setBlock(Boolean.FALSE);
        endpointType.setIncremental(Boolean.TRUE);
        endpointType.setURI(endpointURI);

        return endpointType;
    }

    /**
     * Look for the endpoint in a CycleType object, use an URI as the
     * as a key
     *
     * @param endpointURI the URI identifying the endpoint
     * @return null or the endpoint
     */
    private EndpointType FindEndpoint(String endpointURI) {

        // assume the endpoint is not there
        endpointType = null;

        // iterate over the elements in the harvested element
        Boolean found = false;

        for (int i = 0; i < cycleType.getEndpoint().size() && !found; i++) {
            endpointType = cycleType.getEndpoint().get(i);
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
     * Associate the adapter with an endpoint URI and CycleType object
     *
     * @param endpointURI the URI of the endpoint to be harvested by the cycle
     * @param cycleType the JAXB representation of the harvesting overview file
     * @param factory the JAXB factory for harvesting overview XML files
     */
    public EndpointAdapter(String endpointURI, CycleType cycleType,
                           ObjectFactory factory) {

        this.cycleType = cycleType;
        this.factory   = factory;

        // look for the endpoint in the XML data
        endpointType = FindEndpoint(endpointURI);

        if (endpointType == null) {
            // if it is not in the XML, create a default endpoint data
            endpointType = CreateDefault(endpointURI);

            // and add this data to the XML
            cycleType.getEndpoint().add(endpointType);
        }
    }

    @Override
    public String getURI() {

        return endpointType.getURI();
    }

    @Override
    public void setURI(String URI) {

        endpointType.setURI(URI);
    }

    @Override
    public String getGroup() {

        return endpointType.getGroup();
    }

    @Override
    public void setGroup(String group) {

        endpointType.setGroup(group);
    }

    @Override
    public boolean blocked() {

        return endpointType.isBlock();
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
