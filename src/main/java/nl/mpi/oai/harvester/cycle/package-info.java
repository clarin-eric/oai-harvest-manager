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
 * When each endpoint maintained by the manager has been harvested, or at least
 * an attempted to harvest the endpoint has been made, one harvesting cycle has
 * been completed. Through the cycle and endpoint interfaces, the cycle package
 * provides a framework to a client that needs to decide whether, when and how
 * an endpoint should be harvested. <br><br>
 *
 * The client can specify endpoints to the cycle package. However, if the
 * client desires it, the package will also consider endpoints attempted in the
 * past. To this end, it records details about attempts in a harvest overview. <br><br>
 *
 * Depending on the implementation of the overview, the client to the package
 * can add definitions to the overview that define whether and when and how a
 * specific endpoint should be harvested. The package implements the overview
 * as an XML file. It adapts the XML definitions to objects used by the XML
 * based cycle and vice versa. <br><br>
 *
 * The XML implementation offers the advantage of changing the cycle and
 * endpoint properties manually. This provides the necessary flexibility for
 * managing exceptions in the unfolding of the harvesting cycle.<br><br>
 *
 * For interpretation of the definition in the overview file, please refer to
 * the Endpoint and CycleProperties interfaces.<br><br>
 *
 * <p><IMG SRC="doc-files/package overview - cycle.svg">
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
package nl.mpi.oai.harvester.cycle;