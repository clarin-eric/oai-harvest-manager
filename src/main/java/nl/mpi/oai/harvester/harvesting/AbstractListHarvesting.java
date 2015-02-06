package nl.mpi.oai.harvester.harvesting;

import ORG.oclc.oai.harvester2.verb.HarvesterVerb;
import nl.mpi.oai.harvester.metadata.Provider;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * kj: doc
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
abstract public class AbstractListHarvesting implements Harvesting {

    // information on where to send the request
    protected final Provider provider;
    // pointer to current set
    private int sIndex;

    // Response to the request
    HarvesterVerb response;

    // a list of nodes kept between the processing and parsing of a response
    NodeList nodeList;
    // pointer to next element that needs to be checked
    public int nIndex;

    /* The resumption token send by the previous request. Please note that not
       every mode of harvesting needs it.
     */
    private String resumptionToken;

    /* A list to store identifier and prefix pairs in. A pair can be in the
       list only once, thus ensuring the extending classes to return every record
       identified exactly once.
    */
    protected final SortedArrayList <IdPrefix> targets;
    // pointer to next element to be parsed and returned
    protected int tIndex;

    protected AbstractListHarvesting(Provider provider) {

        this.provider   = provider;
        nIndex          = 0;
        resumptionToken = null;
        tIndex          = 0;
        targets         = new SortedArrayList <> ();
    }

    /**
     * ArrayList sorted according a to the relation defined on the elements
     *
     * @param <T> the type of the elements in the list
     */
    public class SortedArrayList<T> extends ArrayList<T> {

        /**
         * Insert an element into the list if and only if it is not already
         * included in the list.
         *
         * @param element he element to be inserted
         * @return true if the element was inserted, false otherwise
         */
        public boolean checkAndInsertSorted(T element) {

            int i = 0, j;

            Comparable<T> c = (Comparable<T>) element;
            for (; ; ) {
                if (i == this.size()) {
                    // element not included yet
                    this.add(element);
                    return true;
                }
                j = c.compareTo(this.get(i));
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
     * Pair of identifier and prefix. By the compareTo method the class defines
     * an ordering relation on the pairs.
     */
    public class IdPrefix implements Comparable {

        // constituents of the idPrefix
        public final String identifier;
        public final String prefix;

        public IdPrefix (String identifier, String prefix){
            this.identifier = identifier;
            this.prefix     = prefix;
        }

        /**
         * Compare the IdPrefix object to another one
         *
         * @param object another idPrefix to be compared to the idPrefix object
         * @return  -1 if the parameters is smaller than the object
         *           0 if equal
         *           1 if greater
         */
        @Override
        public int compareTo(Object object) {

            if (!(object instanceof IdPrefix)) {
                // we do not expect this
                return 0;
            } else {
                IdPrefix idPrefix = (IdPrefix) object;

                int cIdentifier = this.identifier.compareTo(idPrefix.identifier);
                int cPrefix = this.prefix.compareTo(idPrefix.prefix);

                if (cIdentifier != 0) {
                    return cIdentifier;
                } else {
                    // identifiers are equal, prefixes will not be
                    return cPrefix;
                }
            }
        }
    }

}
