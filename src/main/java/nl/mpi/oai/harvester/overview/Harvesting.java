
package nl.mpi.oai.harvester.overview;

/**
 * General data needed for incremental harvesting. 
 * 
 * By using an abstract class, to some extend, the details of the implementation
 * are hidden: for the harvest manager it is not at all interesting where this 
 * data comes from. The overview class implements the abstract class by using 
 * the methods supplied by the generated classes. 
 * 
 * @author keeloo
 */
public abstract class Harvesting {

    /**
     * Mode of harvesting. 
     */
    public enum Mode {

        /**
         * In this mode harvesting is in principle incremental, however it will
         * effectively only be so if the endpoint allows for it.
         */
        normal,
        
        /**
         * Harvest those endpoint that gave rise to errors.
         */
        retry, 
        
        /**
         * Harvest records that were added to the endpoint after a specific date.
         */
        refresh
    };

    /**
     * Return the mode in which harvesting is to be carried out
     *
     * @return
     */
    public abstract Harvesting.Mode HarvestMode();

    /**
     * Returns the date to use when refreshing. 
     * 
     * Only records added to the endpoint after this data will be harvested. An 
     * epoch zero date on return means that there was no previous harvesting  
     * attempt.
     *
     * @return
     */
    public abstract String HarvestFromDate();
}
