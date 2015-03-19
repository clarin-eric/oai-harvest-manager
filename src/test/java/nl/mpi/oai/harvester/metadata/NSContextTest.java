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

package nl.mpi.oai.harvester.metadata;

import java.util.Iterator;

import nl.mpi.oai.harvester.metadata.NSContext;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the class NSContext.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class NSContextTest {
    /**
     * Test of getNamespaceURI method, of class NSContext. Case: a single
     * prefix is bound to a namespace.
     */
    @Test
    public void testGetNamespaceURI_boundPrefix() {
	String prefix = "xyz";
	String uri = "http://dummy/";
	NSContext instance = new NSContext();
	instance.add(prefix, uri);

	assertEquals(uri, instance.getNamespaceURI(prefix));
	assertEquals("", instance.getNamespaceURI(""));
	assertEquals("", instance.getNamespaceURI("unbound"));
	assertEquals("http://www.w3.org/XML/1998/namespace",
		instance.getNamespaceURI("xml"));
	assertEquals("http://www.w3.org/2000/xmlns/",
		instance.getNamespaceURI("xmlns"));
    }

    /**
     * Test of getNamespaceURI method, of class NSContext. Case: the default
     * namespace (i.e. with empty prefix) is bound.
     */
    @Test
    public void testGetNamespaceURI_boundDefault() {
	String prefix = "";
	String uri = "http://dummy/";
	NSContext instance = new NSContext();
	instance.add(prefix, uri);

	assertEquals(uri, instance.getNamespaceURI(""));
	assertEquals("", instance.getNamespaceURI("unbound"));
	assertEquals("http://www.w3.org/XML/1998/namespace",
		instance.getNamespaceURI("xml"));
	assertEquals("http://www.w3.org/2000/xmlns/",
		instance.getNamespaceURI("xmlns"));
    }

    /**
     * Test of getNamespaceURI method, of class NSContext. Case: null argument.
     */
    @Test(expected = IllegalArgumentException.class) 
    public void testGetNamespaceURI_null() {
	String prefix = "xyz";
	String uri = "http://dummy/";
	NSContext instance = new NSContext();
	instance.add(prefix, uri);

	instance.getNamespaceURI(null);
    }

    /**
     * Test of getPrefix method, of class NSContext. Case: single prefix bound.
     */
    @Test
    public void testGetPrefix_boundPrefix() {
	String prefix = "xyz";
	String uri = "http://dummy/";
	NSContext instance = new NSContext();
	instance.add(prefix, uri);

	assertEquals(prefix, instance.getPrefix(uri));
	assertNull(instance.getPrefix("unbound"));
	assertEquals("xml",
		instance.getPrefix("http://www.w3.org/XML/1998/namespace"));
	assertEquals("xmlns",
		instance.getPrefix("http://www.w3.org/2000/xmlns/"));
    }

    /**
     * Test of getPrefix method, of class NSContext. Case: empty prefix bound.
     */
    @Test
    public void testGetPrefix_boundDefault() {
	String prefix = "";
	String uri = "http://dummy/";
	NSContext instance = new NSContext();
	instance.add(prefix, uri);

	assertEquals(prefix, instance.getPrefix(uri));
	assertNull(instance.getPrefix("unbound"));
	assertEquals("xml",
		instance.getPrefix("http://www.w3.org/XML/1998/namespace"));
	assertEquals("xmlns",
		instance.getPrefix("http://www.w3.org/2000/xmlns/"));
    }

    /**
     * Test of getPrefix method, of class NSContext. Case: null argument.
     */
    @Test(expected = IllegalArgumentException.class) 
    public void testGetPrefix_null() {
	String prefix = "xyz";
	String uri = "http://dummy/";
	NSContext instance = new NSContext();
	instance.add(prefix, uri);

	instance.getPrefix(null);
    }

    /**
     * Test of getPrefixes method, of class NSContext. Case: single prefix
     * bound.
     */
    @Test
    public void testGetPrefixes_single() {
	String prefix1 = "xyz";
	String uri1 = "http://dummy/";

	NSContext instance = new NSContext();
	instance.add(prefix1, uri1);

	Iterator it;

	it = instance.getPrefixes(uri1);
	assertTrue(it.hasNext());
	assertEquals(prefix1, it.next());
	assertFalse(it.hasNext());

	it = instance.getPrefixes("unbound");
	assertFalse(it.hasNext());

	it = instance.getPrefixes("http://www.w3.org/XML/1998/namespace");
	assertTrue(it.hasNext());
	assertEquals("xml", it.next());
	assertFalse(it.hasNext());

	it = instance.getPrefixes("http://www.w3.org/2000/xmlns/");
	assertTrue(it.hasNext());
	assertEquals("xmlns", it.next());
	assertFalse(it.hasNext());
    }

    /**
     * Test of getPrefixes method, of class NSContext. Case: several prefixes
     * defined, including two with the same URI.
     */
    @Test
    public void testGetPrefixes_multiple() {
	String prefix1 = "xyz";
	String uri1 = "http://dummy/";

	String prefix2 = "xxx";
	String prefix3 = "yyy";
	String uri2 = "https://dummy/";

	NSContext instance = new NSContext();
	instance.add(prefix1, uri1);
	instance.add(prefix2, uri2);
	instance.add(prefix3, uri2);

	Iterator it = instance.getPrefixes(uri2);
	assertTrue(it.hasNext());
	assertEquals(prefix2, it.next());
	assertTrue(it.hasNext());
	assertEquals(prefix3, it.next());
	assertFalse(it.hasNext());
    }    

    /**
     * Test of getPrefixes method, of class NSContext. Case: null argument.
     */
    @Test(expected = IllegalArgumentException.class) 
    public void testGetPrefixes_null() {
	String prefix = "xyz";
	String uri = "http://dummy/";
	NSContext instance = new NSContext();
	instance.add(prefix, uri);

	instance.getPrefixes(null);
    }
}
