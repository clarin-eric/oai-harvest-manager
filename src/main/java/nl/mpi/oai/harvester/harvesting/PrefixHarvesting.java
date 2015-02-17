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
 *
 * <http://www.gnu.org/licenses/>.
 */

package nl.mpi.oai.harvester.harvesting;

import ORG.oclc.oai.harvester2.verb.ListMetadataFormats;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.control.Util;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <br>Prefix targeted application of the protocol <br><br>
 *
 * Clients to this class can request prefixes supported by a metadata record by
 * supplying endpoint data and an action. <br><br>
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class PrefixHarvesting extends AbstractHarvesting {
    
    private static final Logger logger = Logger.getLogger(
            PrefixHarvesting.class);

    /** list response elements to be parsed and made available */
    NodeList nodeList;
    
    /** Information on where to send the request */
    final Provider provider;
    
    /** Transformations to be performed on the metadata records */
    final ActionSequence actions;
    
    /** Pointer to next element to be parsed and returned */
    int index;

    /**
     * Create object, associate provider data and desired prefix 
     * 
     * @param provider  the endpoint to address in the request
     * @param actions   specify the format requested
     */
    public PrefixHarvesting(Provider provider, ActionSequence actions) {
        super (provider);
        this.response = null;
        this.provider = provider;
        this.actions  = actions;
        // get ready for parsing
        this.index    = 0;
    }

    /**
     * Request prefixes
     * 
     * @return  false if there was an error, true otherwise     
     */
    @Override
    public boolean request() {
        
        logger.debug("Requesting prefixes for format " + actions.getInputFormat());

        try {
            // try to get a response from the provider's endpoint
            response = new ListMetadataFormats(provider.oaiUrl);
        } catch ( TransformerException 
                | ParserConfigurationException 
                | SAXException 
                | IOException e) {
            /* Something went wrong with the request. This concludes the work
               for this endpoint.
            */
            logger.error(e.getMessage(), e);
            logger.info ("Cannot obtain " + actions.getInputFormat() + 
                    " metadata prefixes from endpoint " + provider.oaiUrl);
            return false;
        }

        // response contains a list of prefixes
        return true;
    }

    @Override
    public Document getResponse() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Not supposed to be invoked in this implementation 
     */
    @Override
    public boolean requestMore() {
        throw new UnsupportedOperationException("Protocol error");
    }

    /**
     * Get a list of prefixes from the response
     * 
     * @return  true if the list was successfully created, false otherwise
     */
    @Override
    public boolean processResponse() {

        ListMetadataFormats formats;

        // check if the response is in the expected ListMetadataFormats class
        if (! (response instanceof ListMetadataFormats)){
            throw new UnsupportedOperationException("Protocol error");
        } else {
            formats = (ListMetadataFormats) response;
        }

        try {
            /* Try to create a list of prefixes from the response. On failure,
               stop the work on the endpoint.
             */
            nodeList = (NodeList) provider.xpath.evaluate(
                    "//*[local-name() = 'metadataFormat']",
                    formats.getDocument(), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            logger.error(e.getMessage(), e);
            logger.info("Cannot create list of prefixes for format " +
                    actions.getInputFormat() + " obtained from endpoint "
                    + provider.oaiUrl);
            // something went wrong when creating the list, try another endpoint
            return false;
        }

        if (nodeList == null) {
            logger.warn("The ListMetadataFormats response from the "
                    + this.provider.oaiUrl + "endpoint looks empty");
        }

        // nodeList contains a list of prefixes, het ready for parsing
        index = 0;

        return true;
    }

    /**
     * Get a prefix from the list
     * 
     * @return  null if an error occurred or the prefix in the response does
     *          not match the specified type, otherwise the next prefix 
     */
    @Override
    public Object parseResponse() {

        // index points to the previous prefix node added to the list, update it

        Node node = nodeList.item(index);
        index++;
        
        // try to extract prefix, schema, and namespace from the response

        String prefix, schema, ns;
        try {
            prefix = Util.getNodeText(provider.xpath,
                    "./*[local-name() = 'metadataPrefix']/text()", node);
            schema = Util.getNodeText(provider.xpath,
                    "./*[local-name() = 'schema']/text()", node);
            ns = Util.getNodeText(provider.xpath,
                    "./*[local-name() = 'metadataNamespace']/text()", node);
        } catch (XPathExpressionException e) {
            // something went wrong parsing, try another prefix
            logger.error(e.getMessage(), e);
            logger.info("Error parsing response for prefix");
            return null;
        }
        
        // compare the requested format to what the response offers
        
        String providedType;
        switch (actions.getInputFormat().getType()) {
            case "prefix":
                providedType = prefix;
                break;
            case "schema":
                providedType = schema;
                break;
            case "namespace":
                providedType = ns;
                break;
            default:
                logger.error("Unknown match type "
                        + actions.getInputFormat().getType());
                providedType = null;
        }

        if (!actions.getInputFormat().getValue().equals(providedType)) {
            // the response does not match the requested type
            return null;
        } else {
            /*  Please note that if the response matches the request at the 
                level of schema or namespace, the prefix will still be part of 
                the response. So for example, requesting a cmdi prefix will 
                match cmdi0554, cmdi0571, cmdi2312 or cmdi9836.
            */
            logger.debug("Found suitable prefix: " + prefix);
            return prefix;
        }
    }
    
    /**
     * Check for more records in the list 
     * 
     * @return  true if there are more, false otherwise
     */
    @Override
    public boolean fullyParsed() {
        return index == nodeList.getLength();
    }
}
