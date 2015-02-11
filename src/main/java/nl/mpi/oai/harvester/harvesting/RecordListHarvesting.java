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
 * <br>This class extends the ListProtocol class by providing ListRecord type of
 * verbs. One with two parameters, for resuming, one with five for the initial
 * request. Since it implements specific verbs, it is also specific in processing
 * and parsing of the responses. <br><br>
 *
 * Note. If the endpoint provides a record in several sets, in the end this
 * class needs to return it to the client only once. This class will use the
 * list provided in the superclass to remove duplicate identifier and prefix
 * pairs. By using this list when parsing, the class will return a record its
 * client at most once. <br><br>
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class RecordListHarvesting extends ListHarvesting implements Harvesting {
    
    private static final Logger logger = Logger.getLogger(RecordListHarvesting.class);


    /**
     * Create object, associate endpoint data and desired prefix 
     * 
     * @param provider the endpoint to address in the request
     * @param prefixes the prefixes returned by the endpoint 
     */
    public RecordListHarvesting(Provider provider, List<String> prefixes) {

        super (provider, prefixes);
        // supply messages specific to requesting records
        message [0] = "Requesting more records with prefix ";
        message [1] = "Requesting records with prefix ";
        message [2] = "Cannot get ";
    }
   
    /**
     * Implementation of the ListRecords verb <br><br>
     *
     * This implementation supplies the form of the verb used in a request
     * based on a resumption token.
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
        return new ListRecords(p1, p2);
    }

    /**
     * Implementation of the ListRecords verb <br><br>
     *
     * This implementation supplies the form of the verb used in the initial
     * request.
     *
     * @param p1 endpoint URL
     * @param p2 from date, for selective harvesting
     * @param p3 until date, for selective harvesting
     * @param p4 metadata prefix
     * @param p5 set
     * @return the response to the request
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
        return new ListRecords(p1, p2, p3, p4, p5);
    }
    
    /**
     * Get token. Here, supply the token returned by the ListRecords verb
     * 
     * @param response the response
     * @return the token
     * @throws TransformerException
     * @throws NoSuchFieldException 
     */
    @Override
    public String getToken (HarvesterVerb response) throws TransformerException,
            NoSuchFieldException{
        return ((ListRecords) this.response).getResumptionToken();
    }

    /**
     * Get the response
     *
     * @return the response
     */
    @Override
    public Document getResponse() {
        return response.getDocument();
    }

    /**
     * Get a list of records from the response
     * 
     * @return true if the list was successfully created, false otherwise
     */
    @Override
    public boolean processResponse() {
        
        // check for protocol error
        
        if (response == null){
            logger.error("Protocol error");
            return false;
        }

        // check if the response needs to be saved

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
     * Get a record from the list
     * 
     * @return null if an error occurred, otherwise the next record in the list
     */
    @Override
    public Object parseResponse() {
        
        // check for protocol error
        
        if (nodeList == null){
            logger.error("Protocol error");
            return null;
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
     * Check for more records in the list 
     * 
     * @return  true if there are more, false otherwise
     */
    @Override
    public boolean fullyParsed() {
        return nIndex == nodeList.getLength();
    }
}