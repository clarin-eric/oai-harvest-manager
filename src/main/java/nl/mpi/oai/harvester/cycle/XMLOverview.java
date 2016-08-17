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
import org.joda.time.DateTime;

import javax.xml.bind.JAXB;
import java.io.File;

/**
 * <br> OverviewType object marshalling <br><br>
 *
 * Read and write an overview to and from an XML file by using the adapters
 * defined in the package. The methods in this class communicate the general
 * cycle and endpoint properties through the CycleProperties and Endpoint
 * interfaces.
 *
 * After the XML file is read by the constructor, a client obtains properties
 * through the getCycleProperties and getEndpoint methods. When it modifies
 * general or endpoint properties, it needs to write back the overview to the
 * file. The client can do so by invoking the finalize method in this class.
 *
 * Note: this class relies on JAXB to generate the types that reflect the XSD
 * defined overviews.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
final class XMLOverview {

    // the file supplied on construction
    private File file;

    // reference to a JAXB generated overviewType object representing the XML
    OverviewType overviewType;

    // factory that creates objects of the generated classes
    final ObjectFactory factory;

    /**
     * <br> Associate the cycle with an XML file <br><br>
     *
     * This constructor initialises the OverviewType object with data from the
     * file.
     *
     * @param overviewFile name of the file
     */
    public XMLOverview(File overviewFile) {

        // create factory that creates objects of the generated classes
        factory = new ObjectFactory();

        // ask the factory for an object that can represent XML
        overviewType = factory.createOverviewType();

        // remember the XML file
        file = overviewFile;

        // get the XML from this file
        Object object = JAXB.unmarshal(file, OverviewType.class);

        /* Check if the object is in the OverviewType class. Note: if the
           unmarshalling method returns null, the object is not in the class,
           otherwise it is.
          */
        if (object == null) {
            throw new Exception();
        } else {
            overviewType = (OverviewType) object;
        }
    }

    /**
     * <br> Create an adapter for the CyclePropertiesType object <br><br>
     *
     * This method returns the adapter object for the general properties
     * enclosed in the EndpointType object. <br><br>
     *
     * @return cycle properties
     */
    public CycleProperties getCycleProperties() {

        return new CyclePropertiesAdapter(this);
    }

    /**
     * <br> Create an adapter for an EndpointType object <br><br>
     *
     * This method returns the adapter object for the endpoint indicated. <br><br>
     *
     * Note: the adapter only makes available the properties for the endpoint
     * indicated. Other endpoints or general properties defined in the overview
     * are outside the scope of the adapter.
     *
     * @param endpointURI the URI of the endpoint requested
     * @param group       the group the endpoint belongs to
     * @param scenario    the scenario used by this endpoint
     * @return            endpoint properties
     */
    public Endpoint getEndpoint(String endpointURI, String group, String scenario) {

        /* Since methods outside the package can invoke this method, check for
           the parameter object to be in place.
         */
        if (endpointURI == null){
            throw new IllegalArgumentException("endpoint URI is null");
        }
        if (group == null){
            throw new IllegalArgumentException("endpoint group is null");
        }

        return new EndpointAdapter(endpointURI, group, scenario, this);
    }

    /**
     * <br> Create an adapter for an EndpointType object <br><br>
     *
     * This method returns the adapter object for the endpoint indicated. <br><br>
     *
     * Note: the adapter only makes available the properties for the endpoint
     * indicated. Other endpoints or general properties defined in the overview
     * are outside the scope of the adapter.
     *
     * @param endpointURI the URI of the endpoint requested
     * @param group       the group the endpoint belongs to
     * @return            endpoint properties
     */
    public Endpoint getEndpoint(String endpointURI, String group) {

        /* Since methods outside the package can invoke this method, check for
           the parameter object to be in place.
         */
        if (endpointURI == null){
            throw new IllegalArgumentException("endpoint URI is null");
        }
        if (group == null){
            throw new IllegalArgumentException("endpoint group is null");
        }

        return new EndpointAdapter(endpointURI, group, null, this);
    }

    /**
     * <br> Create an adapter for an endpointType object <br><br>
     *
     * This method returns the adapter object for the endpoint indicated. <br><br>
     *
     * Note: the adapter only makes available the properties for the endpoint
     * indicated. Other endpoints or general properties defined in the overview
     * are outside the scope of the adapter.
     *
     * @param endpointType the endpoint indicated
     * @return endpoint properties
     */
    public Endpoint getEndpoint (EndpointType endpointType){

        /* Since only methods in the package can invoke this method, assume the
           endpointType is in place.
         */
        return new EndpointAdapter(endpointType.getURI(), endpointType.getGroup(),
                (endpointType.getScenario()!=null?endpointType.getScenario().toString():null), this);
    }

    /**
     * <br> Save the overview <br><br>
     */
    public synchronized void save (){

        // marshall the overview
        JAXB.marshal(overviewType, file);
    }

    /**
     * <br> Save the overview in a file different from the original one <br><br>
     *
     * For testing, allow the client to save the overview in a file different
     * from the one that contained the overview the cycle started with. Note:
     * the method will not modify the original file.
     */
    synchronized void save (File file){

        // remember the new file
        this.file = file;

        // marshall the overview
        JAXB.marshal(overviewType, file);
    }

    /**
     * <br> Save the overview in a file different from the original one <br><br>
     *
     * Allow the client to rotate the file. The method will rename the
     * original file to reflect the current date and time. Next to this,
     * the method will store the overview of the current harvest attempts
     * in a file with the named after the original file.
     */
    public synchronized boolean rotateAndSave (){

        // get the original path and name

        String parent = file.getParent();
        String name   = file.getName();

        int index = name.lastIndexOf(".");

        String nameWithoutExtension = name.substring(0, index);
        String extension = name.substring(index + 1);

        // get the date and time
        DateTime dateTime = new DateTime ();

        // append the date and time and extension
        String newName = parent + "/"+ nameWithoutExtension + " at " +
                dateTime.toString() + "." + extension;

        // create a new file
        File newFile = new File (newName);

        // rename the original file
        boolean done = file.renameTo(newFile);

        if (! done){
            return false;
        } else {
            // create another new file
            File anotherNewFile = new File (parent + "/" + nameWithoutExtension +
                    "." + extension);

            // marshall the overview under the name of the original file
            JAXB.marshal(overviewType, anotherNewFile);

            return true;
        }
    }

}