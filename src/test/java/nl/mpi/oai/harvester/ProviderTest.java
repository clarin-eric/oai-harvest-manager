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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nl.mpi.oai.harvester.metadata.MetadataFormat;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 * Tests for Provider class. These test parsing of actual OAI-PMH responses
 * (found in files in the test resources directory). No network connections
 * are made.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class ProviderTest {
    /**
     * Test of parseProviderName method, of class Provider.
     */
    @Test
    public void testParseProviderName() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/response-Identify.xml"));

	String expResult = "CLARIN Centre Vienna / Language Resources Portal";

	Provider instance = new Provider("dummy", 1);
	String result = instance.parseProviderName(doc);

	assertEquals(expResult, result);
    }

    /**
     * Test of addIdentifiers method, of class Provider.
     */
    @Test
    public void testAddIdentifiers() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/response-ListIdentifiers.xml"));

	List<String> expResult = new ArrayList<>();
	expResult.add("lrp:dict-gate.1");
	expResult.add("lrp:dict-gate.2");
	expResult.add("lrp:dict-gate.3");

	List<String> ids = new ArrayList<>();
	Provider instance = new Provider("dummy", 1);
	instance.addIdentifiers(doc, ids);

	assertEquals(expResult, ids);
    }

    /**
     * Test of parsePrefixes method, of class Provider. In this case multiple
     * metadata prefixes match.
     */
    @Test
    public void testParsePrefixes_multiple() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/response-ListMetadataFormats.xml"));

	List<String> expResult = new ArrayList<>();
	expResult.add("cmdi_lexRes");
	expResult.add("cmdi_teiHdr");
	expResult.add("cmdi_textCorpus");
	expResult.add("cmdi_collection");

	MetadataFormat format = new MetadataFormat("namespace",
		"http://www.clarin.eu/cmd/");
	Provider instance = new Provider("dummy", 1);
	List<String> result = instance.parsePrefixes(doc, format);

	assertEquals(expResult, result);
    }

    /**
     * Test of parsePrefixes method, of class Provider. In this case a simple
     * lookup for a single matching prefix is done.
     */
    @Test
    public void testParsePrefixes_simple() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/response-ListMetadataFormats.xml"));

	List<String> expResult = new ArrayList<>();
	expResult.add("oai_dc");

	MetadataFormat format = new MetadataFormat("prefix", "oai_dc");
	Provider instance = new Provider("dummy", 1);
	List<String> result = instance.parsePrefixes(doc, format);

	assertEquals(expResult, result);
    }

    /**
     * Test of parsePrefixes method, of class Provider. In this case there are
     * no matching metadata prefixes.
     */
    @Test
    public void testParsePrefixes_nomatch() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/response-ListMetadataFormats.xml"));

	List<String> expResult = Collections.emptyList();

	MetadataFormat format = new MetadataFormat("schema", "garbage");
	Provider instance = new Provider("dummy", 1);
	List<String> result = instance.parsePrefixes(doc, format);

	assertEquals(expResult, result);
    }
}
