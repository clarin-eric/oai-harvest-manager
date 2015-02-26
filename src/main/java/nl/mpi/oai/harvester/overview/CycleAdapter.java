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

package nl.mpi.oai.harvester.overview;

import nl.mpi.oai.harvester.generated.HarvestingType;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <br> Make available general harvesting cycle attributes <br><br>
 *
 * The attributes that determine a harvesting cycle are defined by an XML file
 * that takes a form that is defined by the harvesting.xsd file. Please refer
 * to the cycle interface and and endpoint interface for a description of the
 * semantics involved. <br><br>
 *
 * JAXB generates classes representing the XML files. It also provides a
 * factory for creating the elements in them. <br><br>
 *
 * A CycleAdaptor object associates itself with a HarvestingType object that was
 * created by the JAXB factory. When an adapter method needs to obtain a cycle
 * attribute, it invokes the corresponding method on the HarvestingType object.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class CycleAdapter implements Cycle {

    // the JAXB created object representing elements from the XML file
    private final HarvestingType harvesting;

    /**
     * Associate the adapter with a HarvestingType object
     *
     * @param harvesting JAXB representation of the harvesting overview file
     */
    public CycleAdapter(HarvestingType harvesting) {
        this.harvesting = harvesting;
    }

    /**
     * Return the mode attribute by invoking the appropriate generated
     * method
     *
     * @return the mode
     */
    @Override
    public Mode getHarvestMode() {

        Cycle.Mode mode = null;

        switch (harvesting.getMode()) {

            case NORMAL:
                mode = Mode.normal;
                break;
            case REFRESH:
                mode = Mode.refresh;
                break;
            case RETRY:
                mode = Mode.retry;
                break;
        }
        return mode;
    }

    /**
     * <br> Return the date attribute by invoking the appropriate generated
     * method <br><br>
     *
     * The method returns the date in YYYY-MM-DD format, a format the OAI
     * protocol accepts as a parameter to the verbs that allow for selective
     * harvesting. As a return value, the epoch zero date indicates that no
     * attempt to harvest the endpoint has been made.
     *
     * @return the date
     */
    @Override
    public String getHarvestFromDate() {

        // convert XMLGregorianCalendar to string

        XMLGregorianCalendar XMLDate;
        XMLDate = harvesting.getHarvestFromDate();

        return XMLDate.toString();
    }

    /**
     * Return the scenario attribute by invoking the appropriate generated
     * method
     *
     * @return the mode
     */
    @Override
    public String getScenario() {

        return harvesting.getScenario();
    }
}
