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

/**
 * This class represents a schema or family of schemata for metadata, as
 * defined by a match criterion. It may, but DOES NOT necessarily correspond
 * to a single XML schema or OAI-PMH metadata prefix.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class MetadataFormat {
    private final String matchType;
    private final String matchValue;
    
    public MetadataFormat(String matchType, String matchValue) {
	this.matchType = matchType;
	this.matchValue = matchValue;
    }

    public String getType() {
	return matchType;
    }
    
    public String getValue() {
	return matchValue;
    }

    @Override
    public String toString() {
	return matchType+"="+matchValue;
    }
}
