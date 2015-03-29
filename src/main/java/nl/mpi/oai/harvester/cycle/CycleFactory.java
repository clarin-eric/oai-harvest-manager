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

package nl.mpi.oai.harvester.cycle;

import java.io.File;

/**
 * <br> Create cycle type object <br><br>
 *
 * The factory returns a cycle type object. Different types of overviews could
 * be supported. The cycle package supports overviews in the form of XML files
 * through XMLOverview class objects.
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class CycleFactory {

    /**
     * Create a new cycle
     *
     * @param overviewFile local XML file defining the overview
     * @return a cycle based on the overview
     */
    public Cycle createCycle(File overviewFile){

        return new XMLBasedCycle(overviewFile);
    }
}
