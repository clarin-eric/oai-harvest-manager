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
 * <http://www.gnu.org/licenses/>
 *
 */

package nl.mpi.oai.harvester.cycle;

import nl.mpi.oai.harvester.generated.OverviewType;
import nl.mpi.oai.harvester.generated.ModeType;
import nl.mpi.oai.harvester.generated.ScenarioType;

/**
 * <br> Access to general harvest cycle properties stored as XML elements <br><br>
 *
 * A CycleAdaptor object associates itself with a CycleType object that was
 * created by the JAXB factory. When an adapter method needs to access a cycle
 * attribute, it invokes the corresponding method on the CycleType object. <br><br>
 *
 * This class depends on JAXB to generate classes representing the XML file. It
 * also depends on the JAXB factory for creating the elements used in the XML
 * file.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
class CyclePropertiesAdapter implements CycleProperties {

    // the JAXB created object representing elements from the XML file
    private final OverviewType overviewType;

    /**
     * Associate the adapter with a CycleType object
     *
     * @param xmlOverview overview marshalling object
     */
    public CyclePropertiesAdapter(XMLOverview xmlOverview) {

        this.overviewType = xmlOverview.overviewType;
    }

    /**
     * Return the mode attribute by invoking the appropriate generated
     * method
     *
     * @return the mode
     */
    @Override
    public Mode getHarvestMode() {

        ModeType modeType;
        modeType = overviewType.getMode();

        Mode mode;

        if (modeType == null) {
            mode = Mode.normal;
            overviewType.setMode(ModeType.NORMAL);
        } else {

            switch (modeType) {

                case REFRESH:
                    mode = Mode.refresh;
                    break;
                case RETRY:
                    mode = Mode.retry;
                    break;
                default:
                    mode = Mode.normal;
            }
        }

        return mode;
    }


    /**
     * Return the scenario attribute by invoking the appropriate generated
     * method
     *
     * @return the mode
     */
    @Override
    public Scenario getScenario() {

        ScenarioType scenarioType;
        scenarioType = overviewType.getScenario();

        Scenario scenario;

        if (scenarioType == null) {
            scenario = Scenario.ListRecords;
            overviewType.setScenario(ScenarioType.LIST_IDENTIFIERS);
        } else {

            switch (overviewType.getScenario()) {

                case LIST_PREFIXES:
                    scenario = Scenario.ListPrefixes;
                    break;
                case LIST_IDENTIFIERS:
                    scenario = Scenario.ListIdentifiers;
                    break;
                default:
                    scenario = Scenario.ListRecords;
            }
        }
        return scenario;
    }
}
