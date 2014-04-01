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
 * <http://www.gnu.org/licenses/>.
 */

package nl.mpi.oai.harvester;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * This class represents the settings of the application as defined in its
 * configuration file (and, optionally, using command line parameters).
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class Configuration {
    private static final Logger logger = Logger.getLogger(Configuration.class);
    private final XPath xpath;

    /**
     * Configuration options stored as key-value pairs.
     */
    private Map<String, String> settings;

    /**
     * Output files stored in a map using id as key.
     */
    private Map<String, OutputDirectory> outputs;

    /** All defined action sequences, in order of preference. */
    private List<ActionSequence> actionSequences;

    /**
     * All OAI-PMH providers (whether defined in configuration or read from
     * centre registry).
     */
    private List<Provider> providers;

    /**
     * List of names of known configuration options.
     */
    public enum KnownOptions {
	WORKDIR("workdir"), RETRYCOUNT("max-retry-count"),
	RETRYDELAY("retry-delay"), MAXJOBS("max-jobs"),
	POOLSIZE("resource-pool-size"), TIMEOUT("timeout");
	private final String val;
	private KnownOptions(final String s) { val = s; }
	public String toString() { return val; }
    }

    /**
     * Create a new configuration object based on a configuration file.
     * 
     * @param filename configuration file
     */
    public Configuration(String filename) throws ParserConfigurationException,
	    SAXException, XPathExpressionException, IOException{
	XPathFactory xpf = XPathFactory.newInstance();
	xpath = xpf.newXPath();

	readConfig(filename);
    }

    /**
     * Actually read the configuration file.
     * 
     * @param filename configuration file
     */
    private void readConfig(String filename) throws ParserConfigurationException,
	    SAXException, XPathExpressionException, IOException {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(filename);

	logger.debug("Reading: settings");
	// ----- Read configuration options -----
	parseSettings((Node) xpath.evaluate("/config/settings",
		doc.getDocumentElement(), XPathConstants.NODE));

	logger.debug("Reading: outputs");
	// ----- Read list of outputs -----
	parseOutputs((Node) xpath.evaluate("/config/directories",
		doc.getDocumentElement(), XPathConstants.NODE));

	logger.debug("Reading: actions");
	// ----- Read list of actions -----
	parseActions((Node) xpath.evaluate("/config/actions",
		doc.getDocumentElement(), XPathConstants.NODE));

	logger.debug("Reading: providers");
	// Some provider names are fetched over the network, so a reasonable
	// timeout should be set here.
	setTimeout(10);

	// ----- Read list of providers -----
	parseProviders((Node) xpath.evaluate("/config/providers",
		doc.getDocumentElement(), XPathConstants.NODE));

	// Apply configured timeout, overriding our temporary value.
	applyTimeoutSetting();

	logger.debug("Finished reading config");
    }

    /**
     * Parse the settings section only.
     * 
     * @param base top node of the settings section
     */
    private void parseSettings(Node base) throws XPathExpressionException {
	settings = new HashMap<>();

	for (KnownOptions x : KnownOptions.values()) {
	    String opt = x.toString();
	    Node curr = (Node) xpath.evaluate(opt,
		    base, XPathConstants.NODE);
	    if (curr == null) {
		logger.warn("Config file has no value for " + opt
			+ ". This may be an error. Continuing anyway.");
		continue;
	    }
	    String text = curr.getTextContent();
	    if (text != null && !text.isEmpty()) {
		settings.put(opt, text);
	    }
	}
    }

    /**
     * Parse the outputs section only.
     * 
     * @param base top node of the outputs section
     */
    private void parseOutputs(Node base) throws XPathExpressionException,
	    IOException {
	outputs = new HashMap<>();
	NodeList nodeList = (NodeList) xpath.evaluate("./dir", base,
		XPathConstants.NODESET);
	Path workDir = Paths.get(getWorkingDirectory());
        for (int i=0; i<nodeList.getLength(); i++) {
            Node curr=nodeList.item(i);
            String path = Util.getNodeText(xpath, "./@path", curr);
            String id = Util.getNodeText(xpath, "./@id", curr);
            String maxString = Util.getNodeText(xpath, "./@max-files", curr);
	    int max = (maxString==null) ? 0 : Integer.valueOf(maxString);
	    OutputDirectory od = new OutputDirectory(workDir.resolve(path), max);

	    if (outputs.containsKey(id)) {
		logger.error("Configuration file defines several files with id "
			+ id + ". Please fix it.");
		continue;
	    }
	    outputs.put(id, od);
        }
    }

    /**
     * Parse the actions section only.
     * 
     * @param base top node of the actions section
     */
    private void parseActions(Node base) throws XPathExpressionException {
	actionSequences = new ArrayList<>();
	NodeList nodeList = (NodeList) xpath.evaluate("./format", base,
		XPathConstants.NODESET);
        for (int i=0; i<nodeList.getLength(); i++) {
            Node curr = nodeList.item(i);
            String matchType = Util.getNodeText(xpath, "./@match", curr);
            String matchValue = Util.getNodeText(xpath, "./@value", curr);
	    MetadataFormat format = new MetadataFormat(matchType, matchValue);

	    NodeList actions = (NodeList) xpath.evaluate("./action", curr,
		    XPathConstants.NODESET);
	    if (actions != null && actions.getLength() > 0) {
		ArrayList<Action> ac = new ArrayList<>();
		for (int k=0; k<actions.getLength(); k++) {
		    Node s = actions.item(k);
		    String actionType = Util.getNodeText(xpath, "./@type", s);
		    Action act = null;
		    if ("strip".equals(actionType)) {
			try {
			    act = new StripAction();
			} catch (ParserConfigurationException ex) {
			    logger.error(ex);
			}
		    } else if ("save".equals(actionType)) {
			String outDirId = Util.getNodeText(xpath, "./@dir", s);
			String suffix = Util.getNodeText(xpath, "./@suffix", s);
			if (outputs.containsKey(outDirId)) {
			    OutputDirectory outDir = outputs.get(outDirId);
			    String group = Util.getNodeText(xpath,
				    "./@group-by-provider", s);
			    // If the group-by-provider attribute is
			    // not defined, it defaults to true.
			    if (group != null && !Boolean.valueOf(group)) {
				act = new SaveAction(outDir, suffix);
			    } else {
				act = new SaveGroupedAction(outDir, suffix);
			    }
			} else {
			    logger.error("Invalid output directory " + outDirId
				    + " specified for save action");
			}
		    } else if ("transform".equals(actionType)) {
			String xslFile = Util.getNodeText(xpath, "./@file", s);
			try {
			    act = new TransformAction(xslFile);
			} catch (FileNotFoundException | TransformerConfigurationException ex) {
			    logger.error(ex);
			}
		    }
		    if (act != null)
			ac.add(act);
		}
		ActionSequence ap = new ActionSequence(format,
			ac.toArray(new Action[ac.size()]),
			getResourcePoolSize());
		actionSequences.add(ap);
	    } else {
		logger.warn("A format has no actions defined; skipping it");
	    }
        }
    }

    /**
     * Parse the providers section only. Included reading from the registry
     * if required.
     * 
     * @param base top node of the providers section
     */
    private void parseProviders(Node base) throws XPathExpressionException,
	    MalformedURLException {
	providers = new ArrayList<>();

	Node registry = (Node) xpath.evaluate("./import-registry", base,
		XPathConstants.NODE);
	if (registry != null) {
	    String regUrl = Util.getNodeText(xpath, "./@url", registry);
	    RegistryReader rr = new RegistryReader();
		    
	    List<String> provUrls = rr.getEndpoints(new java.net.URL(regUrl));
	    for (String provUrl : provUrls) {
		Provider provider = new Provider(provUrl, getMaxRetryCount());
		providers.add(provider);
	    }
	}

	NodeList prov = (NodeList) xpath.evaluate("./provider", base,
		XPathConstants.NODESET);
	for (int j=0; j<prov.getLength(); j++) {
	    Node cur = prov.item(j);
	    String pName = Util.getNodeText(xpath, "./@name", cur);
	    String pUrl = Util.getNodeText(xpath, "./@url", cur);
	    String pStatic = Util.getNodeText(xpath, "./@static", cur);

	    if (pUrl == null) {
		logger.error("Skipping provider " + pName + ": URL is missing");
		continue;
	    }

	    Provider provider;
	    if (Boolean.valueOf(pStatic)) {
		provider = new StaticProvider(pUrl);
		if (pName != null)
		    provider.setName(pName);
	    } else {
		provider = new Provider(pUrl, getMaxRetryCount());
		if (pName != null)
		    provider.setName(pName);

		// Note: static providers do not support sets, so this only
		// needs to be done here.
		NodeList sets = (NodeList) xpath.evaluate("./set", cur,
			XPathConstants.NODESET);
		if (sets != null && sets.getLength() > 0) {
		    ArrayList<String> setSpec = new ArrayList<>();
		    for (int k=0; k<sets.getLength(); k++) {
			Node s = sets.item(k);
			setSpec.add(s.getTextContent());
		    }
		    provider.setSets(setSpec.toArray(new String[setSpec.size()]));
		}
	    }

	    providers.add(provider);
	}
    }

    public List<Provider> getProviders() {
	return providers;
    }

    public List<ActionSequence> getActionSequences() {
	return actionSequences;
    }

    /**
     * Set a configuration option, overriding the previous value if there
     * was one.
     * 
     * @param key the option name (must be one of known values)
     * @param value the value to be set
     */
    public void setOption(String key, String value) {
	for (KnownOptions x : KnownOptions.values()) {
	    String opt = x.toString();
	    if (opt.equals(key)) {
		settings.put(key, value);
		return;
	    }
	}
	logger.warn("Ignoring attempt to set unknown parameter " + key);
    }

    // The default value for each option is set in its respective
    // getter function below.
    public String getWorkingDirectory() {
        String s = settings.get(KnownOptions.WORKDIR.toString());
        if (s == null)
	    return "workspace";
        return s;
    }
    public int getMaxRetryCount() {
        String s = settings.get(KnownOptions.RETRYCOUNT.toString());
        if (s == null) return 1;
        return Integer.valueOf(s);
    }
    public int getRetryDelay() {
        String s = settings.get(KnownOptions.RETRYDELAY.toString());
        if (s == null) return 0;
        return Integer.valueOf(s);
    }
    public int getMaxJobs() {
        String s = settings.get(KnownOptions.MAXJOBS.toString());
        if (s == null) return 1;
        return Integer.valueOf(s);
    }
    public int getResourcePoolSize() {
        String s = settings.get(KnownOptions.POOLSIZE.toString());
        // Note that the default value is not fixed; instead, it is equal
	// to the maximum number of jobs.
        if (s == null) return getMaxJobs();
        return Integer.valueOf(s);
    }

    /**
     * Set connection properties to reflect configured connection
     * timeout (if there is one).
     */
    public void applyTimeoutSetting() {
        String s = settings.get(KnownOptions.TIMEOUT.toString());
	int timeout = (s==null) ? 0 : Integer.valueOf(s);
	setTimeout(timeout);
    }

    /**
     * Set network timeout to the specified number of seconds.
     * 
     * @param sec timeout in seconds (if 0 or negative, will disable timeout)
     */
    private void setTimeout(int sec) {
	String t = (sec > 0) ? String.valueOf(sec*1000) : "-1";

	// NOTE: This is specific to the Sun implementation of URL
	// connections. It works in Sun JDK and OpenJDK, but not
	// everywhere (e.g. not on Dalvik).
	System.setProperty("sun.net.client.defaultReadTimeout", t);
	System.setProperty("sun.net.client.defaultConnectTimeout", t);	    
    }

    /**
     * Log parsed contents of the configuration.
     */
    void log() {
	logger.info("--- configuration options ---");
	for (Map.Entry<String, String> me : settings.entrySet()) {
	    logger.info("  " + me.getKey() + " --> " + me.getValue());
        }

	logger.info("--- list of outputs ---");
	for (Map.Entry<String, OutputDirectory> me : outputs.entrySet()) {
	    logger.info("  " + me.getKey() + " --> " + me.getValue());
        }

	logger.info("--- list of action sequences ---");
	for (ActionSequence act : actionSequences) {
	    logger.info("  " + act);
	}

	logger.info("--- list of providers ---");
	for (Provider prov : providers) {
	    logger.info("  " + prov);
	}
    }
}
