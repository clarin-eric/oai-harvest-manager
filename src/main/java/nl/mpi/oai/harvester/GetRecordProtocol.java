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

import ORG.oclc.oai.harvester2.verb.GetRecord;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Record oriented application of the OAI protocol.
 * 
 * An object of this class receives a provider, an identifier, an a prefix
 * instance obtained by another application of the protocol. Based on these, 
 * it will try request the identified record from the endpoint.
 * 
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class GetRecordProtocol implements Protocol {

    private static final Logger logger = Logger.getLogger(Provider.class);

    // response to the ListRecords command
    private GetRecord response;

    // list response elements to be parsed and made available
    private Document document;

    // information on where to send the request
    private final Provider provider;
    
    // prefix of requested metadata records
    private final String prefix;
    
    // record identifier
    private final String identifier;
    
    // whether or not the previous request send a resumption token
    private boolean requestMore;
    
    // the resumption token send by the previous request
    private String resumptionToken;
    
    // pointer to next element to be parsed and returned 
    private boolean parsed;   
    
    /**
     * Create object, associate provider, desired prefix and identifier
     * 
     * @param provider    information on where to send the request
     * @param prefix      the prefix of the desired record
     * @param identifier  the identifier of the record
     */
    public GetRecordProtocol (Provider provider, String prefix, String identifier){
        this.response    = null;
        this.document    = null;
        this.provider    = provider;
        this.prefix      = prefix;
        this.identifier  = identifier;
        this.requestMore = false;
        this.parsed      = false;
    }

    /**
     * Request the record, retry if needed
     * 
     * @return 
     */
    @Override
    public boolean request() {
                
        int i = 0;
        for (;;) {
            try {
                response = new GetRecord(provider.oaiUrl, identifier, prefix);
                return true;
            } catch ( IOException 
                    | ParserConfigurationException 
                    | SAXException 
                    | TransformerException e) {
                // something went wrong with the request
                logger.error(e.getMessage(), e);
                logger.info("Cannot " + prefix + " record with id " + identifier
                        + "from endpoint " + provider.oaiUrl);
                if (i == provider.maxRetryCount) {
                    // try another record
                    return false;
                } else i++;
            }
        }
    }

    @Override
    public boolean requestMore() {
        // in this track in the protocol, there can only be one response
        throw new UnsupportedOperationException("Protocol error"); 
    }

    @Override
    public boolean processResponse() {
        throw new UnsupportedOperationException("Protocol error");
    }

    /**
     * Get the record 
     * @return 
     */
    @Override
    public Object parseResponse() {
        parsed = true;
        logger.info("Fetched " + prefix + " record " + identifier);

        /* Get the OAI envelope from the response. Use it, together with the
           identifier and provider from the protocol, to create and return a
           metadata record.
        */
        return new MetadataRecord(identifier, response.getDocument(), provider,
                "record");
    }

    /**
     * Check if the record was returned
     * 
     * @return  true if the record was returned, false otherwise     
     */
    @Override
    public boolean fullyParsed() {
        return parsed;
    }
}
