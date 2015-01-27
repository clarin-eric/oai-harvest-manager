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
import ORG.oclc.oai.harvester2.verb.ListIdentifiers;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Identifier oriented application of the OAI protocol.
 * 
 * An object of this class receives a provider instance and prefixes obtained by
 * another application of the protocol. Based on these, it will try to get 
 * records from the endpoint. For each prefix, this application will consider 
 * sets specified in the provider object. <br><br>
 * 
 * Note. If the endpoint provides a record in several sets, in the end we need
 * to return it to the client only once. Because of this, the class contains a
 * subclass implementing a sorted array list. Al element will only be inserted
 * into the list if it is not already there. In other words, applying the 
 * protocol in this way will never give rise to requesting a record twice or 
 * more often. <br><br>
 * 
 * Note. This class has not been tested in conjunction with sets. <br><br>
 * 
 * Question. When it comes to saving files, how to deal with multiple instances
 * of he same identifier? Could we assume that an identifier is exclusively used
 * with one prefix only? ??? <br><br>
 * 
 * Note. This application of the protocol is more efficient when it comes to 
 * memory usage. 
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class ListIdentifiersProtocol extends ListProtocol implements Protocol {
    
    private static final Logger logger = Logger.getLogger(
            ListIdentifiersProtocol.class);
    
    /**
     * Create object. Add messages specific to listing identifiers 
     * 
     * @param provider the endpoint to address in the request
     * @param prefixes the prefixes returned by the endpoint 
     */
    ListIdentifiersProtocol (Provider provider, List<String> prefixes){
        super(provider, prefixes);
        message [0] = "Requesting more identifiers of records with prefix ";
        message [1] = "Requesting identifiers of records with prefix ";
        message [2] = "Cannot get identifiers of ";
    }
    
    /**
     * Supply the ListIdentifiers verb. Here, make the version with two string
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
        return new ListIdentifiers(p1, p2);
    }

    /**
     * Supply the ListIdentifiers verb. Here, make the version with five string
     * parameters effective.
     * 
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @param p5
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
        return new ListIdentifiers(p1, p2, p3, p4, p5);
    }
    
    /**
     * Get token. Here, supply the token returned by the ListIdentifier method.
     * 
     * @param reponse
     * @return the token
     * @throws TransformerException
     * @throws NoSuchFieldException
     */
    @Override
    public String getToken (HarvesterVerb reponse) throws TransformerException, 
            NoSuchFieldException{
        return ((ListIdentifiers)response).getResumptionToken();
    }
        
    /**
     * Create a list of identifier prefix targets from the response
     * 
     * @return true if the response was processed successfully, false otherwise
     */
    @Override
    public boolean processResponse() {
        
        // check for protocol error
        
        if (response == null){
            logger.error("Protocol error");
            return false;
        }

        try {
            /* Try to add the targets in the response to the list. On 
               faillure, stop the work on the current prefix.
             */
            nodeList = (NodeList)provider.xpath.evaluate(
                    "//*[starts-with(local-name(),'identifier') "
                            + "and parent::*[local-name()='header' "
                            + "and not(@status='deleted')]]/text()",
                    response.getDocument(), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // something went wrong when creating the list, try another prefix
            logger.error(e.getMessage(), e);
            logger.info("Cannot create list of indentifiers of " + 
                    prefixes.get(pIndex) +
                    " records for endpoint " + provider.oaiUrl);
            return false;
        }
        
        // add the identifier and prefix targets into the array
        for (int j = 0; j < nodeList.getLength(); j++) {
            String identifier = nodeList.item(j).getNodeValue();
            IdPrefix pair = new IdPrefix (identifier, 
                    prefixes.get(pIndex));
            targets.checkAndInsertSorted(pair);
        }
        
        return true;
    }

    /**
     * Return the next identifier prefix idPrefix from the list of targets
     * 
     * @return true if the list was parsed successfully, false otherwise
     */
    @Override
    public Object parseResponse() {
        
        // check for protocol error
        if (tIndex >= targets.size()) {
            logger.error("Protocol error");
            return null;
        }

        IdPrefix pair = targets.get(tIndex);
        tIndex++;
        // get the record for the identifier and prefix
        
        GetRecordProtocol p = new GetRecordProtocol (provider, pair.prefix, 
                pair.identifier);
        
        if (p.request()) {
            return p.parseResponse();
        } else {
            return null;
        }
    }
}
