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

package nl.mpi.oai.harvester.action;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;

import nl.mpi.oai.harvester.metadata.Metadata;
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
    private Templates templates;

    /** 
     * Create a new transform action using the specified XSLT. 
     * 
     * @param xsltFile the XSL stylesheet
     * @throws FileNotFoundException stylesheet couldn't be found
     * @throws TransformerConfigurationException there is a problem with the stylesheet
     */
    public TransformAction(String xsltFile) throws FileNotFoundException, TransformerConfigurationException {
	this.xsltFile = xsltFile;
	TransformerFactory transformerFactory = TransformerFactory.newInstance();
        if(transformerFactory instanceof TransformerFactoryImpl) {
            logger.debug("Telling Saxon to send messages as warnings to logger");
            final Configuration tfConfig = ((TransformerFactoryImpl)transformerFactory).getConfiguration();
            tfConfig.setMessageEmitterClass("net.sf.saxon.serialize.MessageWarner");
        }
        transformerFactory.setErrorListener(new TransformActionErrorListener());
	Source xslSource = new StreamSource(new FileInputStream(xsltFile));
	templates = transformerFactory.newTemplates(xslSource);
    }

    @Override
    public boolean perform(List<Metadata> records) {
        for (Metadata record:records) {
            try {
                Transformer transformer = templates.newTransformer();
                DOMSource source = new DOMSource(record.getDoc());
                DOMResult output = new DOMResult();
                transformer.setParameter("provider_name",record.getOrigin().getName());
                transformer.setParameter("record_identifier",record.getId());
                transformer.transform(source, output);
                record.setDoc((Document) output.getNode());
                logger.debug("transformed to XML doc with ["+XPathFactory.newInstance().newXPath().evaluate("count(//*)", record.getDoc())+"] nodes");
            } catch (TransformerException | XPathExpressionException ex) {
                logger.error(ex);
                return false;
            }
        }
        return true;
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
    
    class TransformActionErrorListener implements ErrorListener {

        public TransformActionErrorListener() {
            logger.debug("Redirecting XSLT warnings and errors to this logger");
        }

        @Override
        public void warning(TransformerException te) throws TransformerException {
            logger.warn("Transformer warning: "+te.getMessageAndLocation());
            //logger.debug("Transformation warning stacktrace", te);
        }

        @Override
        public void error(TransformerException te) throws TransformerException {
            // errors will be caught by the service, so swallow here except in debug
            logger.debug("Transformer error", te);
        }

        @Override
        public void fatalError(TransformerException te) throws TransformerException {
            // errors will be caught by the service, so swallow here except in debug
            logger.debug("Transformer fatal error", te);
        }
    }
}
