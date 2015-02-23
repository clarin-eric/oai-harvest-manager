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
import nl.mpi.oai.harvester.generated.HarvestingType;
import nl.mpi.oai.harvester.generated.ObjectFactory;
import nl.mpi.oai.harvester.harvesting.HarvestingException;

import javax.xml.bind.*;

import java.io.File;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <br> A factory for harvesting overview objects <br><br>
 *
 * A harvester overview object packages both general data about the harvesting
 * cycle as well as information specific to individual endpoints that have been
 * harvested at least once before. <br><br>
 *
 * By supplying its URI, a client can identify an endpoint. By interpreting the
 * data recorded, the client can decide if an endpoint needs to be harvested,
 * and also, which method of harvesting should be applied. The client can update
 * the endpoint data to reflect the harvesting attempt. <br><br>
 *
 * By implementing the Harvesting interface as well as the Endpoint interface,
 * this class provides data stored in XML format to a harvesting cycle in an
 * abstract way. The XML file is defined by the harvesting.xsd file, and data
 * is (de)linearised by invoking methods of JAXB generated classes. Note: the
 * XSD file resides in the src/xsd directory. <br><br>
 *
 * About the XML cycle and endpoint representation. The XML file needs to valid
 * with respect to the XSD supplied. These following elements are optional. <br>
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
 * Being optional, defaults do not apply. The following fields are obligatory.
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
 * cycle needs to supply it   <br>
 * cycle needs to supply it   <br>
 * defaults to false          <br>
 * defaults to false          <br>
 * defaults to false          <br>
 * defaults to 'list records' <br>
 * </td>
 * </table><br>
 *
 * When harvesting is done, the harvesting overview should be finalised. If it
 * is not, the changes made to the endpoint data will not be saved.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public final class HarvestingOverview {

    // the file supplied on construction
    private final File file;

    // factory that creates objects of the generated classes
    private final ObjectFactory factory;

    // reference to a generated type object representing the XML 
    private HarvestingType harvesting;

    /**
     * Make available general harvesting data by invoking the methods supplied
     * in the generated HarvestingType class
     */
    private class HarvestingAdapter implements Harvesting {

        /**
         * Return the mode by invoking generated methods
         *
         * @return the mode
         */
        @Override
        public Mode getHarvestMode() {

            Harvesting.Mode mode = null;

            switch (harvesting.getMode()) {

                case NORMAL:
                    mode = Mode.normal;
                    break;
                case REFRESH:
                    mode = Mode.refresh;
                    break;
                case RETRY:
                    mode = Mode.retry;
                    break;
            }
            return mode;
        }

        /**
         * Return the date by invoking generated methods
         *
         * @return the date
         */
        @Override
        public String getHarvestFromDate() {

            // convert XMLGregorianCalendar to string

            XMLGregorianCalendar XMLDate;
            XMLDate = harvesting.getHarvestFromDate();

            /* kj: check this

              XMLDate.toString() // would be equal to 1971-11-03

              Calendar c = Calendar.getInstance();

              c.set(XMLDate.getYear(), XMLDate.getMonth(), XMLDate.getDay());

              The Jetbrains code check reveals a magic endpoint:

              http://blog.jetbrains.com/idea/2012/02/new-magic-constant-inspection/

              It looks like this problem is not too serious. Moreover, we can do
              without the XMLDate conversion to Gregorian

              return c.toString();
            */

            // epoch zero means no previous harvest

            return XMLDate.toString();
        }

        @Override
        public String getScenario() {
            return null;
        }
    }

    /**
     * Make available endpoint harvesting data  by invoking the methods supplied
     * in the generated EndpointType class
     *
     * You can interpret this class as an adapter:
     */
    private class EndPointAdapter implements Endpoint {

        // reference to a generated type object representing the XML 
        EndpointType endpointType;

        /**
         * Create default EndpointType object
         *
         * @param endpointURI the URI identifying the endpoint
         */
        private EndpointType CreateDefault(String endpointURI) {

            endpointType = factory.createEndpointType();

            // set endpoint to default values
            endpointType.setBlock(Boolean.FALSE);
            endpointType.setIncremental(Boolean.TRUE);
            endpointType.setURI(endpointURI);

            return endpointType;
        }

        /**
         * Look for the endpoint in a HarvestingType object; use an URI as the
         * for a key
         *
         * @param endpointURI the URI identifying the endpoint
         * @return null or the endpoint
         */
        private EndpointType FindEndpoint(String endpointURI) {

            // assume the endpoint is not there
            endpointType = null;

            // iterate over the elements in the harvested element
            Boolean found = false;

            for (int i = 0; i < harvesting.getEndpoint().size() && !found; i++) {
                endpointType = harvesting.getEndpoint().get(i);
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
         * Get endpoint data
         */
        EndPointAdapter(String endpointURI) {

            // look for the endpoint in the XML data            
            endpointType = FindEndpoint(endpointURI);

            if (endpointType == null) {
                // if it is not in the XML, create a default endpoint data
                endpointType = CreateDefault(endpointURI);

                // and add this data to the XML
                harvesting.getEndpoint().add(endpointType);
            }
        }

        /**
         * Return the date by invoking generated methods
         *
         * @return the date
         */
        @Override
        public String getRecentHarvestDate() {

            // convert XMLGregorianCalendar to string

            XMLGregorianCalendar XMLDate;
            XMLDate = harvesting.getHarvestFromDate();

            /* kj: check this

              XMLDate.toString() // would be equal to 1971-11-03

              Calendar c = Calendar.getInstance();

              c.set(XMLDate.getYear(), XMLDate.getMonth(), XMLDate.getDay());

              The Jetbrains code check reveals a magic endpoint:

              http://blog.jetbrains.com/idea/2012/02/new-magic-constant-inspection/

              It looks like this problem is not too serious. Moreover, we can do
              without the XMLDate conversion to Gregorian

              return c.toString();
            */

            // epoch zero means no previous harvest

            return XMLDate.toString();
        }

        /**
         * Register success or failure by invoking generated methods
         *
         * @param done true if and only if the endpoint was harvested
         *             successfully, false otherwise
         */
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

                Logger.getLogger(HarvestingOverview.class.getName()).log(
                        Level.SEVERE, null, endpointType);
            }
        }

        @Override
        public String getGroup() {
            return null;
        }

        @Override
        public void setGroup(String group) {

        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public void setCount(int count) {

        }

        @Override
        public int getIncrement() {
            return 0;
        }

        @Override
        public void setIncrement(int increment) {

        }

        /**
         * Check if full harvest is required by invoking generated methods
         *
         * @return true, if and only in case of an error, the endpoint will
         * be selected for a corrective harvesting run, false otherwise
         */
        @Override
        public boolean retry() {
            return endpointType.isRetry();
        }

        @Override
        public String getURI() {
            return null;
        }

        @Override
        public void setURI(String URI) {

        }

        /**
         * Check if incremental harvest is allowed by invoking generated
         * methods
         *
         * @return true, if and only if incremental harvesting of the endpoint
         * is allowed, false otherwise
         */
        @Override
        public boolean allowIncrementalHarvest() {
            return endpointType.isIncremental();
        }

        @Override
        public String getScenario() {
            return null;
        }

        /**
         * Check if harvesting the endpoint is blocked by invoking generated
         * methods
         *
         * @return true, if and only if the endpoint is to be excluded from
         * harvesting, false otherwise
         */
        @Override
        public boolean blocked() {
            return endpointType.isBlock();
        }
    }

    /**
     * <br> Associate the overview with an XML file <br><br>
     *
     * This constructor initialises the harvestingType and endpointType objects
     * wrapped in the overview class with data from the file.
     *
     * @param fileName name of the file
     */
    public HarvestingOverview(String fileName) {

        // create factory that creates objects of the generated classes
        factory = new ObjectFactory();

        // ask the factory for an object representing the XML
        harvesting = factory.createHarvestingType();

        // remember the file where the XML is
        file = new File(fileName);

        // get the XML from this file
        Object object = JAXB.unmarshal(file, HarvestingType.class);

        /* Check if the object is in the HarvestingType class. Note: if the
           unmarshalling method returns null, the object is not in the class,
           otherwise it is.
          */
        if (object == null) {
            throw new HarvestingException();
        } else {
            harvesting = (HarvestingType) object;
        }
    }

    /**
     * <br> Create an adapter for the harvestingType object <br><br>
     *
     * Note: at the level of the XSD, the endpoints are part of harvesting, at the
     * level of the interface the general harvesting characteristics are separate
     * from the list of endpoints. In fact, the client never gets the complete list,
     * it gets an endpoint referred to by the URI.
     *
     * @return harvesting data
     */
    public Harvesting getHarvesting() {

        return new HarvestingAdapter();
    }

    /**
     * <br> Create an adapter for an endpointType object <br><br>
     *
     * This method returns the adapter object for the endpoint indicated
     * by the endpointURI
     *
     * Note: at the level of the XSD, the endpoints are part of harvesting, at the
     * level of the interface the general harvesting characteristics are separate
     * from the list of endpoints. In fact, the client never gets the complete list,
     * it gets an endpoint referred to by the URI.
     *
     * @param endpointURI the URI of the endpoint state requested
     * @return the endpoint
     */
    public Endpoint getEndPoint(String endpointURI) {

        return new EndPointAdapter(endpointURI);
    }

    /**
     * Save harvesting overview
     *
     */
    @Override
    protected void finalize (){

        // try to invoke superclass finalization
        try {
            super.finalize();
        } catch (Throwable e) {
            Logger.getLogger(HarvestingOverview.class.getName()).log(
                    Level.SEVERE, null, e);
        }

        // finalize the harvesting data
        JAXB.marshal(harvesting, file);
    }
}