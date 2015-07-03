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

package nl.mpi.oai.harvester.control;

import java.util.Collections;
import java.util.LinkedList;

/**
 * A very simple generic pool that manages access to resources that
 * are not thread-safe in a multi-thread application.
 *
 * Alternative: A more comprehensive resource pool implementation is
 * available in the Apache Commons framework. That one has more
 * configuration options but lacks support for Java generics.
 * 
 * @param <T> type of resources to pool
 *
 * @author Lari Lampen (MPI-PL)
 */
public class ResourcePool<T> {
    private final LinkedList<T> resources;

    public ResourcePool(T[] resources) {
	this.resources = new LinkedList<>();
	Collections.addAll(this.resources, resources);
    }

    /**
     * Release a resource obtained from this class. For simplicity,
     * there is no error checking, so the caller must not do something
     * silly like try to re-release an already released resource.
     *
     * @param r A resource obtained from this pool and not released yet
     */
    public synchronized void release(T r) {
	resources.add(r);
	notify();
    }

    /**
     * Obtain a resource from the pool.
     *
     * @return A resource not held by any other caller
     * */
    public synchronized T get() {
	while (resources.isEmpty()) {
	    try {
		wait();
	    } catch (InterruptedException ignored) { }
	}
	return resources.removeFirst();
    }

    /**
     * Returns the number of resources available in the pool at the moment it
     * is called.
     * 
     * @return number of resources available
     */
    public int getNumAvailable() {
	return resources.size();
    }
}
