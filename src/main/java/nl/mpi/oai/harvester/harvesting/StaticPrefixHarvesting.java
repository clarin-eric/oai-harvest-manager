package nl.mpi.oai.harvester.harvesting;

import ORG.oclc.oai.harvester2.verb.Identify;
import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.control.Util;
import nl.mpi.oai.harvester.metadata.StaticProvider;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 *
 * kj: doc
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class StaticPrefixHarvesting extends PrefixHarvesting implements Harvesting{

    private static final Logger logger = Logger.getLogger(StaticPrefixHarvesting.class);

    // list response elements to be parsed and made available
    private NodeList nodeList;

    // information on where to send the request
    private final StaticProvider provider;

    // transformations to be performed on the metadata records
    private final ActionSequence actions;

    // pointer to next element to be parsed and returned
    private int index;

    /**
     * Create object, associate provider data and desired prefix
     *
     * @param provider  the endpoint to address in the request
     * @param actions   specify the actions
     */
    public StaticPrefixHarvesting(StaticProvider provider, ActionSequence actions) {
        super(provider, actions);
        this.provider = provider;
        this.actions  = actions;
        this.index    = 0;
    }

    @Override
    public boolean request() {

        // need to restart parsing
        index = 0;

        logger.debug("Requesting prefixes for format " + actions.getInputFormat());

        try {
            Identify identify = new Identify(provider.getOaiUrl());
            provider.content = identify.getDocument();
        } catch ( IOException
                | ParserConfigurationException
                | SAXException
                | TransformerException e) {
            logger.error(e.getMessage(), e);
            logger.info("Cannot get an identification from the static " +
                    provider.getOaiUrl() + " endpoint");
            return false;
        }

        return false;
    }

    // kj: probably override something here
}


