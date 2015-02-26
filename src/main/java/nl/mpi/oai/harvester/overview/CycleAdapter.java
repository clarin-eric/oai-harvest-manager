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

import nl.mpi.oai.harvester.generated.CycleType;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <br> Make available general harvesting cycle attributes <br><br>
 *
 * A CycleAdaptor object associates itself with a CycleType object that was
 * created by the JAXB factory. When an adapter method needs to access a cycle
 * attribute, it invokes the corresponding method on the CycleType object. <br><br>
 *
 * This class depends on JAXB to generate classes representing the XML file. It
 * also depends on the JAXB factory for creating the elements used in the XML
 * file.
 *
 * @author Kees Jan van de Looij (MPI-PL)
 */
public class CycleAdapter implements Cycle {

    // the JAXB created object representing elements from the XML file
    private final CycleType cycleType;

    /**
     * Associate the adapter with a CycleType object
     *
     * @param harvesting JAXB representation of the cycle overview file
     */
    public CycleAdapter(CycleType harvesting) {
        this.cycleType = harvesting;
    }

    /**
     * Return the mode attribute by invoking the appropriate generated
     * method
     *
     * @return the mode
     */
    @Override
    public Mode getHarvestMode() {

        Cycle.Mode mode;

        switch (cycleType.getMode()) {

            case NORMAL:
                mode = Mode.normal;
                break;
            case REFRESH:
                mode = Mode.refresh;
                break;
            case RETRY:
                mode = Mode.retry;
                break;
            default:
                mode = Mode.normal;
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
        XMLDate = cycleType.getHarvestFromDate();

        // kj: provide default

        if (XMLDate == null){
            // provide epoch zero as a default

            return "1970-01-01";
        } else {
            return XMLDate.toString();
        }
    }

    /**
     * Return the scenario attribute by invoking the appropriate generated
     * method
     *
     * @return the mode
     */
    @Override
    public String getScenario() {

        // kj: check values, could be changed into enumerated type

        return cycleType.getScenario();
    }
}
