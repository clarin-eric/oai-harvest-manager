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

package nl.mpi.oai.harvester.harvesting;

import ORG.oclc.oai.harvester2.verb.HarvesterVerb;
import nl.mpi.oai.harvester.Provider;

import java.util.List;

/**
 * <br> Definition of elements common to various implementations of the
 * protocol defined by the harvesting interface <br><br>
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public abstract class AbstractHarvesting implements Harvesting {

    /**
     * <br> Information on where to send the request
     */
    final Provider provider;

    /**
     * <br> Pointer to current set
     *
     * Please note the only the AbstractListHarvesting class updates this
     * index.
     */
    int sIndex;

    /** <br> Metadata prefixes that need to be matched
     *
     */
    List<String> prefixes;

    /**
     * <br> Pointer to current prefix
     *
     * Please note the only the AbstractListHarvesting class updates this
     * index.
     */
    int pIndex;

    /**
     * <br> Response to the OAI request
     */
    HarvesterVerb response;

    /**
     * <br> The resumption token send by the previous request
     *
     * Please note that not every implementation of the protocol defined by
     * the harvesting interface might need it.
     */
    String resumptionToken;

    /**
     * <br> Associate an OAI endpoint with the protocol defined by the
     * harvesting interface.
     *
     * @param provider the provider
     */
    AbstractHarvesting(Provider provider) {

        this.provider = provider;
        pIndex        = 0;

        // check for protocol errors
        if (provider == null){
            throw new HarvestingException();
        }
    }
}
