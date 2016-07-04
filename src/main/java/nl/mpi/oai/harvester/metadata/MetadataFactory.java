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

package nl.mpi.oai.harvester.metadata;

import nl.mpi.oai.harvester.Provider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nl.mpi.oai.harvester.utils.DocumentSource;
import org.xml.sax.InputSource;

/**
 * <br> Factory for metadata objects <br><br>
 *
 * By injecting the factory into harvesting package constructors, when testing,
 * the flow of metadata created can be intercepted.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class MetadataFactory {

    /**
     * <br> Connect an object that implements the metadata interface <br><br>
     *
     * Normally, when not testing, nothing will be connected. Therefore, this
     * method returns null. By spying on the factory, when using Mockito, can
     * the test can return an object that implements the interface. Typically,
     * a test helper object would be suited: it can receive the generated
     * metadata and compare it to the predefined data.
     *
     * @return an object implementing the metadata interface
     */
    public MetadataInterface connectInterface(){
        return null;
    }

    /**
     * <br> Create a metadata record
     *
     * @param id the metadata record identifier
     * @param prefix the metadata prefix
     * @param doc the document containing the metadata
     * @param endpoint the endpoint URI
     * @param isEnvelope whether or not the data is an OAI envelope
     * @param isList whether or not the document is a list of records
     * @return a metadata object
     */
    public Metadata create (String id, String prefix, DocumentSource doc,
                            Provider endpoint,
                            Boolean isEnvelope, Boolean isList){

        // to remember the metadata once it has been created
        Metadata metadata;

        // create the metadata
        metadata = new Metadata(id, prefix, doc, endpoint, isEnvelope, isList);
        
        // an object implementing the metadata interface
        MetadataInterface metadataInterface;

        /* Connect the object. Normally, no object will be connected. However,
           when testing, using a mocking and spying framework, a test helper
           object could be connected.
         */
        metadataInterface = connectInterface();

        // check if the client connected an object the interface
        if (metadataInterface == null){
            // no object connected
            return metadata;
        } else {
            /* Apparently, the test supplied an object. Move the data to the
               object via the interface. That is: invoke the object's new
               metadata method. Note: because the metadata interface specifies
               it in that way, the method will return the metadata itself. In
               this way the harvester package classes won't notice the test
               spies on them by means of the helper object.
             */
            return metadataInterface.newMetadata (metadata);
        }
    }
}
