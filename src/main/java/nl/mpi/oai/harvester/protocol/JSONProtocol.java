package nl.mpi.oai.harvester.protocol;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.control.Configuration;
import nl.mpi.oai.harvester.control.FileSynchronization;
import nl.mpi.oai.harvester.control.Util;
import nl.mpi.oai.harvester.cycle.Cycle;
import nl.mpi.oai.harvester.cycle.Endpoint;
import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.tla.util.Saxon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a single processing thread in the harvesting actions
 * workflow. In practice one worker takes care of one provider. The worker
 * applies a scenario for harvesting
 *
 * @author Meindert Kroese HUC/DI KNAW)
 */
public class JSONProtocol extends Protocol {
    private final Logger logger = LogManager.getLogger(this.getClass());
    /**
     * The configuration
     */
    private final Configuration config;

    /**
     * The provider this worker deals with.
     */
    private final Provider provider;

    /**
     * List of actionSequences to be applied to the harvested metadata.
     */
    private final List<ActionSequence> actionSequences;

    Endpoint endpoint;

    /**
     * Associate a provider and action actionSequences with a scenario
     *
     * @param provider OAI-PMH provider that this thread will harvest
     * @param cycle    the harvesting cycle
     */
    public JSONProtocol(Provider provider, Configuration config, Cycle cycle) {
        super(provider, config, cycle);

        this.config = config;

        this.provider = provider;

        this.actionSequences = config.getActionSequences();

        // register the endpoint with the cycle, kj: get the group
        this.endpoint = cycle.next(provider.getOaiUrl(), "group");
    }

    @Override
    public void run() {
        logger.info("Welcome to JSON Harvest Manager worker!");
        provider.init("json");
        Thread.currentThread().setName(provider.getName().replaceAll("[^a-zA-Z0-9\\-\\(\\)]", " "));

        // setting specific log filename
        ThreadContext.put("logFileName", Util.toFileFormat(provider.getName()).replaceAll("/", ""));


        boolean done = false;

        logger.info("Processing JSON provider[" + provider + "] " +
                "timeout[" + provider.getTimeout() + "] " +
                "and retry[count=" + provider.getMaxRetryCount() + ", " +
                "delays=" + Arrays.toString(provider.getRetryDelays()) + "]"
        );

        // query
        HttpResponse<String> response;
        try {
            response = Unirest.get(provider.getOaiUrl())
                    .header("accept", "application/json")
                    .asString();
            logger.info("Fetch run successfully!");
        } catch (UnirestException e) {
            logger.error("cannot get result back as string");
            throw new RuntimeException(e);
        }

        // load xml string as doc
        Document doc = null;
        try {
            logger.info("before parseJson:"+response.getBody());
            XdmNode node = Saxon.parseJson(response.getBody());
            logger.info("after parseJson:"+node);
            // TODO: make from an XdmNode a Node
            StringReader r = new StringReader(node.toString());
            logger.info("after StringReader:"+r);
            InputSource i = new InputSource(r);
            logger.info("after InputSource:"+i);
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(i);
            //Node n = Saxon.unwrapNode(node);
            //logger.info("after unwrapNode:"+n);
            //doc = n.getOwnerDocument();
            logger.info("after DocumentBuilder:"+doc);
//            doc = Saxon.unwrapNode(Saxon.parseJson(response.getBody())).getOwnerDocument();
        } catch (Exception e) {
            logger.error("parseJson failed:"+e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        logger.info("parseJson ran successfully!");

        // apply the action seq
        logger.info("Size of actionSequences is: " + actionSequences.size());
        for (final ActionSequence actionSequence : actionSequences) {

            logger.info("Action sequence is: " + actionSequence.toString());
            actionSequence.runActions(new Metadata(provider.getName(), "json", doc, provider, true, true));
        }
    }
}
