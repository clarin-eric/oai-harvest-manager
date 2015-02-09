package nl.mpi.oai.harvester.harvesting;

import ORG.oclc.oai.harvester2.verb.HarvesterVerb;
import nl.mpi.oai.harvester.metadata.Provider;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Objects;

/**
 * kj: List related harvesting, doc
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
abstract public class AbstractListHarvesting implements Harvesting {

    // information on where to send the request
    private final Provider provider;
    // pointer to current set
    private int sIndex;

    // Response to the request
    HarvesterVerb response;

    // a list of nodes kept between the processing and parsing of a response
    NodeList nodeList;
    /**  pointer to next element that needs to be checked */
    public int nIndex;

    /* The resumption token send by the previous request. Please note that not
       every mode of harvesting needs it.

       kj: consider moving to an abstract harvesting class
     */
    private String resumptionToken;

    /** A list to store identifier and prefix pairs in. A pair can be in the
       list only once, thus ensuring the extending classes to return every record
       identified exactly once.
    */
    protected final SortedArrayList  targets;
    /** pointer to next element to be parsed and returned */
    protected int tIndex;

    /**
     * kj: doc
     * 
     * @param provider 
     */
    protected AbstractListHarvesting(Provider provider) {

        this.provider   = provider;
        nIndex          = 0;
        resumptionToken = null;
        tIndex          = 0;
        targets         = new SortedArrayList ();
    }
    
    /**
     * ArrayList sorted according a to the relation defined on the elements
     *
     * Note: since the class does not depend on the outer class, consider it
     *       static.
     */
    protected static class SortedArrayList extends ArrayList<IdPrefix> {
        
        /**
         * Since this class implements the Serializable interface, declare an
         * explicit version number. An serialized object in this class will
         * carry the identification to ensure that an instance of receiving is
         * compatible in the sense that it can correctly deserialize the object.
         * Please update the identification whenever the class changes.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Insert an element into the list if and only if it is not already
         * included in the list.
         *
         * @param element he element to be inserted
         * @return true if the element was inserted, false otherwise
         */
        public boolean checkAndInsertSorted(IdPrefix element) {

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
     * Pair of identifier and prefix. By the compareTo method the class defines
     * an ordering relation on the pairs.
     * 
     * Note: like SortedArrayList class, this class can be static.
     */
    public static class IdPrefix implements Comparable<IdPrefix> {

        /** constituents of the idPrefix */
        public final String identifier;
        /** kj: doc */
        public final String prefix;

        /**
         * kj: doc
         * @param identifier
         * @param prefix 
         */
        public IdPrefix (String identifier, String prefix){
            this.identifier = identifier;
            this.prefix     = prefix;
        }

        /**
         * Compare the IdPrefix object to another one
         *
         * @param idPrefix another idPrefix to be compared to the idPrefix object
         * @return -1 if the parameters is smaller than the object <br>
         *          0 if equal <br>
         *          1 if greater
         */
        @Override
        /* kj: Intellij analyses

           'Not annotated parameter overrides @NotNull parameter'
        
           Netbeans analyses:
        
           Class defines compareTo(...) and uses Object.equals()
        
           This class defines a compareTo(...) method but inherits its equals() 
           method from java.lang.Object. Generally, the value of compareTo should
           return zero if and only if equals returns true. If this is violated, 
           weird and unpredictable failures will occur in classes such as 
           PriorityQueue. In Java 5 the PriorityQueue.remove method uses the 
           compareTo method, while in Java 6 it uses the equals method.
        
           From the JavaDoc for the compareTo method in the Comparable interface:
         
           It is strongly recommended, but not strictly required that 
           (x.compareTo(y)==0) == (x.equals(y)). Generally speaking, any class
           that implements the Comparable interface and violates this condition
           should clearly indicate this fact. The recommended language is "Note:
           this class has a natural ordering that is inconsistent with equals."
        
           kj: equals method has been implemented, extract annotation from the above remark
         */
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
         * kj: why this is needed, refer to the explanation above
         *
         * @param object kj: doc
         * @return
         */
        @Override
        public boolean equals(Object object) {

            if (object instanceof IdPrefix) {
                IdPrefix idPrefix = (IdPrefix) object;
                if (!this.identifier.equals(idPrefix.identifier)) {
                    return false;
                } else {
                    return this.prefix.equals(idPrefix.prefix);
                }
            }
            return super.equals(object);
        }

        /** 
         * Generate hashcode for storing and manipulating class instances
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
}
