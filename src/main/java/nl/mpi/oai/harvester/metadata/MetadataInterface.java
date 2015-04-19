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

package nl.mpi.oai.harvester.metadata;

import nl.mpi.oai.harvester.Provider;
import org.w3c.dom.Document;

/**
 * By returning the metadata received, a test helper method implementing the
 * interface can record the metadata created by some scenario.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public interface MetadataInterface {

    /**
     * <br> Reflect the metadata presented <br><br>
     *
     * Note: the method should return the metadata it receives. In the meantime
     * it can inspect the data.
     *
     * @param metadata the metadata
     * @return the metadata
     */
    Metadata newMetadata(Metadata metadata);
}
