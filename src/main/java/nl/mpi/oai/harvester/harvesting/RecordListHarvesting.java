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

import ORG.oclc.oai.harvester2.verb.HarvesterVerb;
import ORG.oclc.oai.harvester2.verb.ListRecords;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.Provider;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <br> Implement methods for requesting a list of records <br><br>
 *
 * The methods in this class invoke methods in the OCLC library that implement
 * the OAI ListRecords verb. This verb accepts two different types of lists of
 * parameters: one with two, and one with five parameters. Please refer to the
 * definition of the OAI protocol for a definition of these parameters. <br><br>
 *
 * This class provides list based harvesting with a concrete verb to base
 * requests on. Because supplies a specific verb, ListRecords, the response
 * processing needed is specific also. Hence the class also implements this
 * processing. <br><br>
 *
 * Note. If the endpoint provides a record in several sets, in the end this
 * class needs to return it to the client only once. This class will use the
 * list provided in the superclass to remove duplicate identifier and prefix
 * pairs. By using this list when parsing, the class will return a record its
 * client at most once.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 * @author Lari Lampen (MPI-PL, xpath parsing)
 */
public final class RecordListHarvesting extends ListHarvesting
        implements Harvesting {
    
    private static final Logger logger = Logger.getLogger(RecordListHarvesting.class);

    /**
     * Associate endpoint data and desired prefix
     * 
     * @param provider the endpoint to address in the request
     * @param prefixes the prefixes returned by the endpoint 
     */
    public RecordListHarvesting(Provider provider, List<String> prefixes) {

        super (provider, prefixes);
        // supply the superclass with messages specific to requesting records
        message [0] = "Requesting more records with prefix ";
        message [1] = "Requesting records with prefix ";
        message [2] = "Cannot get ";
        /* Invariant: the response is in place, please refer to the superclass
           constructor.
         */
    }
   
    /**
     * <br> Create a request based on the two parameter ListRecords verb <br><br>
     *
     * This method creates a request following a resumption token: a command
     * based on the ListRecords verb and two parameters. It returns a ListRecord
     * object from the OCLC library.
     *
     * @param p1 metadata prefix
     * @param p2 resumption token
     * @return the response to the request
     * @throws java.io.IOException 
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerException
     * @throws java.lang.NoSuchFieldException
     */
    @Override
    public HarvesterVerb verb2(String p1, String p2) throws 
            IOException,
            ParserConfigurationException,
            SAXException,
            TransformerException,
            NoSuchFieldException {

        // implement by returning ListRecords with the two parameters supplied
        return new ListRecords(p1, p2);
    }

    /**
     * Create a request based on the five parameter ListRecords verb <br><br>
     *
     * This method creates the initial ListRecords request: a command based
     * on the ListRecords verb and five parameters. It returns a ListRecord
     * object from the OCLC library.
     *
     * @param p1 endpoint URL
     * @param p2 from date, for selective harvesting
     * @param p3 until date, for selective harvesting
     * @param p4 metadata prefix
     * @param p5 set
     * @return the request
     * @throws java.io.IOException 
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.transform.TransformerException
     * @throws java.lang.NoSuchFieldException
     */
    @Override
    public HarvesterVerb verb5(String p1, String p2, String p3, String p4,
            String p5) throws
            IOException,
            ParserConfigurationException,
            SAXException,
            TransformerException,
            NoSuchFieldException {

        // implement by returning ListRecords with the five parameters supplied
        return new ListRecords(p1, p2, p3, p4, p5);
    }
    
    /**
     * <br> Get the resumption token associated with a specific response <br><br>
     *
     * This method implements a resumption token request by invoking the
     * getResumptionToken OCLC library method.
     *
     * @param response the response
     * @return the token
     * @throws TransformerException
     * @throws NoSuchFieldException 
     */
    @Override
    public String getToken (HarvesterVerb response) throws
            TransformerException,
            NoSuchFieldException{

        // invariant: the response is ListRecords class object
        return ((ListRecords) this.response).getResumptionToken();
    }

    /**
     * <br> Get the response from the endpoin <br><br>
     *
     * @return the response
     */
    @Override
    public Document getResponse() {

        // response is in place, please refer to the superclass constructor
        return response.getDocument();
    }

    /**
     * <br> Create a list of metadata elements from the response <br><br>
     *
     * This method filters a list of nodes from the response. The filter is
     * an XPath expression build around the ListRecords element, the element
     * that holds the metadata records.
     *
     * Note: the parseResponse method will take the list of nodes as input
     * 
     * @return true if the list was successfully created, false otherwise
     */
    @Override
    public boolean processResponse() {

        // response is in place, please refer to the superclass constructor
        try {
            /* Try to create a list of records from the response. On failure,
               stop the work on the current prefix.
             */
            nodeList = (NodeList)provider.xpath.evaluate(
                    "//*[parent::*[local-name()='ListRecords']]",
                    response.getDocument(), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // something went wrong when creating the list, try another prefix
            logger.error(e.getMessage(), e);
            logger.info("Cannot create list of " + prefixes.get(pIndex) + 
                    " records for endpoint " + provider.oaiUrl);
            return false;
        }
        
        return true;
    }

    /**
     * <br> Return the next metadata element in the list <br><br>
     *
     * This method returns the next metadata element from the list of nodes
     * created by the processResponse method. It applies XPath filtering to
     * the header and record elements.
     *
     * Note: the method will skip records the endpoint has flagged as 'deleted'
     * 
     * @return null if an error occurred, otherwise the next record in the list
     */
    @Override
    public Object parseResponse() {
        
        // check for protocol error
        if (nodeList == null){
            throw new HarvestingException();
        }
                     
        // turn the next node into a document
        Node node = nodeList.item(nIndex).cloneNode(true);
        nIndex++;
        Document doc = provider.db.newDocument();
        Node copy = doc.importNode(node, true);
        doc.appendChild(copy);

        // evaluate the document, find the identifier
        Node idNode;
        try {
            idNode = (Node) provider.xpath.evaluate("//*[starts-with(local-name(),"
                    + "'identifier') and parent::*[local-name()='header'"
                    + "and not(@status='deleted')]]/text()",
                    doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            // something went wrong parsing, try another record
            logger.error(e.getMessage(), e);
            logger.info("error parsing header, skipping record");
            return null;
        }

        if (idNode == null) {
            /* The OAI header does not contain an identifier or the record has
               been marked as deleted. In any case: skip it.
            */
            return null;
        }

        // evaluate the document, find the Metadata record
        Node dataNode;
        try {
            dataNode = (Node) provider.xpath.evaluate("//*[local-name()="
                    + "'metadata'"
                    + "and parent::*[local-name()='record']]/*[1]",
                    doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            // something went wrong parsing, try another record
            logger.error(e.getMessage(), e);
            logger.info("cannot find Metadata, skipping record");
            return null;
        }

        if (dataNode == null) {
            // the node does not contain metadata
            return null;
        }
        
        // create a document to store the metadata in
        dataNode = dataNode.cloneNode(true);
        doc = provider.db.newDocument();
        copy = doc.importNode(dataNode, true);
        doc.appendChild(copy);

        String id = idNode.getTextContent();
        
        // check if the record has already been released by trying to add it to
        IdPrefix idPrefix = new IdPrefix (id, prefixes.get(pIndex));
        if (targets.checkAndInsertSorted(idPrefix)){
            // inserted, not released to the client before
            return new Metadata(id, doc, provider, false, false);
        } else {
            // not inserted, the record has already been release to the client
            return null;
        }
    }

    /**
     * <br> Check if the list is fully parsed <br><br>
     *
     * This method checks if, as a consequence of repeatedly invoking
     * processResponse the end of the list nodes created by parseResponse
     * has been reached.
     *
     * @return  true if there are more, false otherwise
     */
    @Override
    public boolean fullyParsed() {

        // check for protocol error
        if (nodeList == null){
            throw new HarvestingException();
        }

        return nIndex == nodeList.getLength();
    }
}