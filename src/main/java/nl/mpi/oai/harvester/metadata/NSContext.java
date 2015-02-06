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

import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;

/**
 * An implementation of an XML namespace context.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class NSContext implements NamespaceContext {
    private static final Logger logger = Logger.getLogger(NSContext.class);

    /**
     * URIs indexed by prefix.
     */
    private Map<String, String> pref2ns;

    /**
     * Prefixes indexed by URI. (Note that multiple prefixes can be bound to
     * the same URI, but a prefix is only bound to a single URI at a time.
     */
    private Map<String, List<String>> ns2pref;

    /**
     * Create a new namespace context (with no bindings).
     */
    public NSContext() {
	pref2ns = new HashMap<>();
	ns2pref = new HashMap<>();
    }

    /**
     * Add a new namespace binding.
     *
     * @param prefix namespace prefix
     * @param ns namespace address
     */
    public void add(String prefix, String ns) {
	pref2ns.put(prefix, ns);
	List<String> prefixes;
	if (ns2pref.containsKey(ns)) {
	    prefixes = ns2pref.get(ns);
	} else {
	    prefixes = new ArrayList<>();
	    ns2pref.put(ns, prefixes);
	}
	prefixes.add(prefix);
    }

    /**
     * Look up namespace URI based on prefix. Some of the return
     * values are fixed by the XML standard.
     */
    @Override
    public String getNamespaceURI(String prefix) {
	if (prefix == null)
	    throw new IllegalArgumentException("Illegal check of null namespace prefix");

	if (pref2ns.containsKey(prefix))
	    return pref2ns.get(prefix);

	if (prefix.equals(XMLConstants.XML_NS_PREFIX))
	    return XMLConstants.XML_NS_URI;
	if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
	    return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

	return "";
    }

    /**
     * Look up prefix based on namespace URI. Some of the return
     * values are fixed by the XML standard.
     *
     * @param uri namespace URI
     */
    @Override
    public String getPrefix(String uri) {
	if (uri == null)
	    throw new IllegalArgumentException("Illegal check of null namespace URI");

	if (ns2pref.containsKey(uri))
	    return ns2pref.get(uri).get(0);

	if (uri.equals(XMLConstants.XML_NS_URI))
	    return XMLConstants.XML_NS_PREFIX;
	if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
	    return XMLConstants.XMLNS_ATTRIBUTE;

	return null;
    }

    /**
     * Get list of prefixes bound to a namespace URI. Some of the return values
     * are fixed by the XML standard.
     */
    @Override
    public Iterator getPrefixes(String uri) {
	if (uri == null)
	    throw new IllegalArgumentException("Illegal check of null namespace URI");

	if (ns2pref.containsKey(uri))
	    return ns2pref.get(uri).iterator();

	if (uri.equals(XMLConstants.XML_NS_URI)) {
	    List<String> dummy = new ArrayList<>(1);
	    dummy.add(XMLConstants.XML_NS_PREFIX);
	    return dummy.iterator();
	}
	if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
	    List<String> dummy = new ArrayList<>(1);
	    dummy.add(XMLConstants.XMLNS_ATTRIBUTE);
	    return dummy.iterator();
	}

	return Collections.emptyIterator();
    }
}
