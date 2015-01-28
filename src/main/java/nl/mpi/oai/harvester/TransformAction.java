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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * This class represents the application of an XSL transformation to the
 * XML content of a metadata record.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class TransformAction implements Action {
    private static final Logger logger = Logger.getLogger(TransformAction.class);

    /** The file containing the XSL transformation. */
    private String xsltFile;

    /** Prepared XSL transformation object. */
    private Transformer transformer;

    /** Create a new transform action using the specified XSLT. */
    public TransformAction(String xsltFile) throws FileNotFoundException,
	    TransformerConfigurationException {
	this.xsltFile = xsltFile;
	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	Source xslSource = new StreamSource(new FileInputStream(xsltFile));
	transformer = transformerFactory.newTransformer(xslSource);
    }

    @Override
    public boolean perform(MetadataRecord record) {
	try {
	    DOMSource source = new DOMSource(record.getDoc());
	    DOMResult output = new DOMResult();
	    transformer.setParameter("provider_name",record.getOrigin().getName());
	    transformer.transform(source, output);
	    record.setDoc((Document) output.getNode());
	    return true;
	} catch (TransformerException ex) {
	    logger.error(ex);
	    return false;
	}
    }

	@Override
    public String toString() {
	return "transform using " + xsltFile;
    }

    // Transform actions differ if and only if the XSLT files differ.
    @Override
    public int hashCode() {
	return xsltFile.hashCode();
    }
    @Override
    public boolean equals(Object o) {
	if (o instanceof TransformAction) {
	    TransformAction t = (TransformAction)o;
	    return xsltFile.equals(t.xsltFile);
	}
	return false;
    }

    @Override
    public Action clone() {
	try {
	    // This is a deep copy. The new object has its own Transform object.
	    return new TransformAction(xsltFile);
	} catch (FileNotFoundException | TransformerConfigurationException ex) {
	    logger.error(ex);
	}
	return null;
    }
}
