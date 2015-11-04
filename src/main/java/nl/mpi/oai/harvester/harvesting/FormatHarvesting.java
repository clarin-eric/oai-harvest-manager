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
 * <br> Format harvesting <br><br>
 *
 * This class provides for a way to obtain the metadata formats supported by an
 * OAI endpoint. A client can request formats matching the format supplied by an
 * action. After processing the endpoint's response, the client can obtain the
 * formats one after the other by parsing.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class FormatHarvesting extends AbstractHarvesting implements
        Harvesting {
    
    private static final Logger logger = Logger.getLogger(
            FormatHarvesting.class);

    /**
     * <br> What the endpoint responded with
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
     * @param oaiFactory the OAI factory
     * @param provider the endpoint to address in the request
     * @param actions  specify the format requested
     */
    public FormatHarvesting(OAIFactory oaiFactory,
                            Provider provider, ActionSequence actions) {

        super (oaiFactory, provider, null);

        this.document = null;
        this.provider = provider;
        this.actions  = actions;
        // get ready for parsing
        this.index    = 0;
    }

    /**
     * <br> Request metadata formats
     * 
     * @return false if there was an error, true otherwise
     */
    @Override
    public boolean request() {
        
        logger.debug("Requesting formats matching " + actions.getInputFormat());

        int i = 0;
        for (;;) {
            try {
                // get metadata formats from the endpoint
                document = oaiFactory.createListMetadataFormats(provider.oaiUrl, provider.getTimeout());
            } catch (IOException
                    | ParserConfigurationException
                    | SAXException
                    | TransformerException
                    | NoSuchFieldException e) {
                // report
                logger.error("FormatHarvesting["+this+"]["+provider+"] request try["+(i+1)+"/"+provider.maxRetryCount+"] failed!");
                logger.error(e.getMessage(), e);
            }

            if (document == null){
                i++;
                if (i == provider.maxRetryCount) {
                    // something went wrong, ending the work for the current endpoint
                    logger.info ("Cannot obtain metadata formats from endpoint " +
                            provider.getOaiUrl());
                    return false;
                } else {
                    if (provider.retryDelay > 0) {
                        try {
                            Thread.sleep(provider.retryDelay);
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            } else {
                // response contains a list of prefixes
                return true;
            }
        }
    }

    @Override
    public Document getResponse() {

        // check for a protocol error
        if (document == null){
            throw new HarvestingException();
        } else {
            return document;
        }
    }

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
    public boolean processResponse(Document document) {

        // check for a protocol error
        if (document == null){
            throw new UnsupportedOperationException("Protocol error");
        }

        try {
            /* Try to create a list of prefixes from the response. On failure,
               stop the work on the endpoint.
             */
            nodeList = (NodeList) provider.xpath.evaluate(
                    "//*[local-name() = 'metadataFormat']",
                    document, XPathConstants.NODESET);
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

        // nodeList contains a list of prefixes, ready for parsing
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
     * @return null if an error occurred or the response does contain the
     *         specified type, otherwise the next prefix
     */
    @Override
    public Object parseResponse() {

        // index points to the next prefix node in the list
        Node node = nodeList.item(index);
        index++;
        
        /* Try to extract the value from the type specifications the response
           includes. Only look at the 'prefix', 'schema' or 'namespace' types.
         */

        String prefixValue, schemaValue, nsValue;
        try {
            prefixValue = Util.getNodeText(provider.xpath,
                    "./*[local-name() = 'metadataPrefix']/text()", node);
            schemaValue = Util.getNodeText(provider.xpath,
                    "./*[local-name() = 'schema']/text()", node);
            nsValue = Util.getNodeText(provider.xpath,
                    "./*[local-name() = 'metadataNamespace']/text()", node);
        } catch (XPathExpressionException e) {
            // something went wrong parsing, try another prefix
            logger.error(e.getMessage(), e);
            logger.info("Error parsing response for format");
            return null;
        }
        
        /* Remember the value of the type the action sequence indicates as the
           desired type
         */
        String providedValue;

        switch (actions.getInputFormat().getType()) {
            case "prefix":
                providedValue = prefixValue;
                break;
            case "schema":
                providedValue = schemaValue;
                break;
            case "namespace":
                providedValue = nsValue;
                break;
            default:
                logger.error("Unknown match type "
                        + actions.getInputFormat().getType());
                providedValue = null;
        }

        // check if the value offered matches the value desired
        if (!actions.getInputFormat().getValue().equals(providedValue)) {
            // the response does not match the requested type
            return null;
        } else {
            /* Regardless of the level of the match, be it prefix, schema or
               namespace, return the metadata prefix the endpoint supports. This
               means that while requesting a cmdi prefix will match cmdi0554,
               cmdi0571, cmdi2312 or cmdi9836, for example, requesting a cmdi
               namespace can just as well yield any of these prefixes.
            */
            logger.debug("Found suitable prefix: " + prefixValue);
            return prefixValue;
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
