package nl.mpi.oai.harvester.protocol;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
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

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This class represents a single processing thread in the harvesting actions
 * workflow. In practice one worker takes care of one provider. The worker
 * applies a scenario for harvesting
 *
 * @author Vic Ding (HUC/DI KNAW)
 */
public class NdeProtocol extends Protocol {
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

    /* Harvesting scenario to be applied. ListIdentifiers: first, based on
       endpoint data and prefix, get a list of identifiers, and after that
       retrieve each record in the list individually. ListRecords: skip the
       list, retrieve multiple records per request.
     */
    private final String scenarioName;

    // kj: annotate
    Endpoint endpoint;

    /**
     * Associate a provider and action actionSequences with a scenario
     *
     * @param provider OAI-PMH provider that this thread will harvest
     * @param cycle    the harvesting cycle
     */
    public NdeProtocol(Provider provider, Configuration config, Cycle cycle) {
        super(provider, config, cycle);

        this.config = config;

        this.provider = provider;

        this.actionSequences = config.getActionSequences();

        // register the endpoint with the cycle, kj: get the group
        this.endpoint = cycle.next(provider.getOaiUrl(), "group");

        // get the name of the scenario the worker needs to apply
        this.scenarioName = provider.getScenario();
    }

    @Override
    public void run() {
        logger.info("Welcome to NDE Harvest Manager worker!");
        provider.init("nde");
        Thread.currentThread().setName(provider.getName().replaceAll("[^a-zA-Z0-9\\-\\(\\)]", " "));

        // setting specific log filename
        ThreadContext.put("logFileName", Util.toFileFormat(provider.getName()).replaceAll("/", ""));

        // TODO: what is map doing?
//        String map = config.getMapFile();
//        synchronized (map) {
//            PrintWriter m = null;
//            try {
//                m = new PrintWriter(new FileWriter(map, true));
//                if (config.hasRegistryReader()) {
//                    m.println(config.getRegistryReader().endpointMapping(provider.getOaiUrl(), provider.getName()));
//                } else {
//                    m.printf("%s,%s,,", provider.getOaiUrl(), Util.toFileFormat(provider.getName()).replaceAll("/", ""));
//                    m.println();
//                }
//            } catch (IOException e) {
//                logger.error("failed to write to the map file!", e);
//            } finally {
//                if (m != null)
//                    m.close();
//            }
//        }
        // TODO: ??

        boolean done = false;

        logger.info("Processing provider[" + provider + "] " +
                "using scenario[" + scenarioName + "], " +
                "incremental[" + provider.getIncremental() + "], " +
                "timeout[" + provider.getTimeout() + "] " +
                "and retry[count=" + provider.getMaxRetryCount() + ", " +
                "delays=" + Arrays.toString(provider.getRetryDelays()) + "]"
        );

        FileSynchronization.addProviderStatistic(provider);

        String queryString = config.getQuery();
        // transforming queryString as the original is escaped
        Map<String, XdmValue> vars = new HashMap<>();
        vars.put("provider-url", new XdmAtomicValue(provider.getOaiUrl()));
        try {
            queryString = Saxon.avt(queryString, Saxon.wrapNode(config.getDoc()), vars);
        } catch (SaxonApiException e) {
            throw new RuntimeException(e);
        }

        // query
        HttpResponse<String> response;
        try {
            response = Unirest.post(config.getQueryEndpoint())
                    .header("accept", "application/sparql-results+xml")
                    .field("query", queryString)
                    .asString();
            logger.info("Query run successfully!");
        } catch (UnirestException e) {
            logger.error("cannot get result back as string");
            throw new RuntimeException(e);
        }

        logger.info("Querying " + config.getQueryEndpoint() + " with query: " + queryString);
        logger.info(response.getStatus() + " " + response.getStatusText());
        // load xml string as doc
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc = null;
        try {
            logger.info("Getting response body...");
            InputStream in = response.getRawBody();
            File temp = new File("/app/workdir/temp.txt");
            FileOutputStream out = new FileOutputStream(temp);
            org.apache.commons.io.IOUtils.copy(in,out,1000000);
            out.close();
//            builder = factory.newDocumentBuilder();
//            doc = builder.parse(new InputSource(new StringReader(response.getBody())));
        } catch (Throwable e) {
            logger.info("Error querying " + config.getQueryEndpoint() + " with query: " + queryString);
            logger.info(response.getStatus() + " " + response.getStatusText());
            logger.info(e.getMessage(), e);
            throw new RuntimeException(e);
//            logger.info(e.getStackTrace());
        }

        // apply the action seq
        logger.info("Size of actionSequences is: " + actionSequences.size());
        for (final ActionSequence actionSequence : actionSequences) {

            actionSequence.runActions(new Metadata(provider.getName(), "nde", doc, provider, true, true));
            logger.info("Action sequence is: " + actionSequence.toString());
        }
    }
}
