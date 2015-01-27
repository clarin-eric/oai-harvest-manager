/*
 * Copyright (C) 2014, The Max Planck Institute for
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

package nl.mpi.oai.harvester;

import ORG.oclc.oai.harvester2.verb.HarvesterVerb;
import ORG.oclc.oai.harvester2.verb.ListRecords;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Record oriented application of the OAI protocol.
 *
 * An object of this class receives a provider instance and prefixes obtained by
 * another application of the protocol. Based on these, it will try to get
 * records from the endpoint. For each prefix, this application will consider 
 * sets specified in the provider object.<br><br>
 *
 * Note. Given the nature of the iteration, first over prefixes, within those
 * over sets, it cannot be guaranteed that a records will be requested twice or
 * more often. When the protocol is instantiated without a set specified, every
 * record will be retrieved only once. If in a particular situation duplicates
 * give rise to problems, please use the ListIndentifiers protocol. ??? <br><br>
 *
 * Note. It would be possible to keep track of the records retrieved. Create a
 * list, and add an identifier and prefix idPrefix once a record has been
 * retrieved from the endpoint. Only return a record to the client if it is not
 * in the list. Note: the feature could be turned of when sets == null <br><br>
 * 
 * Note. This class has not been tested in conjunction with sets. <br><br>
 * 
 * Note. This application of the protocol is more efficient when it comes to 
 * memory usage. Clearly explain that here, a list of nodes is kept between the
 * processing of a response and the parsing. Here, the target list is used for
 * keeping track of duplicates. A node list is required for processing the list 
 * records request.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class ListRecordsProtocol extends ListProtocol implements Protocol {
    
    private static final Logger logger = Logger.getLogger(ListRecordsProtocol.class);

    /**
     * Create object, associate endpoint data and desired prefix 
     * 
     * @param provider the endpoint to address in the request
     * @param prefixes the prefixes returned by the endpoint 
     */
    public ListRecordsProtocol (Provider provider, List<String> prefixes) {
        super (provider, prefixes);
        message [0] = "Requesting more records with prefix ";
        message [1] = "Requesting records with prefix ";
        message [2] = "Cannot get ";
    }
   
    /**
     * Supply the ListRecords verb. Here, make the version with two string
     * parameters effective.
     * 
     * @param p1
     * @param p2
     * @return 
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
     * Supply the ListRecords verb. Here, make the version with five string
     * parameters effective.
     * 
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @param p5
     * @return 
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
     * Get token. Here, supply the token returned by the ListRecord method.
     * 
     * @param reponse
     * @return the token
     * @throws TransformerException
     * @throws NoSuchFieldException 
     */
    @Override
    public String getToken (HarvesterVerb reponse) throws TransformerException, 
            NoSuchFieldException{
        return ((ListRecords)response).getResumptionToken();
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

        // evaluate the document, find the metadata record
        Node dataNode;
        try {
            dataNode = (Node) provider.xpath.evaluate("//*[local-name()="
                    + "'metadata'"
                    + "and parent::*[local-name()='record']]/*[1]",
                    doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            // something went wrong parsing, try another record
            logger.error(e.getMessage(), e);
            logger.info("cannot find metadata, skipping record");
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
            return new MetadataRecord(id, doc, provider, "part of multiple records");
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