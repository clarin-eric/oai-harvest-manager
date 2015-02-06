package nl.mpi.oai.harvester.harvesting;

import nl.mpi.oai.harvester.metadata.StaticProvider;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.util.List;

/**
 * kj: doc
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class StaticRecordHarvesting extends AbstractListHarvesting implements
        Harvesting {

    private static final Logger logger = Logger.getLogger(
            AbstractListHarvesting.class);

    // information on where to send the request
    StaticProvider provider;

    // metadata prefixes that need to be requested
    private final List<String> prefixes;
    // pointer to current prefix
    protected int pIndex;

    protected StaticRecordHarvesting(StaticProvider provider,
                                     List<String> prefixes) {
        super(provider);
        this.prefixes = prefixes;
    }

    @Override
    public boolean request() {

        // check for protocol errors

        if (provider.content == null) {
            logger.error("Protocol error");
            return false;
        }

        if (provider.getOaiUrl() == null) {
            logger.error("Protocol error");
            return false;
        }

        // provider content is in place

        return true;
    }

    @Override
    public Document getResponse() {
        return provider.content;
    }

    @Override
    public boolean requestMore() {
        // the response contains everything that can be harvested for this provider and prefix
        return false;
    }

    @Override
    public boolean processResponse() {

        // kj: check for the prefixes to be there

        // get the identifiers associated with the prefixes

        // add the identifier and prefix targets into the array
        for (int j = 0; j < nodeList.getLength(); j++) {
            String identifier = nodeList.item(j).getNodeValue();
            IdPrefix pair = new IdPrefix(identifier,
                    prefixes.get(pIndex));
            targets.checkAndInsertSorted(pair);
        }

        // kj: ready to return the identifiers

        return false;
    }

    @Override
    public Object parseResponse() {

        // check for protocol error
        if (tIndex >= targets.size()) {
            logger.error("Protocol error");
            return null;
        }

        IdPrefix pair = targets.get(tIndex);
        tIndex++;

        /* Get the record for the identifier and prefix. We probably won't use
           the protocol for getting a record here.
        */

        // if everything is fine, return the record, null otherwise

        return null;
    }

    @Override
    public boolean fullyParsed() {
        return false;
    }
}
