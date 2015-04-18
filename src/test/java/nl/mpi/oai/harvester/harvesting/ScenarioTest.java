
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

import ORG.oclc.oai.harvester2.verb.HarvesterVerb;
import ORG.oclc.oai.harvester2.verb.ListIdentifiers;
import ORG.oclc.oai.harvester2.verb.ListMetadataFormats;
import ORG.oclc.oai.harvester2.verb.ListRecords;
import nl.mpi.oai.harvester.Provider;
import nl.mpi.oai.harvester.action.ActionSequence;
import nl.mpi.oai.harvester.metadata.MetadataFactory;
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
 * kj: or the other way around
 *
 * Note: decrease the reliance on Mockito. The helper allows for testing that
 * deviates from the notion of fixture. Implement an OAI verb interface, and
 * let the helper implement it. In this way it can more elegantly provide the
 * test with data. Next to this, the harvesting package constructors need to
 * accept an OAI verb factory.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
@RunWith(MockitoJUnitRunner.class) // initialise @Mock annotated mocks
public class ScenarioTest {

    /* When not testing, the ListMetadataFormats constructor will return the
       formats supported by the endpoint. Since the test does not apply the OAI
       protocol, and therefore does not connect to any endpoint, mock the
       formats. kj: improve
    */
    ListMetadataFormats formats;

    // similarly, when listing records mock the response
    @Mock
    ListRecords records;

    // similarly, when listing records mock the response
    @Mock
    ListIdentifiers identifiers;

    /**
     *
     */
    @Test
    public void listRecordsTest(){

        // create a helper
        ListRecordsTestHelper helper = new ListRecordsTestHelper();
        // do the test
        try {
            listHarvestingTest(helper);
        } catch (ParserConfigurationException
                | TransformerException
                | SAXException
                | NoSuchFieldException
                | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    // @Test
    public void listIdentifiersTest(){

        // create a helper
        ListIdentifiersTestHelper helper = new ListIdentifiersTestHelper();
        // do the test
        try {
            listHarvestingTest(helper);
        } catch (ParserConfigurationException
                | TransformerException
                | SAXException
                | NoSuchFieldException
                | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <br> Follow a list scenario <br><br>
     *
     * Note: the scenario followed depends on the helper specified.
     *
     * @param helper helper for the test
     */
     public void listHarvestingTest(ListTestHelper helper) throws
             ParserConfigurationException,
             TransformerException,
             SAXException,
             NoSuchFieldException,
             IOException {

        /* Since the test only needs the input format, and not a fully fledged
           action sequence, mock the sequence.
         */
        ActionSequence sequence = mock(ActionSequence.class);
        // define what the mocked object will return
        when(sequence.getInputFormat()).thenReturn(helper.getMetadataFormat());
        when(sequence.containsStripResponse()).thenReturn(true);

        // get the first provider from the helper
        Provider endpoint = helper.getFirstEndpoint();
        for (; ; ) {

            // create a scenario with the real endpoint and the mocked sequence
            Scenario scenario = new Scenario(endpoint, sequence);

            // spy on format harvesting
            FormatHarvesting formatHarvesting = spy(new FormatHarvesting(
                    endpoint, sequence));

             /* Whenever the request method would invoke the ListMetadataFormats
                constructor, use a mock to divert the response to.
              */
            doReturn(formats).when(
                    formatHarvesting).getMetadataFormats(any(String.class));

            /* Instead of a response, let the helper return a predefined
               document containing a metadata format list.
             */
            doReturn(helper.getDocument("FormatLists")).when(
                    formatHarvesting).getResponse();

            /* Now mocking and spying has been set up, follow the prefix list
               harvesting scenario.
             */
            List<String> prefixes = scenario.getPrefixes(formatHarvesting);

            // check the metadata by spying on it
            MetadataFactory factory = spy(new MetadataFactory());
            // let the helper check the data
            when(factory.connectInterface()).thenReturn(helper);

            if (helper instanceof ListRecordsTestHelper) {

                // spy on record list harvesting
                RecordListHarvesting recordListHarvesting = spy(
                        new RecordListHarvesting(endpoint, prefixes, factory));

                // like in the case of format harvesting, mock the response kj: improve
                doReturn(records).when(recordListHarvesting).verb2(
                        any(String.class), any(String.class));

                /* Instead of a response, let the helper return a predefined
                   document containing a list of record list.
                 */
                doReturn(helper.getDocument("RecordLists")).when(
                        recordListHarvesting).getResponse();

                /* Instead of a resumption token ...

                 */
                doReturn(helper.getResumptionToken()).when(
                        recordListHarvesting).getToken(any(HarvesterVerb.class));

                /* Now, mocking and spying has been set up, follow the record
                   list harvesting scenario.
                */
                scenario.listRecords(recordListHarvesting);

            } else {

                // spy on identifier list harvesting
                IdentifierListHarvesting identifierListHarvesting = spy(
                        new IdentifierListHarvesting(endpoint, prefixes, factory));


                doReturn(identifiers).when(identifierListHarvesting).verb2(
                        any(String.class), any(String.class));

                /* Instead of a response, let the helper return a predefined
                   document containing an identifier list.
                 */
                doReturn(helper.getDocument("IdentifierLists")).when(
                        identifierListHarvesting).getResponse();

                 /* Instead of a resumption token ...

                 */
                doReturn(helper.getResumptionToken()).when(
                        identifierListHarvesting).getToken(any(HarvesterVerb.class));

                /* Now, mocking and spying has been set up, follow the record
                   list harvesting scenario.
                */
                scenario.listIdentifiers(identifierListHarvesting);
            }

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
}
