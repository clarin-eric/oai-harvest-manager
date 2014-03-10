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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;

/**
 * This class represents the action of saving a record onto the file system.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class SaveAction implements Action {
    private static final Logger logger = Logger.getLogger(SaveAction.class);

    protected OutputDirectory dir;

    /** Create a new save action using the specified output directory. */
    public SaveAction(OutputDirectory dir) {
	this.dir = dir;
    }

    @Override
    public boolean perform(MetadataRecord record) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(record.getDoc());
	    
	    OutputStream os = Files.newOutputStream(chooseLocation(record));
	    StreamResult result = new StreamResult(os);

            transformer.transform(source, result);
	    return true;
        } catch (TransformerException | IOException ex) {
	    logger.error(ex);
	    return false;
        }
    }

    /**
     * Simply choose location to save in.
     * 
     * @param record metadata record
     * @return path to new file in suitable location
     */
    protected Path chooseLocation(MetadataRecord record) throws IOException {
	return dir.placeNewFile(record.getId());
    }

    @Override
    public String toString() {
	return "save to " + dir;
    }
}
