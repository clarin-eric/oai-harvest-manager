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

import org.w3c.dom.Document;

/**
 * This class represents a single record of metadata. More specifically, it
 * represents a metadata record which is instantiated as an XML document tree
 * and which may be subjected to modifications and manipulations that affect
 * the content but not the true identity of the document. In other words, the
 * record has a single immutable identifier, but its XML representation may
 * change.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class MetadataRecord {
    /** A unique identifier, such as the OAI-PMH record identifier. */
    private final String id;

    /** The OAI-PMH provider where this record originated. */
    private final Provider origin;

    /** The XML content of this record. */
    private Document doc;

    /**
     * Create a metadata record.
     * 
     * @param id unique identifier
     * @param doc XML tree corresponding to this record
     */
    public MetadataRecord(String id, Document doc, Provider origin) {
	this.id = id;
	this.doc = doc;
	this.origin = origin;
    }

    /**
     * Modify the XML tree representation of this record in a way that does
     * not change its identity.
     * 
     * @param doc modified content of this record
     */
    public void setDoc(Document doc) {
	this.doc = doc;
    }

    /** Get this record's unique identifier. */
    public String getId() {
	return id;
    }

    /** Get the XML tree representing this record. */
    public Document getDoc() {
	return doc;
    }

    /** Get the provider from which this record was harvested. */
    public Provider getOrigin() {
	return origin;
    }
}
