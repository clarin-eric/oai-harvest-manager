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

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Executable class, main entry point of OAI Harvester.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class Main {
    private static final String sep = System.getProperty("file.separator");
    private static final Logger logger = Logger.getLogger(Main.class);

    /** Object containing entries from configuration file. */
    private static Configuration config;

    private static void runHarvesting(Configuration config) {
	// Ensure the timeout setting is honored.
	// config.applyTimeoutSetting();

	config.log();
	/* create dirRoot/infoFiles if needed */
	// Util.ensureDirExists(config.getSettings().getOrigDir());

	for (Provider prov : config.getProviders()) {
	    logger.info("Processing provider: " + prov);
	    for (ActionSequence as : config.getActionSequences()) {
		// Break the inner loop after the first successful completion
		// of an action sequence.
		if (prov.performActions(as))
		    break;
	    }
	}

	/*
	// ---------- 1. Compile a list of tasks. ----------
	HarvestingTask[] tasks = config.getHarvestingTasks();

	// ---------- 2. Log the task list. ----------
	logger.info("List of tasks to be carried out:");
	for (HarvestingTask task : tasks) {
	    logger.info("    "+task);
	}

	// ---------- 3. Start a thread for each task. ----------
	for (HarvestingTask task : tasks) {
	    HarvestAndTransformationProcess.startHarvesterThread(config,
								 task);
	}

	// ---------- 4. Wait for all the tasks to finish. ----------
	HarvestAndTransformationProcess.waitForThreads();
	*/
    }

    public static Configuration getConfig() {
	return config;
    }

    public static void main(String[] args) {
	String configFile = null;

	// Select Saxon XPath implementation (necessary in case there
        // are other XPath libraries in classpath).
        System.setProperty("javax.xml.xpath.XPathFactory",
		"net.sf.saxon.xpath.XPathFactoryImpl");

	// If the "config" parameter is specified, take it as the
	// configuration file name.
	for (String arg : args) {
	    if (arg.startsWith("config=")) {
		configFile = arg.substring(7);
	    } else if (!arg.contains("=")) {
		configFile = arg;
	    }
	}

	// If a configuration file name hasn't been specified in the
	// arguments, use "resources/config.xml" by default.
	if (configFile == null) {
	    configFile = "resources" + sep + "config.xml";
	}

	// Read the configuration file, then process command line
	// parameters (which may override configuration options).
	try {
	    config = new Configuration(configFile);
	} catch (ParserConfigurationException | SAXException 
		| XPathExpressionException | IOException ex) {
	    logger.error("Unable to read configuration file", ex);
	    return;
	}
	for (String arg : args) {
	    if (arg.indexOf('=') > -1) {
		String[] tmp=arg.split("=");
		if (tmp.length == 1) {
		    config.setOption(tmp[0], null);
		} else if (tmp.length >= 2) {
		    config.setOption(tmp[0], tmp[1]);
		}
	    }
	}
	config.applyTimeoutSetting();

	runHarvesting(config);
    }
}
