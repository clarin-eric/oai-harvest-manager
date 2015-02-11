package nl.mpi.oai.harvester.harvesting;

import ORG.oclc.oai.harvester2.verb.Identify;
import nl.mpi.oai.harvester.StaticProvider;
import nl.mpi.oai.harvester.action.ActionSequence;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * <br> Get prefixes<br><br>
 *
 * Applying static harvesting means that before getting the prefixes, the
 * static contents needs to be fetched from the provider. In fact, in static
 * harvesting, this is all that will be available. Because the records are
 * in this document also, it needs to be passed to other applications of the
 * protocol.<br><br>
 *
 * Note: this class defines what is different from fetching prefixes from a non
 * static endpoint.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class StaticPrefixHarvesting extends PrefixHarvesting {

    private static final Logger logger = Logger.getLogger(
            StaticPrefixHarvesting.class);

    /**
     * Associate provider data and actions
     *
     * @param provider the endpoint to address in the request
     * @param actions the actions
     *
     */
    public StaticPrefixHarvesting(StaticProvider provider, ActionSequence actions) {
        super(provider, actions);
    }

    /**
     * <br> Try to obtain the static content
     *
     * @return false if there was an error, true otherwise
     */
    @Override
    public boolean request() {

        // need to restart parsing
        index = 0;

        logger.debug("Finding prefixes for format " + actions.getInputFormat());

        if (!(provider instanceof StaticProvider)) {
            // provider is not a provider of static content
            logger.error("Protocol error"); return false;
        } else {
            StaticProvider p = (StaticProvider) provider;

            if (p.getResponse() != null) {
                // already fetched the static content
                return true;
            } else {
                // content not yet there, try to fetch it
                try {
                    response = new Identify(provider.getOaiUrl());
                } catch (IOException
                        | ParserConfigurationException
                        | SAXException
                        | TransformerException e) {
                    logger.error(e.getMessage(), e);
                    logger.info("Cannot get an identification from the static " +
                            provider.getOaiUrl() + " endpoint");
                    return false;
                }
                // response contains static content, store it at provider level
                p.setResponse(response);

                return true;
            }
        }
    }

    /**
     * <br> Get the prefixes from the static content
     *
     * @return false if there was an error, true otherwise
     */
    public boolean processResponse() {

        // check for protocol errors

        if (!(provider instanceof StaticProvider)) {
            // no static provider
            logger.error("Protocol error"); return false;
        } else {
            StaticProvider p = (StaticProvider) provider;

            try {
                nodeList = (NodeList) provider.xpath.evaluate(
                        "//*[local-name() = 'metadataFormat']",
                        response.getDocument(),
                        XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                // could not extract metadata prefixes from the static content.
                logger.error(e.getMessage(), e);
                logger.info("Cannot obtain " + actions.getInputFormat() +
                        " metadata prefixes for from endpoint " + p.oaiUrl);
                return false;
            }

            // now the nodeList contains a list of prefixes
            return true;
        }
    }
}


