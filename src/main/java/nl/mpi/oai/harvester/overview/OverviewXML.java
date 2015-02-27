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

import nl.mpi.oai.harvester.generated.ObjectFactory;
import nl.mpi.oai.harvester.generated.OverviewType;
import nl.mpi.oai.harvester.harvesting.HarvestingException;

import javax.xml.bind.*;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <br> CycleType object marshalling <br><br>
 *
 * By returning CycleAdapter or EndpointAdapter class objects through the the
 * Cycle and Endpoint interfaces, the methods on the objects in this class make
 * available XML defined attributes of the harvesting cycle.
 *
 * Note: the XSD defining the cycle overview XML files resides in the src/xsd
 * directory. <br><br>
 *
 * These are the elements conveyed through the Cycle interface.
 *
 * <table>
 * <td>
 * mode     <br>
 * date     <br>
 * scenario <br><br>
 * </td>
 * <td>
 * defaults to 'normal' <br>
 * defaults to '1970-01-01' <br>
 * defaults to 'ListRecords' <br><br>
 * </td>
 * </table>
 *
 * From the elements in the Endpoint interface, the following are optional. <br>
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
 * Being optional, defaults do not apply. The following elements are obligatory.
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
 * Please refer to the cycle interface and endpoint interface for a description
 * of the semantics involved. <br><br>
 *
 * By supplying its URI, the harvest cycle can identify an endpoint. By
 * interpreting the attributes recorded, it can decide if an endpoint needs to
 * be harvested, and also, which method of harvesting it should apply. The
 * cycle can update the endpoint attributes to reflect the harvest attempt. <br><br>
 *
 * When harvesting is done, the harvesting overview should be finalised. If it
 * is not, the changes made to the endpoint data will not be saved.
 *
 * kj: check the use of string restrictions before xs:string
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public final class OverviewXML {

    // the file supplied on construction
    private final File file;

    // reference to a generated overviewType object representing the XML
    private OverviewType overviewType;

    // factory that creates objects of the generated classes
    ObjectFactory factory;

    /**
     * <br> Associate the overview with an XML file <br><br>
     *
     * This constructor initialises the harvestingType and endpointType objects
     * wrapped in the overview class with data from the file.
     *
     * @param fileName name of the file
     */
    public OverviewXML(String fileName) {

        // create factory that creates objects of the generated classes
        factory = new ObjectFactory();

        // ask the factory for an object representing the XML
        overviewType = factory.createOverviewType();

        // remember the file wheScenarioTypere the XML is
        file = new File(fileName);

        // get the XML from this file
        Object object = JAXB.unmarshal(file, OverviewType.class);

        /* Check if the object is in the CycleType class. Note: if the
           unmarshalling method returns null, the object is not in the class,
           otherwise it is.
          */
        if (object == null) {
            throw new HarvestingException();
        } else {
            overviewType = (OverviewType) object;
        }
    }

    /**
     * <br> Create an adapter for the harvestingType object <br><br>
     *
     * Note: at the level of the XSD, the endpoints are part of harvesting, at
     * the level of the interface the general harvesting characteristics are
     * separate from the list of endpoints. In fact, the client never gets the
     * complete list, it gets an endpoint referred to by the URI.
     *
     * @return cycle attributes
     */
    public Cycle getCycle() {

        return new CycleAdapter(overviewType);
    }

    /**
     * <br> Create an adapter for an endpointType object <br><br>
     *
     * This method returns the adapter object for the endpoint indicated
     * by the endpointURI
     *
     * Note: at the level of the XSD, the endpoints are part of harvesting, at
     * the level of the interface the general harvesting characteristics are
     * separate from the list of endpoints. In fact, the client never gets the
     * complete list, it gets an endpoint referred to by the URI.
     *
     * @param endpointURI the URI of the endpoint state requested
     * @return endpoint attributes
     */
    public Endpoint getEndPoint(String endpointURI) {

        return new EndpointAdapter(endpointURI, overviewType, factory);
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
            Logger.getLogger(OverviewXML.class.getName()).log(
                    Level.SEVERE, null, e);
        }

        // finalize the harvesting data
        JAXB.marshal(overviewType, file);
    }
}