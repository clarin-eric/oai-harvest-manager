
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
 * A factory for harvesting overview objects.
 *
 * Objects giving a view on harvesting typically carry data about harvesting at
 * the level of an endpoint or the process of harvesting in general. 
 * 
 * By creating an object of this overview class, the harvest manager has access
 * to the data about harvesting stored in XML format. 
 * 
 * The harvester can retrieve endpoint data by supplying the endpoint URL. In
 * principle it could also generate new default endpoint views.
 *
 * Once retrieved, on harvesting, endpoint data can be modified, thus recording
 * the current state of harvesting. When harvesting is done, the harvesting
 * overview should be finalised. If it is not, the changes made to the endpoint
 * data will not be saved.
 *
 * The HarvestingOverview class implements the abstract Endpoint and
 * Harvesting classes to be used by the harvest manager. It does this by
 * invoking methods from the generated sources. So, in the process of accessing
 * endpoint and harvesting data, the harvest manager will only deal with
 * abstract objects. In this way, however only to some extend, details of the
 * implementation are hidden.
 *
 * @author keeloo
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
        public Mode HarvestMode() {

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
        public String HarvestFromDate() {

            // convert XMLGregorianCalendar to string

            XMLGregorianCalendar XMLDate;
            XMLDate = harvesting.getHarvestFromDate();

            // what if the element is not in the XML ?

            Calendar c = Calendar.getInstance();

            c.set(XMLDate.getYear(), XMLDate.getMonth(), XMLDate.getDay());

            // epoch zero means no previous harvest

            return c.toString();
        }
    }

    /**
     * Make available endpoint harvesting data  by invoking the methods supplied
     * in the generated EndpointType class
     */
    private class EndPointAdapter implements Endpoint {

        // reference to a generated type object representing the XML 
        EndpointType e;

        /**
         * Create default EndpointType object
         *
         * @param endpointURI
         */
        private EndpointType CreateDefault(String endpointURI) {

            e = factory.createEndpointType();

            // set some elements in the endpoint
            e.setBlock(Boolean.FALSE);
            e.setIncremental(Boolean.TRUE);
            e.setURI(endpointURI);
            e.setState("something might be wrong here");

            return e;
        }

        /**
         * Look for the endpoint in a HarvestingType object; use an URI as the
         * for a key
         *
         * @param endpointURI
         * @return null or the endpoint
         */
        private EndpointType FindEndpoint(String endpointURI) {

            // assume the endpoint is not there
            e = null;

            // iterate over the elements in the harvested element
            Boolean found = false;

            for (int i = 0; i < harvesting.getEndpoint().size() && !found; i++) {
                e = harvesting.getEndpoint().get(i);
                if (e.getURI().compareTo(endpointURI) == 0) {
                    found = true;
                }
            }

            if (found) {
            } else {
                e = null;
            }

            return e;
        }

        /**
         * Get endpoint data
         */
        EndPointAdapter(String endpointURI) {

            // look for the endpoint in the XML data            
            e = FindEndpoint(endpointURI);

            if (e == null) {
                // if it is not in the XML, create a default endpoint data
                e = CreateDefault(endpointURI);

                // and add this data to the XML
                harvesting.getEndpoint().add(e);
            }
        }

        /**
         * Return the date by invoking generated methods
         *
         * @return the date
         */
        @Override
        public String GetRecentHarvestDate() {

            // return the date of the previous harvest

            XMLGregorianCalendar XMLDate;
            XMLDate = e.getHarvested();

            // what if the element is not in the XML ?

            Calendar c = Calendar.getInstance();

            c.set(XMLDate.getYear(), XMLDate.getMonth(), XMLDate.getDay());

            // epoch zero means no previous harvest

            return c.toString();
        }

        /**
         * Register success or failure by invoking generated methods
         *
         * @param done
         */
        @Override
        public void DoneHarvesting(Boolean done) {

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

                e.setAttempted(XMLDate);

                if (done) {

                    // set a new date for incremental harvesting

                    e.setHarvested(XMLDate);
                }

            } catch (DatatypeConfigurationException ex) {

                Logger.getLogger(HarvestingOverview.class.getName()).log(
                        Level.SEVERE, null, e);
            }
        }

        /**
         * Check if full harvest is required by invoking generated methods
         *
         * @return
         */
        @Override
        public boolean retry() {
            return e.isRetry();
        }

        /**
         * Check if incremental harvest is allowed by invoking generated
         * methods
         *
         * @return
         */
        @Override
        public boolean allowIncrementalHarvest() {
            return e.isIncremental();
        }

        /**
         * Check if harvesting the endpoint is blocked by invoking generated
         * methods
         *
         * @return
         */
        @Override
        public boolean doNotHarvest() {
            return e.isBlock();
        }
    }

    /**
     * Put data back in the XML in the file remembered from the construction
     * <p/>
     * Create an overview based on the XML in a file
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

        if (object instanceof HarvestingType) {
            harvesting = (HarvestingType) object;
        } else {
            throw new HarvestingException();
        }
    }

    /**
     * Ask the factory for general harvesting data
     *
     * @return
     */
    public Harvesting getHarvesting() {

        return new HarvestingAdapter();
    }

    /**
     * Ask the factory for data about the endpoint identified by the URI
     *
     * @param endpointURI
     * @return
     */
    public Endpoint getEndPoint(String endpointURI) {

        return new EndPointAdapter(endpointURI);
    }

    /**
     * Close the harvesting overview
     *
     */
    @Override
    protected void finalize (){

        JAXB.marshal(harvesting, file);
    }
}