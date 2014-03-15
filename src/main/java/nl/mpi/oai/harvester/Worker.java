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

import java.util.List;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

/**
 * This class represents a single processing thread in the harvesting actions
 * workflow. In practice one worker takes care of one provider.
 *
 * @author Lari Lampen (MPI-PL)
 */
public class Worker implements Runnable {
    private static final Logger logger = Logger.getLogger(Worker.class);
    /** A standard semaphore is used to track the number of running threads. */
    private static Semaphore semaphore;

    /** The provider this worker deals with. */
    private Provider provider;

    /** List of sequences to be applied to the harvested metadata. */
    private List<ActionSequence> sequences;

    /**
     * Set the maximum number of concurrent worker threads.
     * 
     * @param num number of running threads that may not be exceeded
     */
    public static void setConcurrentLimit(int num) {
	semaphore = new Semaphore(num);
    }

    /**
     * Create a Worker object.
     * 
     * @param provider OAI-PMH provider that this thread will harvest
     * @param sequences list of actions to take on harvested metadata
     */
    public Worker(Provider provider, List<ActionSequence> sequences) {
	this.provider = provider;
	this.sequences = sequences;
    }

    /**
     * Start this worker thread. This method will block for as long as
     * necessary until a thread can be started without violating the limit.
     */
    public void startWorker() {
	for (;;) {
	    try {
		semaphore.acquire();
		break;
	    } catch (InterruptedException e) { }
	}
	Thread t = new Thread(this);
	t.start();
    }

    @Override
    public void run() {
	logger.info("Processing provider " + provider);
	for (ActionSequence as : sequences) {
	    // Break the inner loop after the first successful completion
	    // of an action sequence.
	    if (provider.performActions(as))
		break;
	}
	logger.info("Processing finished for " + provider);
	semaphore.release();
    }
}
