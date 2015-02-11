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

import ORG.oclc.oai.harvester2.verb.GetRecord;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.Provider;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <br>This class provides the GetRecord verb and the parsing specific to it
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class RecordHarvesting extends AbstractHarvesting {

    private static final Logger logger = Logger.getLogger(RecordHarvesting.class);

    // prefix of requested metadata records
    private final String prefix;

    // record identifier
    private final String identifier;

    // pointer to next element to be parsed and returned 
    private boolean parsed;   
    
    /**
     * Associate provider, desired prefix and identifier with the request <br><br>
     * 
     * @param provider    information on where to send the request
     * @param prefix      the prefix of the desired record
     * @param identifier  the identifier of the record
     */
    public RecordHarvesting(Provider provider, String prefix, String identifier){
        super(provider);
        this.response    = null;
        this.prefix      = prefix;
        this.identifier  = identifier;
        this.parsed      = false;
    }

    /**
     * Request the record, retry if needed <br><br>
     * 
     * @return false if an error occurred, true otherwise
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
                logger.info("Cannot get " + prefix + " record with id " + identifier
                        + " from endpoint " + provider.oaiUrl);
                if (i == provider.maxRetryCount) {
                    // try another record
                    return false;
                } else i++;
            }
        }
    }

    @Override
    public Document getResponse() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean requestMore() {
        // in this application of the protocol, there can only be one response
        throw new UnsupportedOperationException("Protocol error"); 
    }

    @Override
    public boolean processResponse() {
        // this application of the protocol does not need response processing
        throw new UnsupportedOperationException("Protocol error");
    }

    /**
     * Get the record
     *
     * @return the record
     */
    @Override
    public Object parseResponse() {
        parsed = true;
        logger.info("Fetched " + prefix + " record " + identifier);

        /* Get the OAI envelope from the response. Use it, together with the
           identifier and provider from the protocol, to create and return a
           metadata record.
        */
        return new Metadata(identifier, response.getDocument(), provider, true, false);
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
