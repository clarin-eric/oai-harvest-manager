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

/**
 * <br> Generally speaking, the harvesting package interfaces a client with the
 * OAI protocol for harvesting metadata. The package follows the protocol for
 * all verbs it can apply in the same way. The interface to the package defines
 * the basic steps the client can take to obtain the desired metadata records.<br><br>
 *
 * To harvest records from the endpoint the specified to the package, the client
 * can engage itself in different scenarios: listing metadata formats, listing
 * identifiers and records, or listing records directly.<br><br>
 *
 * The package will apply the ListMetadataFormats verb to harvest a list of
 * formats. An endpoint should at least support one of: metadata prefix,
 * namespace or schema. The package transforms the response to the metadata
 * format query into a list of metadata prefixes.<br><br>
 *
 * A prefix identifies the type of record provided by the endpoint, like for
 * example: dc (Dublin Core), olac (Open Language Archives Community), or cmdi
 * (Component Metadata Initiative). The client to the package can use the
 * prefixes to further process the records harvested.<br><br>
 *
 * <p><IMG SRC="doc-files/package_overview_-_harvesting.svg" ALT="package overview - harvesting">
 *
 * <br>After having obtained a list of prefixes the client can follow a
 * scenario in which it first obtains a list of identifiers to metadata records
 * and after that list the records identified one by one. Alternatively, the
 * client can obtain the records directly.<br><br>
 *
 * The package uses a list based store, both for the results of the application
 * of the ListIdentifiers and the ListRecords verb. This list also provides for
 * a means to avoid returning duplicate records to the client. A record would
 * be returned more than once if the client were to specify more than one set in
 * which the record occurs.<br><br>
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
package nl.mpi.oai.harvester.harvesting;