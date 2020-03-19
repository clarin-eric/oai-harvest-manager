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
package nl.mpi.oai.harvester.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NodeList;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Tests for RegistryReader. (Only parsing of canned responses is tested. No
 * connection to a registry is made.)
 *
 * @author Lari Lampen (MPI-PL)
 * @author twan@clarin.eu
 */
public class RegistryReaderTest {

    private DocumentBuilder db;
    private RegistryReader instance;

    @Before
    public void setUp() throws Exception {
        instance = new RegistryReader();
        db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    /**
     * Test of getProviderInfoUrls method, of class RegistryReader.
     */
    @Test
    public void testGetProviderInfoUrls() throws Exception {
        Document docSummary = db.parse(getClass().getResourceAsStream("/centre-registry-overview.xml"));

        List<String> result = instance.getProviderInfoUrls(docSummary);
        assertEquals(24, result.size());
    }

    /**
     * Test of getEndpoint method, of class RegistryReader.
     */
    @Test
    public void testGetEndpoint() throws Exception {
        String expResult = "http://www.phonetik.uni-muenchen.de/cgi-bin/BASRepository/oaipmh/oai.pl?verb=Identify";

        NodeList result = instance.getEndpoints(getProviderInfoDoc());
        assertEquals(expResult, result.item(0).getNodeValue());
    }

    @Test
    public void testGetOaiPmhSets() throws Exception {
        String endpoint = "http://www.phonetik.uni-muenchen.de/cgi-bin/BASRepository/oaipmh/oai.pl?verb=Identify";
        NodeList result = instance.getOaiPmhSets(getProviderInfoDoc(), endpoint);
        assertEquals(2, result.getLength());
    }

    @Test
    public void testGetOaiPmhSetsNone() throws Exception {
        String endpoint = "http://www.clarin.eu";
        NodeList result = instance.getOaiPmhSets(getProviderInfoDoc(), endpoint);
        assertEquals(0, result.getLength());
    }

    private Document getProviderInfoDoc() throws SAXException, IOException {
        try (InputStream resource = getClass().getResourceAsStream("/centre-registry-providerinfo.xml")) {
            return db.parse(resource);
        }
    }

}
