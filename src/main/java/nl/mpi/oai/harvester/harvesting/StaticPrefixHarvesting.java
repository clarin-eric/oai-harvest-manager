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
 * <br> Get prefixes <br><br>
 *
 * <br> Get the prefixes supported by a static endpoint <br><br>
 *
 * The methods in the class establish a protocol that fits a scenario for
 * prefix fetching. Because a static endpoint can only present its content
 * in one way only, a request for prefixes results in a response that contains
 * all the data the endpoint contains. <br><br>
 *
 * Strictly speaking, after invoking the request method in this class, the
 * request method in the StaticRecordListHarvesting class does not need to
 * be invoked. The response carrying the content will be passed to the
 * StaticProvider class object for reference by a StaticRecordListHarvesting
 * object. <br><br>
 *
 * After invoking the request method, the processResponse and parseResponse
 * methods need to be invoked. Once the fullyParsed method returns false, all
 * the prefixes will have been returned by subsequent invocations of the
 * parseResponse method.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 *         xpath parsing by Lari Lampen (MPI-PL)
 */
public final class StaticPrefixHarvesting extends FormatHarvesting
        implements Harvesting {

    private static final Logger logger = Logger.getLogger(
            StaticPrefixHarvesting.class);

    /**
     * Associate provider data and actions
     *
     * @param provider the endpoint to address in the request
     * @param actions the actions
     *
     */
    public StaticPrefixHarvesting(OAIFactory oaiFactory,
                                  StaticProvider provider, ActionSequence actions) {

        super(oaiFactory, provider, actions);

        // invariant: the provider is a StaticProvider class object
    }

    /**
     * <br> Try to obtain the static content
     *
     * @return false if there was an error, true otherwise
     */
    @Override
    public boolean request() {

        logger.debug("Finding prefixes for format " + actions.getInputFormat());

        /* Provider is a StaticProvider class object, please refer to the
           constructor.
         */
        StaticProvider p = (StaticProvider) provider;
        response = p.getResponse();

        if (response != null) {
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
                logger.info("Cannot get content from the static " +
                        provider.getOaiUrl() + " endpoint");
                return false;
            }
            // response contains static content, store it in the provider level
            p.setResponse(response);

            return true;
        }
    }

    /**
     * <br> Get the prefixes from the static content
     *
     * @return false if there was an error, true otherwise
     */
    public boolean processResponse() {

        /* Provider is a StaticProvider class object, please refer to the
           constructor.
         */
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

        /* Now the nodeList contains a list of prefixes. The superclass
           methods can process this list.
         */
        return true;
    }
}


