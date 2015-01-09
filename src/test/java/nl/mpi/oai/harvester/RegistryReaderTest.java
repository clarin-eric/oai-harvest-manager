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

package nl.mpi.oai.harvester;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NodeList;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 * Tests for RegistryReader. (Only parsing of canned responses is tested.
 * No connection to a registry is made.)
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class RegistryReaderTest {
    /**
     * Test of getProviderInfoUrls method, of class RegistryReader.
     */
    @Test
    public void testGetProviderInfoUrls() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document docSummary = db.parse(getClass().getResourceAsStream("/centre-registry-overview.xml"));

	RegistryReader instance = new RegistryReader();
	List<String> result = instance.getProviderInfoUrls(docSummary);
	assertEquals(24, result.size());
    }

    /**
     * Test of getEndpoint method, of class RegistryReader.
     */
    @Test
    public void testGetEndpoint() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document docProvInfo = db.parse(getClass().getResourceAsStream("/centre-registry-providerinfo.xml"));

	RegistryReader instance = new RegistryReader();
	String expResult = "http://www.phonetik.uni-muenchen.de/cgi-bin/BASRepository/oaipmh/oai.pl?verb=Identify";
	NodeList result = instance.getEndpoints(docProvInfo);
	assertEquals(expResult, result.item(0).getNodeValue());
    }
}
