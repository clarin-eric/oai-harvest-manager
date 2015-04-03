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
import javax.annotation.Resource;
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
 * <br> Prefix harvesting <br><br>
 *
 * This class provides for a way to obtain the formats supported by an OAI
 * endpoint. A client can request formats matching the format supplied by an
 * action. After processing the endpoint's response, the client can obtain the
 * formats one after the other by parsing.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class FormatHarvesting extends AbstractHarvesting implements
        Harvesting {
    
    private static final Logger logger = Logger.getLogger(
            FormatHarvesting.class);

    /**
     * <br> List response elements to be parsed and made available
     */
    NodeList nodeList;
    
    /**
     * <br> Information on where to send the request
     */
    final Provider provider;
    
    /**
     * <br< Transformations to be performed on the metadata records
     */
    final ActionSequence actions;
    
    /**
     * <br> Pointer to next element to be parsed and returned
     */
    int index;

    /**
     * <br> Create object, associate provider data and desired prefix
     * 
     * @param provider the endpoint to address in the request
     * @param actions  specify the format requested
     */
    public FormatHarvesting(Provider provider, ActionSequence actions) {
        super (provider);
        this.response = null;
        this.provider = provider;
        this.actions  = actions;
        // get ready for parsing
        this.index    = 0;
    }

    /**
     * kj: using a mockito spy
     */
    public ListMetadataFormats make (String url) throws
            ParserConfigurationException,
            TransformerException,
            SAXException,
            IOException {

        return new ListMetadataFormats(url);
    }

    /**
     * <br> Request prefixes
     * 
     * @return false if there was an error, true otherwise
     */
    @Override
    public boolean request() {
        
        logger.debug("Requesting formats matching " + actions.getInputFormat());

        try {
            // try to get a response from the provider's endpoint
            response = make(provider.oaiUrl);
        } catch ( TransformerException 
                | ParserConfigurationException 
                | SAXException 
                | IOException e) {
            /* Something went wrong with the request. This concludes the work
               for this endpoint.
            */
            logger.error(e.getMessage(), e);
            logger.info ("Cannot obtain metadata formats from endpoint " +
                    provider.getOaiUrl());
            return false;
        }

        // response contains a list of prefixes
        return true;
    }

    @Override
    public Document getResponse() {
        // check for protocol error
        if (response == null){
            throw new HarvestingException();
        } else {
            // response holds 'listMetadataFormats', mock needed in test method
            return response.getDocument();
        }
    }

    /**
     * <br>
     */
    @Override
    public boolean requestMore() {
        // there can only be one request
        throw new HarvestingException();
    }

    /**
     * <br> Create a list of prefixes from the response <br><br>
     *
     * This method filters a list of nodes from the response. The filter is
     * an XPath expression build around the metadataFormat element, the element
     * that holds the prefixes. The parseResponse method takes the list of
     * nodes as input.
     *
     * @return true if the list was successfully created, false otherwise
     */
    @Override
    public boolean processResponse() {

        ListMetadataFormats formats;

        // check if the response is in the expected ListMetadataFormats class
        // kj: mock not possible, different approach needed
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
            logger.info("Cannot create list of formats matching " +
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
     * <br> Return the next prefix element in the list <br><br>
     *
     * This method returns the next metadata element from the list of nodes
     * created by the processResponse method. It applies XPath filtering to
     * the metadataPrefix, schema and metadataNamespace elements.
     *
     * @return null if an error occurred or the prefix in the response does
     *         not match the specified type, otherwise the next prefix
     */
    @Override
    public Object parseResponse() {

        // index points to the next prefix node added to the list

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
            logger.info("Error parsing response for format");
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

                kj: some clarification needed here.

                The method always returns a prefix regardless of the type
                provided. If this type could be a name space, then the string
                returned by the protocol will still contain a reference to a
                prefix. When the method extracts the prefix, it does so even
                the provided response type is of type name space or schema.
            */
            logger.debug("Found suitable prefix: " + prefix);
            return prefix;
        }
    }
    
    /**
     * <br> Check if the list is fully parsed <br><br>
     *
     * This method checks if, as a consequence of repeatedly invoking
     * processResponse the end of the list nodes created by parseResponse
     * has been reached.
     * 
     * @return true if there are more, false otherwise
     */
    @Override
    public boolean fullyParsed() {
        return index == nodeList.getLength();
    }
}
