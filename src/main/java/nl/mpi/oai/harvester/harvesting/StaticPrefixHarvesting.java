package nl.mpi.oai.harvester.harvesting;

import ORG.oclc.oai.harvester2.verb.Identify;
import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.metadata.Provider;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * <br>Request prefixes<br><br>
 *
 * Applying static harvesting  means that before getting the prefixes, the
 * static contents needs to be fetched from the provider. In fact, in static
 * harvesting, this is all that will be available. Because the records are
 * in this document also, it needs to be passed to other applications of the
 * protocol.<br><br>
 *
 * Note: this class defines what is different from parsing prefixes from a non
 * static endpoint.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
class StaticPrefixHarvesting extends PrefixHarvesting {

    private static final Logger logger = Logger.getLogger(
            StaticPrefixHarvesting.class);

    /**
     * Create object, associate provider data and desired prefix
     *
     * @param provider  the endpoint to address in the request
     * @param actions   specify the actions
     *
     */
    public StaticPrefixHarvesting(Provider provider, ActionSequence actions) {
        super(provider, actions);
        // start parsing
        this.index = 0;
    }

    /**
     * kj: doc
     *
     * @return false if there was an error, true otherwise
     */
    @Override
    public boolean request() {

        // need to restart parsing
        index = 0;

        logger.debug("Requesting prefixes for format " + actions.getInputFormat());

        try {
            response = new Identify(provider.getOaiUrl());
        } catch ( IOException
                | ParserConfigurationException
                | SAXException
                | TransformerException e) {
            logger.error(e.getMessage(), e);
            logger.info("Cannot get an identification from the static " +
                    provider.getOaiUrl() + " endpoint");
            return false;
        }

        // response contains static content
        return true;
    }

    /**
     * <br>Get the prefixes from the document
     *
     * @return false if there was an error, true otherwise
     */
    public boolean processResponse() {

        try {
            nodeList = (NodeList) provider.xpath.evaluate(
                    "/os:Repository/os:ListMetadataFormats",
                    response.getDocument(),
                    XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            /* Something went wrong with the request. This concludes the work
               for this endpoint. kj: not really the request
            */
            logger.error(e.getMessage(), e);
            logger.info("Cannot obtain " + actions.getInputFormat() +
                    " metadata prefixes from endpoint " + provider.oaiUrl);
            return false;
        }

        // nodeList contains a list of prefixes
        return true;
    }
}


