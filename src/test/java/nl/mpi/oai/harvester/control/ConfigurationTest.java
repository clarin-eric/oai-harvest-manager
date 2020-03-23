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
import java.io.Reader;
import java.util.function.Function;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ConfigurationTest {

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationTest.class);

    @Rule
    public TemporaryFolder workdir = new TemporaryFolder();

    private final Function<String, String> testConfigFilter = (String line) -> {
        assert (line != null);
        return line.replaceAll("\\{\\{workdir\\}\\}", workdir.getRoot().getAbsolutePath());
    };

    public ConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of readConfig method, of class Configuration.
     */
    @Test
    public void testReadConfig() throws Exception {
        final String filename = pathForResource("/config/test-config-basic.xml");
        final Configuration instance = new Configuration();
        instance.readConfig(filename);
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
