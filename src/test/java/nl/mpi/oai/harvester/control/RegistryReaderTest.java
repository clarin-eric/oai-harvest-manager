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

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NodeList;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.google.common.base.Charsets;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
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

    private static final String PROVIDER_INFO_RESOURCE = "/centre-registry-providerinfo.xml";
    private static final String REGISTRY_OVERVIEW_RESOURCE = "/centre-registry-overview.xml";
    private static final String REGISTRY_PATH = "/";
    private static final String CENTRE_INFO_RESOURCE_PATH = "/restxml/1";
    private String registryURl;
    private String centreInfoUrl;
    
    private DocumentBuilder db;
    private RegistryReader instance;

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(8089);

    @Rule
    public WireMockClassRule wireMockInstanceRule = wireMockRule;
    
    @Before
    public void setUp() throws Exception {
        instance = new RegistryReader();
        db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        //set up mock centre registry REST XML server
        stubFor(get(urlEqualTo(CENTRE_INFO_RESOURCE_PATH))
                .willReturn(aResponse()
                        .withBody(getResourceAsString(PROVIDER_INFO_RESOURCE))));
        centreInfoUrl = "http://localhost:" + wireMockRule.getOptions().portNumber() + CENTRE_INFO_RESOURCE_PATH;

        stubFor(get(urlEqualTo(REGISTRY_PATH))
                .willReturn(aResponse()
                        .withBody(getResourceAsString(REGISTRY_OVERVIEW_RESOURCE)
                                .replaceAll("<Center_id_link>\\S+</Center_id_link>", "<Center_id_link>" + centreInfoUrl + "</Center_id_link>"))));
        registryURl = "http://localhost:" + wireMockRule.getOptions().portNumber() + REGISTRY_PATH;
    }

    /**
     * Test of getProviderInfoUrls method, of class RegistryReader.
     */
    @Test
    public void testGetProviderInfoUrlsFromDoc() throws Exception {
        Document docSummary = db.parse(getClass().getResourceAsStream(REGISTRY_OVERVIEW_RESOURCE));

        List<String> result = instance.getProviderInfoUrls(docSummary);
        assertEquals(24, result.size());
    }

    /**
     * Test of getEndpoint method, of class RegistryReader.
     */
    @Test
    public void testGetEndpointFromDoc() throws Exception {
        String expResult = "http://www.phonetik.uni-muenchen.de/cgi-bin/BASRepository/oaipmh/oai.pl?verb=Identify";

        NodeList result = instance.getEndpoints(getProviderInfoDoc());
        assertEquals(expResult, result.item(0).getNodeValue());
    }

    @Test
    public void testGetEndpointsFromService() throws Exception {
        final List<String> urls = instance.getEndpoints(new URL(registryURl));
        assertEquals(48, urls.size()); // 24 'centres' * 2 endpoints
    }
    
    @Test
    public void testGetOaiSetsFromService() throws Exception {
        final String endpointUrl1 = "http://www.phonetik.uni-muenchen.de/cgi-bin/BASRepository/oaipmh/oai.pl?verb=Identify";
        final String endpointUrl2 = "http://www.phonetik.uni-muenchen.de/cgi-bin/BASRepository/oaipmh/oai2.pl?verb=Identify";
        
        final Map<String, Collection<CentreRegistrySetDefinition>> map = instance.getEndPointOaiPmhSetMap(new URL(registryURl));
        assertEquals(2, map.size());
        assertTrue(map.containsKey(endpointUrl1));
        assertEquals(2, map.get(endpointUrl1).size());
        assertTrue(map.containsKey(endpointUrl2));
        assertEquals(0, map.get(endpointUrl2).size());
    }

    @Test
    public void testGetOaiPmhSetsFromDoc() throws Exception {
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
        try (InputStream resource = getClass().getResourceAsStream(PROVIDER_INFO_RESOURCE)) {
            return db.parse(resource);
        }
    }

    private static String getResourceAsString(String resourceName) throws IOException {
        final String registryOverviewString;
        try (InputStream infoResourceStream = RegistryReaderTest.class.getResourceAsStream(resourceName)) {
            registryOverviewString = IOUtils.toString(infoResourceStream, Charsets.UTF_8.name());
        }
        return registryOverviewString;
    }

}
