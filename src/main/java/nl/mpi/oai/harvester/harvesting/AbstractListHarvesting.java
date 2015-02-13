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

import nl.mpi.oai.harvester.Provider;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Objects;

/**
 * <br>Harvesting depending on a list<br><br>
 *
 * This class gathers the fields and methods needed for harvesting that are
 * typical for harvesting depending on a list. You could, for example think
 * of harvesting using a list of identifiers or a list of records.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
abstract public class AbstractListHarvesting extends AbstractHarvesting {

    private static final Logger logger = Logger.getLogger(
            AbstractListHarvesting.class);

    /** <br> a list of nodes kept between the processing and parsing of a
     *  response
     */
    NodeList nodeList;
    /** <br> pointer to next element that needs to be checked */
    int nIndex;

    /** <br> A list to store identifier and prefix pairs in. A pair can be in
     *  the list only once, thus ensuring the extending classes to return every
     *  record identified exactly once.
     */
    final SortedArrayList targets;
    /** pointer to next element to be parsed and returned */
    int tIndex;

    /**
     * <br> Associate list based harvesting with a provider
     * 
     * @param provider the provider
     */
    AbstractListHarvesting(Provider provider) {

        super(provider);
        nIndex          = 0;
        resumptionToken = null;
        tIndex          = 0;
        targets         = new SortedArrayList ();
    }
    
    /**
     * <br> ArrayList sorted according a to the relation defined on the elements
     *
     * Note: since the class does not depend on the outer class, consider it
     *       static.
     */
    static class SortedArrayList extends ArrayList<IdPrefix> {
        
        /*
         * Since this class implements the Serializable interface, declare an
         * explicit version number. An serialized object in this class will
         * carry the identification to ensure that an instance of receiving is
         * compatible in the sense that it can correctly deserialize the object.
         * Please update the identification whenever the class changes.
         */
        private static final long serialVersionUID = 1L;

        /**
         * <br> Insert an element into the list if and only if it is not already
         * included in the list.
         *
         * @param element he element to be inserted
         * @return true if the element was inserted, false otherwise
         */
        boolean checkAndInsertSorted(IdPrefix element) {

            int i = 0, j;

            for (; ; ) {
                if (i == this.size()) {
                    // element not included yet
                    this.add(element);
                    return true;
                }
                // j = c.compareTo(this.get(i));
                j = element.compareTo (this.get(i));
                if (j == 0) {
                    // found a match, element already in the list
                    return false;
                } else {
                    if (j > 0) {
                        // there could still be a match, continue
                        i++;
                    } else {
                        // there will not be a match, insert
                        this.add(i, element);
                        return true;
                    }
                }
            }
        }
    }

    /**
     * <br> Pair of identifier and prefix. By the compareTo method the class
     * defines an ordering relation on the pairs.
     * 
     * Note: like SortedArrayList class, this class can be static.
     *
     * Note: because of the ordering relation defined, the class implements
     *       an equals method next to the compareTo method.
     */
    static class IdPrefix implements Comparable<IdPrefix> {

        /** constituents of the idPrefix, the identifier part of the pair*/
        final String identifier;
        /** Prefix part of the pair */
        final String prefix;

        /**
         * <br>Create an identifier and prefix pair
         *
         * @param identifier the identifier part of the pair
         * @param prefix the prefix part of the pair
         */
        IdPrefix (String identifier, String prefix){
            this.identifier = identifier;
            this.prefix     = prefix;
        }

        /**
         * <br> Compare the IdPrefix object to another one<br><br>
         *
         * Please note that this method defines an ordering relation that is
         * not compatible with the object.equals method. Because the class
         * would inherit that equals method, an alternative equals method is
         * guaranteeing a correct interpretation of the ordering.
         *
         * @param idPrefix another idPrefix to be compared to the idPrefix object
         * @return -1 if the parameters is smaller than the object <br>
         *          0 if equal <br>
         *          1 if greater
         */
        @Override
        public int compareTo(IdPrefix idPrefix) {

            int cIdentifier = this.identifier.compareTo(idPrefix.identifier);
            int cPrefix = this.prefix.compareTo(idPrefix.prefix);

            if (cIdentifier != 0) {
                return cIdentifier;
            } else {
                // identifiers are equal, prefixes will not be
                return cPrefix;
            }
        }

        /**
         * <br> Check if the IdPrefix object is equal to another object<br><br>
         *
         * @param object another IdPrefix object
         * @return true if the other object is of type IdPrefix type, and both
         *         objects are equal, false otherwise.
         */
        @Override
        public boolean equals(Object object) {

            if (!(object instanceof IdPrefix)) {

                /* Since the class is its own superclass, the comparison cannot
                   be passed on to another class. Consider the objects to be
                   unequal.
                 */
                return false;
            } else {
                // the parameter object is also of type IdPrefix, cast it
                IdPrefix idPrefix = (IdPrefix) object;
                // invoke string comparison on the components
                if (!this.identifier.equals(idPrefix.identifier)) {
                    /* The identifiers differ. This means that the pairs are
                       are not the same.
                     */
                    return false;
                } else {
                    // equality depends on the prefixes to be equal
                    return this.prefix.equals(idPrefix.prefix);
                }
            }
        }

        /** 
         * <br> Generate hashcode for storing and manipulating class instances
         *
         * @return the hashcode
         */
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.identifier);
            hash = 97 * hash + Objects.hashCode(this.prefix);
            return hash;
        }
    }

    /**
     * Determine if a client scenario should make another request to the
     * endpoint. At first, iterate over the resumption tokens the endpoint
     * might generate. After that, iterate over the prefixes supplied. For
     * each prefix iterate over the sets indicated in the provider object.
     *
     * Note: this is the only method that increments the pIndex and sIndex
     * fields.
     *
     * Invariant. When harvesting does not involve sets:
     *
     * pIndex <= prefixes.size
     *
     * otherwise:
     *
     * pIndex <= prefixes.size && sIndex <= provider.sets.length
     *
     * @return true if the endpoint could still have metadata available
     *         associated with the set and prefix indicated by pIndex and
     *         sIndex, false otherwise
     */
    @Override
    public boolean requestMore() {

        // check for protocol errors
        if (pIndex > prefixes.size()){
            throw new HarvestingException();
        }
        if (provider.sets == null){
            // harvesting does not involve sets
        } else {
            if (sIndex > provider.sets.length){
                throw new HarvestingException();
            }
        }

        if (!(resumptionToken == null || resumptionToken.isEmpty())) {
            // indicate another request could be made
            return true;
        } else {
            // no need to resume requesting within the current set and prefix
            if (provider.sets == null) {
                pIndex++;
                return pIndex != prefixes.size(); // done
            } else {
                sIndex++;
                if (sIndex == provider.sets.length) {
                    // try the next prefix
                    sIndex = 0;
                    pIndex++;
                    return pIndex != prefixes.size(); // done
                } else {
                    // try the next set
                    logger.debug("Requesting records in the "
                            + provider.sets[sIndex] + " set");
                    return true;
                }
            }
        }
    }

    /**
     * Determine if a client scenario has fully traversed the list of target
     * records
     *
     * Invariant: pIndex <= prefixes.size
     *
     * @return true if there are more, false otherwise
     */
    public boolean fullyParsed() {

        // check for protocol errors
        if (tIndex > targets.size()) {
            throw new HarvestingException();
        }

        return tIndex == targets.size();
    }
}
