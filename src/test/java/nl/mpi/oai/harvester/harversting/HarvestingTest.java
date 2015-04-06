package nl.mpi.oai.harvester.harversting;

import ORG.oclc.oai.harvester2.verb.ListMetadataFormats;
import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.harvesting.FormatHarvesting;
import nl.mpi.oai.harvester.metadata.MetadataFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
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
 * None of the tests in this package will utilise the OAI protocol. The tests
 * will run without connecting to any endpoint. The response that in the real
 * world will be obtained from endpoint, come from the XML files in the
 * resources folder.
 *
 * The files contain endpoint responses in XML format. By comparing the results
 * of the processing by the methods in the harvesting package, to what is
 * expected given the contents of the mocked responses, the test verify the
 * classes in the package.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
@RunWith(MockitoJUnitRunner.class)
public class HarvestingTest {

    /**
     * Get a mocked response from a file
     *
     * @param file containing and endpoint response in XML form
     * @return the response in document form
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
        // return the document contained in the file
        return db.parse(getClass().getResourceAsStream(file.getAbsolutePath()));
    }

    /* When not testing, the ListMetadataFormats constructor will return the
       formats supported by the endpoint. Since the test does not apply the OAI
       protocol, and therefore does not connect to any endpoint, mock the
       response.
     */
    @Mock ListMetadataFormats response;

    @Test
    /**
     * Test format harvesting without connecting to an endpoint
     */
    public void formatHarvestingTest() throws IOException,
            SAXException,
            ParserConfigurationException {

        // create a real metadata format
        // MetadataFormat format = new MetadataFormat("prefix", "oai_dc");
        MetadataFormat format = new MetadataFormat("namespace", "http://www.clarin.eu/cmd/");

        // Since the test only needs the input format, and not a fully fledged
        // action sequence, mock the sequence
        ActionSequence actionSequence = mock(ActionSequence.class);
        // let the getInputFormat method return the metadata format defined
        when (actionSequence.getInputFormat()).thenReturn(format);

        // create a real provider
        Provider provider = new Provider("http://www.endpoint.org", 0);

        // spy on format harvesting
        FormatHarvesting formatHarvesting = spy (new FormatHarvesting(
                provider, actionSequence));

        /* Whenever the formatHarvesting Request method would invoke the
           constructor of the ListMetadataFormats class, return the mocked
           response. This object will not be referred to.
         */
        try {
            doReturn(response).when(formatHarvesting).getResponse(any(String.class));
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        // the FormatHarvesting class is now set up for spying
        boolean done;
        done = formatHarvesting.request();
        if (! done){
            fail();
        }

        // get a document from a file
        File testFile = new File ("/response-ListMetadataFormats.xml");
        Document testDoc = getDocumentFromFile(testFile);

        /* Because we do not know what a response would look like, mock the
           getResponse() method by returning the document containing the
           response.
         */
        doReturn(testDoc).when(formatHarvesting).getResponse();

        // get the response
        Document document = formatHarvesting.getResponse();

        /* All the necessary mocks have been defined. Invoke any of the
           remaining methods in the FormatHarvesting class to create any
           test desired.
         */

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
        assertEquals(prefixes.toString(),"[oai_dc]");
    }
}
