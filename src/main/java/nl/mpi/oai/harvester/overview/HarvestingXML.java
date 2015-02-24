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

import nl.mpi.oai.harvester.generated.HarvestingType;
import nl.mpi.oai.harvester.generated.ObjectFactory;
import nl.mpi.oai.harvester.harvesting.HarvestingException;

import javax.xml.bind.*;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <br> A factory for harvesting overview objects <br><br>
 *
 * kj: the following might need to be reviewed
 *
 * A harvester overview object packages both general data about the harvesting
 * cycle as well as information specific to individual endpoints that have been
 * harvested at least once before. <br><br>
 *
 * kj: specify general attributes like mode, date, and scenario elements
 *
 * By supplying its URI, the cycle can identify an endpoint. By interpreting
 * the attributes recorded, it can decide if an endpoint needs to be harvested,
 * and also, which method of harvesting it should apply. The cycle can update
 * the endpoint attributes to reflect the harvesting attempt. <br><br>
 *
 * By returning a CycleAdapter and EndpointAdapter class object through the
 * Cycle and Endpoint interfaces, this class provides data stored in XML format
 * to a harvesting cycle in an abstract way. The XML file is defined by the
 * harvesting.xsd file, and data is (de)linearised by invoking methods of JAXB
 * generated classes. Note: the XSD file resides in the src/xsd directory. <br><br>
 *
 * About the XML cycle and endpoint representation. The XML file needs to valid
 * with respect to the XSD supplied. These following elements are optional. <br>
 *
 * kj: here we have an element to attribute mapping
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
 * kj: consider an alternative for the inline classes
 *
 * Alternative: create EndpointAdapter and CycleAdapter class with a constructor
 * that accepts a HarvestedType object.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public final class HarvestingXML {

    // the file supplied on construction
    private final File file;

    // factory that creates objects of the generated classes
    private final ObjectFactory factory;

    // reference to a generated type object representing the XML 
    private HarvestingType harvesting;

    /**
     * <br> Associate the overview with an XML file <br><br>
     *
     * This constructor initialises the harvestingType and endpointType objects
     * wrapped in the overview class with data from the file.
     *
     * @param fileName name of the file
     */
    public HarvestingXML(String fileName) {

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
    public Cycle getCycle() {

        return new CycleAdapter(harvesting);
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

        return new EndpointAdapter(endpointURI, harvesting);
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
            Logger.getLogger(HarvestingXML.class.getName()).log(
                    Level.SEVERE, null, e);
        }

        // finalize the harvesting data
        JAXB.marshal(harvesting, file);
    }
}