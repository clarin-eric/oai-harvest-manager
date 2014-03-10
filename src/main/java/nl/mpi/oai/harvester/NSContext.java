/*
 * Copyright (C) 2000-2014, The Max Planck Institute for
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

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

/**
 * This is just a helper class implementing NamespaceContext
 * It enables us to make proper xpath queries to OLAC compliant
 * XML files
 *
 * @author Peter Wood (MPI-PL)
 */

public class NSContext implements NamespaceContext {
    private String ns;
    private String pre;

    public NSContext(String ns, String pre) {
	this.pre = pre;
	this.ns = ns;
    }

    public String getNamespaceURI(String prefix) {
	if (prefix.equals("olac")) {
	    return "http://www.language-archives.org/OLAC/1.0/";
	}
	return (prefix.equals(pre)? ns: null);
    }

    public String getPrefix(String namespaceURI) {
	return null;
    }

    public Iterator<Object> getPrefixes(String namespaceURI) {
	return null;
    }
}
