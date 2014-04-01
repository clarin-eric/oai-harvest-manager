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
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 * Tests for StaticProvider class. These test parsing of an actual static
 * provider response (found in the file static-repo.xml in the test resources
 * directory). No network connections are made.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class StaticProviderTest {
    /**
     * Test of getProviderName method, of class StaticProvider.
     */
    @Test
    public void testGetProviderName() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/static-repo.xml"));

	String expResult = "Magoria Books' Carib and Romani Archive";

	StaticProvider instance = new StaticProvider(doc);
	String result = instance.getProviderName();

	assertEquals(expResult, result);
    }

    /**
     * Test of init + getName methods, of class StaticProvider.
     */
    @Test
    public void testGetName() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/static-repo.xml"));

	String expResult = "Magoria Books' Carib and Romani Archive";

	StaticProvider instance = new StaticProvider(doc);
	instance.init();
	String result = instance.getName();

	assertEquals(expResult, result);
    }

    /**
     * Test of getPrefixes method, of class StaticProvider.
     */
    @Test
    public void testGetPrefixes() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/static-repo.xml"));

	List<String> expResult = new ArrayList<>();
	expResult.add("olac");

	StaticProvider instance = new StaticProvider(doc);
	MetadataFormat format = new MetadataFormat("namespace",
		"http://www.language-archives.org/OLAC/1.0/");
	List<String> result = instance.getPrefixes(format);

	assertEquals(expResult, result);
    }

    /**
     * Test of getIdentifiers method, of class StaticProvider.
     */
    @Test
    public void testGetIdentifiers() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/static-repo.xml"));

	List<String> expResult = new ArrayList<>();
	expResult.add("oai:mbcarrom.linguistlist.org:370");
	expResult.add("oai:mbcarrom.linguistlist.org:371");

	StaticProvider instance = new StaticProvider(doc);
	List<String> result = instance.getIdentifiers("olac");

	assertEquals(expResult, result);
    }

    /**
     * Test of getRecord method, of class StaticProvider.
     */
    @Test
    public void testGetRecord() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/static-repo.xml"));

	String id = "oai:mbcarrom.linguistlist.org:371";
	String mdPrefix = "olac";
	StaticProvider instance = new StaticProvider(doc);
	MetadataRecord result = instance.getRecord(id, mdPrefix);

	assertNotNull(result);
	assertEquals(id, result.getId());
	assertEquals(instance, result.getOrigin());
	assertNotNull(result.getDoc());
    }

    /**
     * Test of getRecord method, of class StaticProvider. In this case there is
     * no match.
     */
    @Test
    public void testGetRecord_noSuchId() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/static-repo.xml"));

	String id = "garbage";
	String mdPrefix = "olac";
	StaticProvider instance = new StaticProvider(doc);
	MetadataRecord result = instance.getRecord(id, mdPrefix);

	assertNull(result);
    }

    /**
     * Test of getRecord method, of class StaticProvider.
     */
    @Test
    public void testGetRecord_noSuchPrefix() throws Exception {
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(getClass().getResourceAsStream("/static-repo.xml"));

	String id = "oai:mbcarrom.linguistlist.org:371";
	String mdPrefix = "garbage";
	StaticProvider instance = new StaticProvider(doc);
	MetadataRecord result = instance.getRecord(id, mdPrefix);

	assertNull(result);
    }
}
