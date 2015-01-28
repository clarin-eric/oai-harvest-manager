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
    protected String suffix;

    /**
     * Create a new save action.
     * 
     * @param dir output directory to save to
     * @param suffix suffix to be added to identifier to generate filename
     */
    public SaveAction(OutputDirectory dir, String suffix) {
	this.dir = dir;
	this.suffix = (suffix == null) ? "" : suffix;
    }

    @Override
    public boolean perform(MetadataRecord record) {

        OutputStream os = null;
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(record.getDoc());

            os = Files.newOutputStream(chooseLocation(record));
            StreamResult result = new StreamResult(os);

            transformer.transform(source, result);
            return true;
        } catch (TransformerException | IOException ex) {
            logger.error(ex);
            return false;
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Simply choose location to save in.
     * 
     * @param record metadata record
     * @return path to new file in suitable location
     */
    protected Path chooseLocation(MetadataRecord record) throws IOException {
	return dir.placeNewFile(Util.toFileFormat(record.getId()) + suffix);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder("save to ");
	sb.append(dir);
	if (!suffix.isEmpty())
	    sb.append(" using suffix ").append(suffix);
	return sb.toString();
    }

    // Save actions are equal iff the directories are the same.
    @Override
    public int hashCode() {
	return dir.hashCode() + 29 * suffix.hashCode();
    }
    @Override
    public boolean equals(Object o) {
	if (o instanceof SaveAction && !(o instanceof SaveGroupedAction)) {
	    SaveAction a = (SaveAction)o;
	    // OK, this is pretty stupid, but works...
	    return this.toString().equals(a.toString());
	}
	return false;
    }

    @Override
    public Action clone() {
	// This is a shallow copy, resulting in multiple references to a single
	// OutputDirectory, which is as intended.
	return new SaveAction(dir, suffix);
    }
}
