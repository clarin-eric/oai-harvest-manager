package nl.mpi.oai.harvester.harversting;

import ORG.oclc.oai.harvester2.verb.HarvesterVerb;
import ORG.oclc.oai.harvester2.verb.ListMetadataFormats;
import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.harvesting.FormatHarvesting;
import nl.mpi.oai.harvester.metadata.MetadataFormat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * <br> Tests targeting the harvesting package <br><br>
 *
 * Trying out mockito
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
@RunWith(MockitoJUnitRunner.class)
public class HarvestingTest {

    @BeforeClass
    public static void beforeAll() {
        // mock the Provider
        provider = mock(Provider.class);

        // mock the actionSequence
        actionSequence = mock(ActionSequence.class);
    }

    static Provider provider;
    static ActionSequence actionSequence;

    // set up the ListMetadataFormats class for mocking
    @Mock
    HarvesterVerb response;

    @InjectMocks
    // mock the FormatHarvesting
    FormatHarvesting harvesting = new FormatHarvesting(provider,
            actionSequence);

    /**
     *
     * @param file
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public Document getDocumentFromFile(File file) throws
            ParserConfigurationException,
            IOException,
            SAXException {

        // set up a factory for the document builders
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // set up a document builder
        DocumentBuilder db = dbf.newDocumentBuilder();
        // create a document from the file
        Document document = db.parse(getClass().getResourceAsStream(
                file.getAbsolutePath()));

        return document;
    }

    @Test
    /**
     * Testing the FormatHarvesting class
     */
    public void formatHarvestingTest() throws IOException,
            SAXException,
            ParserConfigurationException {

        // create a metadata format
        MetadataFormat format = new MetadataFormat("prefix", "oai_dc");

        // define the mock for the actionSequence object
        when (actionSequence.getInputFormat()).thenReturn(format);

        // define the mock for the provider
        when(provider.getOaiUrl()).thenReturn("http://www.endpoint.org");

        // get a document from a file
        File testFile = new File ("/response-ListMetadataFormats.xml");
        Document testDoc = getDocumentFromFile(testFile);

        // define the mock for the method in FormatHarvesting method
        when(response.getDocument()).thenReturn(testDoc);

        // now invoke any desired format harvesting method
        boolean done;
        done = harvesting.request();
        if (! done){
            fail();
        }

        done = harvesting.processResponse();
        if (! done){
            fail();
        }

        done = harvesting.fullyParsed();
        if (! done){
            fail();
        }

        final List<String> prefixes = new ArrayList<>();

        for (;;){
            if (harvesting.fullyParsed()) break;
            String prefix = (String) harvesting.parseResponse();
            if (prefix != null) {
                prefixes.add(prefix);
            }
        }

        // compare the list of prefixes to what was is expected

    }
}
