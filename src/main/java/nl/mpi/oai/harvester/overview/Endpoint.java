
package nl.mpi.oai.harvester.overview;

/**
 * On creation, an implemented object is tied to a particular endpoint.
 * 
 * In a way, this is a second level of abstraction. The first is the abstraction
 * made by the generated code. Not really abstract classes, the abstraction is in 
 * the generation of code: things you do not need to worry about. The abstraction
 * here leaves room for the generated types, and adds some specific things like 
 * the declaration of the mode. In other words: it adds characteristics not 
 * included or particular to XML.
 * 
 * @author keeloo
 */
public abstract class Endpoint {

    /**
     * Check if a retry is allowed for the endpoint. 
     * 
     * Only if true and if the harvesting manager is in retry mode, the 
     * records added to the endpoint after the harvested date will be eligible 
     * for a new harvesting attempt. 
     * 
     * @return true or false 
     */
    public abstract boolean retry();
    
    /**
     * Check if incremental harvesting is allowed.
     * 
     * Only if true, incremental harvesting, if specified at the general level,
     * will be carried out. In this case the date the the previous successful
     * harvest determines which records will be requested from the endpoint.
     * 
     * @return true or false
     */
    public abstract boolean allowIncrementalHarvest ();

    /**
     * Check if the endpoint is allowed to be harvested.
     * 
     * Only if true, there will be no harvesting for the endpoint regardless of
     * any other specification. This mode can be used to temporarily block 
     * harvesting, for example in case the endpoint fails to perform correctly.
     * 
     * @return true or false
     */
    public abstract boolean doNotHarvest ();

    /**
     * Return the date for incrementally harvesting.
     * 
     * Up to this date the records were harvested before. Incremental harvesting
     * means that only those records that were added to the endpoint after this 
     * date will be harvested.
     *
     * @return the date
     */
    public abstract String GetRecentHarvestDate();

    /**
     * Indicate success or failure.
     * 
     * In case of success, the date for incremental harvesting will be set to 
     * the current date. Otherwise the date will not be modified. Harvesting in 
     * retry mode will consider this endpoint for harvesting again. 
     *
     * @param done true in case of success, false otherwise
     */
    public abstract void DoneHarvesting(Boolean done);
}
