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
 * kj: specify
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class MetadataFactory {

    public MetadataInterface getHook (){
        return null;
    }

    public Metadata create (String id, Document doc, Provider endpoint, Boolean isInEnvelope, Boolean isList){

        Metadata metadata;

        metadata = new Metadata(id, doc, endpoint, isInEnvelope, isList);

        MetadataInterface hook;

        hook = getHook();

        if (hook == null){
            return metadata;
        } else {
            return hook.newMetadata (metadata);
        }
    }
}
