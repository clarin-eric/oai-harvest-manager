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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This action corresponds to stripping off the OAI-PMH envelope surrounding
 * a harvested metadata record.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class StripAction implements Action {
    private static final Logger logger = Logger.getLogger(StripAction.class);

    private final XPath xpath;
    private final DocumentBuilder db;

    public StripAction() throws ParserConfigurationException {
	XPathFactory xpf = XPathFactory.newInstance();
	xpath = xpf.newXPath();	
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	db = dbf.newDocumentBuilder();
    }

    @Override
    /**
     * Note: consider a strip action that is sensitive to different kinds of
     * responses, like for example: record, list of records, metadata content
     * of a record.
     */
    public boolean perform(MetadataRecord record) {

        switch (record.getType()) {
            case "multiple records": {
                // not implemented yet
                return false;
            }

            case "part of multiple records": {
                // strip action moves saving away from the orig
                record.setType("content");
                return true;
            }

            case "record": {

                // Get the first child node of the "metadata" tag;
                // that's the content of the response without the
                // OAI-PMH envelope.

                Node contentRoot = null;
                try {
                    contentRoot = (Node) xpath.evaluate("//*[local-name()=" +
                                    "'metadata' and parent::*[local-name()=" +
                                    "'record']]/*[1]",
                            record.getDoc(), XPathConstants.NODE);
                } catch (XPathExpressionException ex) {
                    logger.error(ex);
                }

                if (contentRoot == null) {
                    logger.warn("No content was found in this envelope");
                    return false;
                }

                Document content = db.newDocument();
                Node copy = content.importNode(contentRoot, true);
                content.appendChild(copy);
                record.setDoc(content);
                record.setType("content");

                return true;
            }
            default:
                // nothing to be done, error could be reported
                return true;
        }
    }

    @Override
    public String toString() {
	return "strip";
    }

    // All strip actions are equal.
    @Override
    public int hashCode() {
	return 1;
    }
    @Override
    public boolean equals(Object o) {
	if (o instanceof StripAction) {
	    return true;
	}
	return false;
    }

    @Override
    public Action clone() {
	try {
	    // All strip actions are the same. This is effectively a "deep"
	    // copy since it has its own XPath object.
	    return new StripAction();
	} catch (ParserConfigurationException ex) {
	    logger.error(ex);
	}
	return null;
    }
}
