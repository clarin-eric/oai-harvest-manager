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
import javax.xml.transform.TransformerException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * <br> Tests targeting the harvesting package <br><br>
 *
 * kj: work on the documentation
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
@RunWith(MockitoJUnitRunner.class)
public class HarvestingTest {

    @BeforeClass
    public static void beforeAll() {
    }

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

    @Mock ListMetadataFormats listMetadataFormats;

    @Test
    /**
     * Testing the FormatHarvesting class
     */
    public void formatHarvestingTest() throws IOException,
            SAXException,
            ParserConfigurationException {

        // create a real metadata format
        MetadataFormat format = new MetadataFormat("prefix", "oai_dc");

        ActionSequence actionSequence = mock(ActionSequence.class);
        // define the mock for the actionSequence object
        when (actionSequence.getInputFormat()).thenReturn(format);

        // create a real provider
        Provider provider = new Provider("http://www.endpoint.org", 0);
        FormatHarvesting formatHarvesting = spy (new FormatHarvesting(
                provider, actionSequence));

        try {
            doReturn(listMetadataFormats).when(formatHarvesting).getResponse(any(String.class));
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        // now invoke any desired format harvesting method
        boolean done;
        done = formatHarvesting.request();
        if (! done){
            fail();
        }

        // get a document from a file
        File testFile = new File ("/response-ListMetadataFormats.xml");
        Document testDoc = getDocumentFromFile(testFile);

        // mock a response
        doReturn(testDoc).when(formatHarvesting).getResponse();

        // get the response
        Document document = formatHarvesting.getResponse();

        // process the response
        done = formatHarvesting.processResponse(document);
        if (! done){
            fail();
        }

        final List<String> prefixes = new ArrayList<>();

        for (;;){
            if (formatHarvesting.fullyParsed()) break;
            String prefix = (String) formatHarvesting.parseResponse();
            if (prefix != null) {
                prefixes.add(prefix);
            }
        }

        // compare the list of prefixes to what was is expected
        assertEquals(prefixes.toString(),"oai_dc");
    }
}
