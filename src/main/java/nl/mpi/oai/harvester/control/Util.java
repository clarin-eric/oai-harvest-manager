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

package nl.mpi.oai.harvester.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Utility functions for use in a static context.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class Util {
    private static final Logger logger = Logger.getLogger(Util.class);

    /**
     * Ensure a directory exists. That is, if it already exists, do
     * nothing; otherwise create the directory (and any nested
     * directories above if required).
     *
     * @param dir The directory to be checked/created
     * @throws IOException if dir already exists as a non-directory file or if
     *                     it cannot be created
     */
    public static void ensureDirExists(Path dir) throws IOException {
	if (Files.exists(dir)) {
	    if (!Files.isDirectory(dir)) {
		throw new IOException("File exists: " + dir);
	    }
	    return;
	}
	Files.createDirectories(dir);
    }

    /**
     * Ensure a directory exists. That is, if it already exists, do
     * nothing; otherwise create the directory (and any nested
     * directories above if required).
     *
     * @param dirname Name of the directory to be checked/created
     * @throws IOException problem with the directory
     */
    public static void ensureDirExists(String dirname) throws IOException {
	ensureDirExists(Paths.get(dirname));
    }

    /**
     * Convert name string to format suitable for use in the file system and
     * in URLs. In practice, convert into a string that matches the regular
     * expression "^\w*$".
     * 
     * @param name original name
     * @return cleaned up name
     */
    public static String toFileFormat(String name) {
	if (name == null)
	    return null;

	return Normalizer.normalize(name.trim(), Normalizer.Form.NFD)
		.replaceAll("\\p{M}", "").replaceAll("\\W+", "_");
    }

    /** 
     * Return text content of a node, or null if it has none. 
     * 
     * @param xpath XPath engine
     * @param xp xpath to the node
     * @param n context node
     * @return text content of the node
     * @throws javax.xml.xpath.XPathExpressionException something is wrong with the xpath
     */
    public static String getNodeText(XPath xpath, String xp, Node n)
	    throws javax.xml.xpath.XPathExpressionException {
	Node p = (Node) xpath.evaluate(xp, n, XPathConstants.NODE);
	if (p == null)
	    return null;
	String s = p.getNodeValue();
	return (s == null) ? null : s.trim();
    }
}
