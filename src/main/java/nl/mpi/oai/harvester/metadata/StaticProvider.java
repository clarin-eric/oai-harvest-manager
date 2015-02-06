package nl.mpi.oai.harvester.metadata;

import nl.mpi.oai.harvester.metadata.Provider;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;

/**
 * kj: doc
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class StaticProvider extends Provider {

    /**
     * kj: doc
     */
    public StaticProvider(String url, int maxRetryCount) throws ParserConfigurationException {
        super(url, maxRetryCount);
    }

    // kj: doc
    public Document content = null;


}
