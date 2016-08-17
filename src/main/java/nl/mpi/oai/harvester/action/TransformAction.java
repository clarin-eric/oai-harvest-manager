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

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import nl.mpi.oai.harvester.metadata.Metadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * This class represents the application of an XSL transformation to the
 * XML content of a metadata record.
 * 
 * @author Lari Lampen (MPI-PL)
 */
public class TransformAction implements Action {
    private static final Logger logger = LogManager.getLogger(TransformAction.class);

    /** The file containing the XSL transformation. */
    private String xsltFile;

    /** The directory containing cached resources. */
    private Path cacheDir;
    
    /** Transformer factory */
    TransformerFactory factory;

    /** Prepared XSL transformation object. */
    private Templates templates;
    
    /** A standard semaphore is used to track the number of running transforms. */
    private Semaphore semaphore;

    /** 
     * Create a new transform action using the specified XSLT. 
     * 
     * @param xsltFile the XSL stylesheet
     * @param cacheDir the directory to cache results of resource requests
     * @param maxJobs the maximum number of concurrent transforms
     * @throws FileNotFoundException stylesheet couldn't be found
     * @throws TransformerConfigurationException there is a problem with the stylesheet
     */
    public TransformAction(String xsltFile,Path cacheDir,int maxJobs) throws FileNotFoundException, TransformerConfigurationException {
        this(xsltFile,cacheDir,(maxJobs>0?new Semaphore(maxJobs):null));
    }
    
    /** 
     * Create a new transform action using the specified XSLT. 
     * 
     * @param xsltFile the XSL stylesheet
     * @param cacheDir the directory to cache results of resource requests
     * @param semaphore a semaphore to control the concurrent number of transforms
     * @throws FileNotFoundException stylesheet couldn't be found
     * @throws TransformerConfigurationException there is a problem with the stylesheet
     */
    public TransformAction(String xsltFile,Path cacheDir,Semaphore semaphore) throws FileNotFoundException, TransformerConfigurationException {
	this.xsltFile = xsltFile;
        this.cacheDir = cacheDir;
        this.semaphore = semaphore;
	factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        if(factory instanceof TransformerFactoryImpl) {
            TransformerFactoryImpl transformerFactoryImpl = ((TransformerFactoryImpl)factory);
            logger.debug("Telling Saxon to send messages as warnings to logger");
            final Configuration tfConfig = transformerFactoryImpl.getConfiguration();
            tfConfig.setMessageEmitterClass("net.sf.saxon.serialize.MessageWarner");
            if (cacheDir != null) {
                logger.debug("Setting the URLResolve to cache in "+cacheDir);
                transformerFactoryImpl.setURIResolver(new TransformActionURLResolver(transformerFactoryImpl.getURIResolver()));
            }
        }
        factory.setErrorListener(new TransformActionErrorListener());
        Source xslSource = null;
        if (xsltFile.startsWith("http:") || xsltFile.startsWith("https:"))
            xslSource = new StreamSource(xsltFile);
        else
            xslSource = new StreamSource(new FileInputStream(xsltFile),xsltFile);
	templates = factory.newTemplates(xslSource);
    }

    @Override
    public boolean perform(List<Metadata> records) {
        for (Metadata record:records) {
            try {
                if (semaphore!=null) {
                    for (;;) {
                        try {
                            logger.debug("request transform action");
                            semaphore.acquire();
                            logger.debug("acquired transform action");
                            break;
                        } catch (InterruptedException e) { }
                    }
                }
                Transformer transformer = templates.newTransformer();
                Source source = null;
                Result output = null;
                if (record.hasStream()) {
                    source = new SAXSource(record.getSource());
                    output = new StreamResult(new ByteArrayOutputStream());
                } else {
                    source = new DOMSource(record.getDoc());
                    output = new DOMResult();
                }
                transformer.setParameter("provider_name",record.getOrigin().getName());
                transformer.setParameter("record_identifier",record.getId());
                transformer.transform(source, output);
                if (record.hasStream()) {                 
                    byte[] bytes = ((ByteArrayOutputStream)((StreamResult)output).getOutputStream()).toByteArray();
                    record.setStream(new ByteArrayInputStream(bytes));
                    logger.debug("transformed to XML stream with ["+bytes.length+"] bytes");
                } else {
                    record.setDoc((Document) ((DOMResult)output).getNode());
                    logger.debug("transformed to XML doc with ["+XPathFactory.newInstance().newXPath().evaluate("count(//*)", record.getDoc())+"] nodes");
                }
            } catch (TransformerException | XPathExpressionException ex) {
                logger.error("Transformation error: ",ex);
                return false;
            } finally {
                if (semaphore!=null) {
                    semaphore.release();
                    logger.debug("released transform action");
                }
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
	    return new TransformAction(xsltFile,cacheDir,semaphore);
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
    
    class TransformActionURLResolver implements URIResolver {
        
        private URIResolver resolver;
        
        public TransformActionURLResolver(URIResolver resolver) {
            this.resolver = resolver;
        }
        
        public Source resolve(String href, String base) throws TransformerException {
            logger.debug("Transformer resolver: resolve("+href+","+base+")");
            String uri = href;
            if (base != null && !base.equals("")) {
                try {
                    uri = (new URL(new URL(base),href)).toString();
                } catch (MalformedURLException ex) {
                    logger.error("Transformer resolver: couldn't resolve("+href+","+base+") continuing with just "+href,ex);
                }
            }
            String cacheFile = uri.replaceAll("[^a-zA-Z0-9]", "_");
            logger.debug("Transformer resolver: check cache for "+cacheFile);
            Source res = null;
            if (Files.exists(cacheDir.resolve(cacheFile))) {
                res = new StreamSource(cacheDir.resolve(cacheFile).toFile());
                logger.debug("Transformer resolver: loaded "+cacheFile+" from cache");
            } else {
                res = resolver.resolve(href, base);
                Result result = new StreamResult(cacheDir.resolve(cacheFile).toFile());
                Transformer xformer = factory.newTransformer();
                xformer.transform(res, result);
                logger.debug("Transformer resolver: stored "+cacheFile+" in cache");
            }
            return res;
        }
    }
}
