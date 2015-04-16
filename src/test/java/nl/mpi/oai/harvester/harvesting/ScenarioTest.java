
/*
 * Copyright (C) 2015, The Max Planck Institute for
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

package nl.mpi.oai.harvester.harvesting;

import ORG.oclc.oai.harvester2.verb.ListMetadataFormats;
import ORG.oclc.oai.harvester2.verb.ListRecords;
import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.metadata.MetadataFactory;
import nl.mpi.oai.harvester.metadata.MetadataFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the harvesting scenario class <br><br>
 *
 * This tests class provides tests that provide the metadata records defined
 * in the extensions of the test helper class to the harvesting interface. It
 * does this by applying mocking and spying made available by Mockito. By
 * relying on the test helper class, it compares the records defined in the
 * extensions with the results the harvesting scenario yields. <br><br>
 *
 * While the test, by spying, passes mocked OAI records to real methods in the
 * harvesting package, this package needs to provide the resulting metadata
 * records to the test helper. This is necessary because the test helper needs
 * to compare the predefined input to the generated output. To this end the
 * harvesting classes use a metadata factory instead of creating the metadata
 * themselves.<br><br>
 *
 * Note: doReturn().when() differs from when().thenReturn() in that in the
 * when applying the second of the two the method indicated is effectively
 * invoked while in the case of the first expression it is not.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
@RunWith(MockitoJUnitRunner.class) // initialise @Mock annotated mocks
public class ScenarioTest {

    /* When not testing, the ListMetadataFormats constructor will return the
       formats supported by the endpoint. Since the test does not apply the OAI
       protocol, and therefore does not connect to any endpoint, mock the
       formats.
    */
    @Mock
    ListMetadataFormats formats;

    // similarly, when listing records mock the response
    @Mock
    ListRecords records;

    @Test
    /**
     * Follow the list records scenario
     */
    public void ListRecords() {

        // create a real metadata format object
        MetadataFormat format = new MetadataFormat("namespace",
                "http://www.clarin.eu/cmd/");

        /* Since the test only needs the input format, and not a fully fledged
           action sequence, mock the sequence.
         */
        ActionSequence actionSequence = mock(ActionSequence.class);
        // let the getInputFormat method return the metadata format defined
        when(actionSequence.getInputFormat()).thenReturn(format);
        when(actionSequence.containsStripResponse()).thenReturn(true);

        // create test helper
        ListRecordsTestHelper helper = new ListRecordsTestHelper();

        // get the first provider from the helper
        Provider endpoint = helper.getFirstEndpoint();
        for (;;){

            // create the scenario with the endpoint and the action sequence
            Scenario scenario = new Scenario(endpoint, actionSequence);

            // spy on format harvesting
            FormatHarvesting formatHarvesting = spy (new FormatHarvesting(
                    endpoint, actionSequence));

             /* Whenever the request method would invoke the ListMetadataFormats
                constructor, mock its response.
              */
            try {
                doReturn(formats).when(formatHarvesting).getMetadataFormats(
                        any(String.class));
            } catch (TransformerException
                    | ParserConfigurationException
                    | SAXException
                    | IOException e) {
                e.printStackTrace();
            }

            /* Replacing the real responses, whenever getResponse() is invoked,
               let the helper return a document containing the prefixes.
             */
            doReturn(helper.getDocument("FormatLists")).when(
                    formatHarvesting).getResponse();

            /* Now, safely invoke the scenario get the prefixes by invoking the
               scenario.
             */
            List<String> prefixes = scenario.getPrefixes(formatHarvesting);

            // spy on the metadata factory also
            MetadataFactory metadataFactory = spy (new MetadataFactory());
            // doReturn(helper).when(factory.connectInterface());
            when(metadataFactory.connectInterface()).thenReturn(helper);

            // finally, define spying on record list harvesting
            RecordListHarvesting recordListHarvesting = spy (
                    new RecordListHarvesting(endpoint, prefixes, metadataFactory));

            // like in the case of format harvesting, again, mock the response
            try {
                doReturn(records).when(recordListHarvesting).verb2(
                        any(String.class), any(String.class));
            } catch (TransformerException
                    | ParserConfigurationException
                    | SAXException
                    | IOException
                    | NoSuchFieldException e) {
                e.printStackTrace();
            }

            /* Replacing the real responses, whenever getResponse() is invoked,
               let the helper return a document containing the prefixes.
             */
            doReturn(helper.getDocument("RecordLists")).when(
                    recordListHarvesting).getResponse();

            /* Now, the mocking and spying setup is done. Follow the record list
               harvesting scenario.
             */
            scenario.listRecords(recordListHarvesting);

            // switch to the next endpoint
            endpoint = helper.getNextEndpoint();
            if (endpoint == null) {
                break;
            }
        }

        // determine if the test was successful
        if (!helper.success()) {
            fail();
        }
    }

    @Test
    public void ListIdentifiers() {
        // kj: complete this
    }
}
