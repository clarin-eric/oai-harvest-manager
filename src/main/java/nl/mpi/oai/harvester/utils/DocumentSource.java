/*
 * Copyright (C) 2016, CLARIN ERIC.
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

package nl.mpi.oai.harvester.utils;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author menzowi
 */
public class DocumentSource {
    
    private static final Logger logger = LogManager.getLogger(DocumentSource.class);
    
    private String id = null;
    
    private Document doc = null;
    private InputStream str = null;
    
    public DocumentSource(Document doc) {
        this("",doc);
    }
    
    public DocumentSource(InputStream str) {
        this("",str);
    }
    
    public DocumentSource(String id, Document doc) {
        this.id = id;
        this.doc = doc;
    }
    
    public DocumentSource(String id, InputStream str) {
        this.id = id;
        this.str = str;
    }
    
    public boolean hasDocument() {
        return (doc!=null);
    }
    
    public boolean hasStream() {
        return (str!=null);
    }

    public boolean hasSource() {
        return (str!=null);
    }
    
    public Document getDocument() {
        if (doc==null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(getSource());         
                str = null;
                logger.debug("switched from stream to tree for DocumentSource["+id+"]",new Throwable());
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                logger.error(ex.getMessage(),ex);
                logger.debug("failed to switch from stream to tree for DocumentSource["+id+"]");
            }
        }
        return doc;
    }
    
    public InputSource getSource() {
        return new InputSource(getStream());
    }
    
    public InputStream getStream() {
        if (str==null)
            return null;
        try {
            str.reset();
        } catch (IOException ex) {
            logger.error(ex.getMessage(),ex);
            logger.debug("failed to reset stream for DocumentSource["+id+"]");
        }
        return str;
    }
    
    public void setDocument(Document doc) {
        if (str!=null)
                logger.debug("switched from stream to tree for DocumentSource["+id+"]",new Throwable());
        this.doc = doc;
        this.str = null;
    }
    
    public void setStream(InputStream str) {
        if (doc!=null)
                logger.debug("switched from tree to stream for DocumentSource["+id+"]",new Throwable());
        this.str = str;
        this.doc = null;
    }
    
    public void close() {
        if (str!=null) {
            try {
                str.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(),ex);
                logger.debug("failed to close stream for DocumentSource["+id+"]");
            }
        }
    }
}
