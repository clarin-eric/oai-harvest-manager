/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.oai.harvester.control;

import com.google.common.base.Charsets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;
import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.action.ActionSequence;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ConfigurationTest {

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationTest.class);
    private static final String BASIC_CONFIG_RESOURCE = "/config/test-config-basic.xml";
    
    private static Configuration BASIC_CONFIG;
    
    @Rule
    public TemporaryFolder workdir = new TemporaryFolder();

    private final Function<String, String> testConfigFilter = (String line) -> {
        assert (line != null);
        return line.replaceAll("\\{\\{workdir\\}\\}", workdir.getRoot().getAbsolutePath());
    };

    @Test
    public void testReadConfig() throws Exception {
        final Configuration config = getBasicConfig();
        assertNotNull(config);
    }
    
    @Test
    public void testBasicProps() throws Exception {
        final Configuration config = getBasicConfig();
        assertEquals(false, config.isDryRun());
        assertEquals(false, config.isIncremental());
    }
    
    @Test
    public void testActionSequences() throws Exception {
        final List<ActionSequence> actionSequences = getBasicConfig().getActionSequences();
        
        assertNotNull(actionSequences);
        assertEquals(4, actionSequences.size());
        assertEquals("namespace", actionSequences.get(0).getInputFormat().getType());
        assertEquals("http://www.clarin.eu/cmd/1", actionSequences.get(0).getInputFormat().getValue());
    }
    
    @Test
    public void testProviders() throws Exception {
        final List<Provider> providers = getBasicConfig().getProviders();
        assertNotNull(providers);
        assertEquals(1, providers.size());
        assertEquals("https://www.meertens.knaw.nl/flat/oai2", providers.get(0).getOaiUrl());
    }
    
    private Configuration getBasicConfig() throws Exception {
        synchronized(ConfigurationTest.class) {
            if(BASIC_CONFIG == null) {
                BASIC_CONFIG = readConfig(BASIC_CONFIG_RESOURCE);
            }
        }
        return BASIC_CONFIG;
    }
    
    private Configuration readConfig(String name) throws Exception {
        final String filename = pathForResource(name);
        final Configuration config = new Configuration();
        config.readConfig(filename);
        return config;
    }

    private String pathForResource(String resource) {
        return fileForResource(resource, testConfigFilter).getAbsolutePath();
    }

    private File fileForResource(String resource, Function<String, String> filter) {
        try (InputStream stream = getClass().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Resource not found");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8.name()))) {
                final File tempFile = File.createTempFile(getClass().getName(), ".xml");
                tempFile.deleteOnExit();

                logger.debug("Resource {} -> file on disk {}", resource, tempFile.getAbsoluteFile());

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(filter.apply(line));
                        writer.newLine();
                    }
                    return tempFile;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error while reading resource for test: " + resource, ex);
        }
    }
}
