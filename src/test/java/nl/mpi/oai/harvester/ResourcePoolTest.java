/*
 * Copyright (C) 2014, The Max Planck Institute for
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

package nl.mpi.oai.harvester;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for resource pool class.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class ResourcePoolTest {
    /**
     * Test of get and release. Since the underlying class does no checking
     * (by design), this only tests the simplest non-error scenario.
     */
    @Test
    public void testGetAndRelease() {
	System.out.println("release");
	String[] t = {"string"};
	ResourcePool<String> pool = new ResourcePool<>(t);
	assertEquals(pool.getNumAvailable(), 1);
	String r = pool.get();
	assertEquals(t[0], r);
	assertEquals(pool.getNumAvailable(), 0);
	pool.release(r);
	assertEquals(pool.getNumAvailable(), 1);
    }    
}
