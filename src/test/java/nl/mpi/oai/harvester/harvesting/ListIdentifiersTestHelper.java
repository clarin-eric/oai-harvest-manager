
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

import nl.mpi.oai.harvester.metadata.MetadataFormat;

/**
 * <br> Fix a test for the list identifiers scenario <br><br>
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class ListIdentifiersTestHelper extends TestHelper {

    @Override
    String getTestName() {

        return "ListIdentifiers";
    }

    @Override
    String[] getEndpointURIs() {
        // a table of endpoint URIs
        final String[] endpointURIs;

        endpointURIs = new String[]{
                "http://metalb.csc.fi/cgi-bin/que"};

        return endpointURIs;
    }

    @Override
    MetadataFormat getMetadataFormats() {

        // return the metadata format for the test
        MetadataFormat metadataFormat = new MetadataFormat("namespace",
                "http://www.clarin.eu/cmd/");

        return metadataFormat;
    }

    @Override
    void addTraces() {

        // add the traces identifying the records the test should yield
        addToList("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh03de30");
        addToList("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh7b8760");
        addToList("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh034a70");
        addToList("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh136cd0");
        addToList("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:sh3123d0");
        addToList("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:shaccf90");
        addToList("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:shd0d800");
        addToList("http://metalb.csc.fi/cgi-bin/que", "cmdi0571", "oai:kielipankki.fi:shff9200");
    }



}
