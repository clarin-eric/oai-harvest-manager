package nl.mpi.oai.harvester.control;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class CentreRegistrySetDefinition {

    private final String setSpec;
    private final String setType;

    public CentreRegistrySetDefinition(String setSpec, String setType) {
        this.setSpec = setSpec;
        this.setType = setType;
    }

    public String getSetSpec() {
        return setSpec;
    }

    public String getSetType() {
        return setType;
    }

    @Override
    public String toString() {
        return "CentreRegistrySetDefinition{" + "setSpec=" + setSpec + ", setType=" + setType + '}';
    }

}
