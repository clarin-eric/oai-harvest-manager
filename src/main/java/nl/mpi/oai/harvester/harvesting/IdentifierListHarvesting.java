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
import ORG.oclc.oai.harvester2.verb.ListIdentifiers;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import nl.mpi.oai.harvester.Provider;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <br>This class extends the ListProtocol class by providing ListIdentifiers type
 * of verbs. One with two parameters, for resuming, one with five for the initial
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
public class IdentifierListHarvesting extends ListHarvesting implements Harvesting {
    
    private static final Logger logger = Logger.getLogger(
            IdentifierListHarvesting.class);
    
    /**
     * Associate endpoint and prefixes with the protocol
     * 
     * @param provider the endpoint to address in the request
     * @param prefixes the prefixes returned by the endpoint
     *
     * kj: unused declaration
     */
    public IdentifierListHarvesting(Provider provider, List<String> prefixes){
        super(provider, prefixes);
        // supply messages specific to requesting identifiers
        message [0] = "Requesting more identifiers of records with prefix ";
        message [1] = "Requesting identifiers of records with prefix ";
        message [2] = "Cannot get identifiers of ";
    }
    
    /**
     * Implementation of the ListIdentifiers verb <br><br>
     *
     * This implementation supplies the form of the verb used in a request
     * based on a resumption token. <br><br>
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
        return new ListIdentifiers(p1, p2);
    }

    /**
     * Implementation of the ListIdentifiers verb <br><br>
     *
     * This implementation supplies the form of the verb used in the initial
     * request. <br><br>
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
        return new ListIdentifiers(p1, p2, p3, p4, p5);
    }
    
    /**
     * Get resumption token <br><br>
     *
     * Supply the token returned by the ListIdentifiers request. <br><br>
     * 
     * @param response the response to the request
     * @return the token
     * @throws TransformerException
     * @throws NoSuchFieldException
     */
    @Override
    public String getToken (HarvesterVerb response) throws TransformerException,
            NoSuchFieldException{
        return ((ListIdentifiers) this.response).getResumptionToken();
    }

    @Override
    public Document getResponse() {
        throw new UnsupportedOperationException("Not implemented yet");
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
            throw new UnsupportedOperationException("Protocol error");
        }

        try {
            /* Try to add the targets in the response to the list. On 
               failure, stop the work on the current prefix.
             */
            nodeList = (NodeList)provider.xpath.evaluate(
                    "//*[starts-with(local-name(),'identifier') "
                            + "and parent::*[local-name()='header' "
                            + "and not(@status='deleted')]]/text()",
                    response.getDocument(), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // something went wrong when creating the list, try another prefix
            logger.error(e.getMessage(), e);
            logger.info("Cannot create list of identifiers of " +
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
     * Invariant: pIndex <= prefixes.size
     * 
     * @return true if the list was parsed successfully, false otherwise
     */
    @Override
    public Object parseResponse() {
        
        // check for protocol errors
        if (tIndex >= targets.size()) {
            throw new HarvestingException();
        }

        IdPrefix pair = targets.get(tIndex);
        tIndex++;
        // get the record for the identifier and prefix
        RecordHarvesting p = new RecordHarvesting(provider, pair.prefix,
                pair.identifier);
        
        if (p.request()) {
            return p.parseResponse();
        } else {
            return null;
        }
    }
}
