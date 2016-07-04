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

package nl.mpi.oai.harvester.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import nl.mpi.oai.harvester.metadata.Metadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.evt.XMLEvent2;


/**
 * This action corresponds to splitting the OAI-PMH envelope with multiple records
 * into multiple ones with each one harvested metadata record.
 * 
 * @author Menzo Windhouwer (CLARIN-ERIC)
 */
public class SplitAction implements Action {

    private final Logger logger = LogManager.getLogger(SplitAction.class);

    private final XPath xpath;
    private final DocumentBuilder db;

    public SplitAction() throws ParserConfigurationException {
	XPathFactory xpf = XPathFactory.newInstance();
	xpath = xpf.newXPath();	
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	db = dbf.newDocumentBuilder();
    }

    @Override
    public boolean perform(List<Metadata> records) {
        List<Metadata> newRecords = new ArrayList();
        while (!records.isEmpty()) {
            Metadata record = records.remove(0);
            
            if (record.hasDoc()) {

                // Get the child nodes of the "metadata" tag;
                // that's the content of the response without the
                // OAI-PMH envelope.

                NodeList content = null;
                try {
                    content = (NodeList) xpath.evaluate("//*[local-name()='record']",
                            record.getDoc(), XPathConstants.NODESET);
                } catch (XPathExpressionException ex) {
                    logger.error(ex);
                }

                if ((content != null) && (content.getLength()>0)) {
                    for (int i=0;i<content.getLength();i++) {
                        Document doc = db.newDocument();
                        Node copy = doc.importNode(content.item(i), true);
                        doc.appendChild(copy);
                        String id = "";
                        try {
                            String status = (String) xpath.evaluate(
                                "./*[local-name()='header']/@status",
                                content.item(i),XPathConstants.STRING);
                            id = (String) xpath.evaluate(
                                "./*[local-name()='header']/*[local-name()='identifier']",
                                content.item(i),XPathConstants.STRING);
                            if (!status.equals("deleted")) {
                                logger.debug("split off XML doc["+i+"]["+id+"] with ["+xpath.evaluate("count(//*)", doc)+"] nodes");
                                newRecords.add(new Metadata(
                                            id, record.getPrefix(),
                                            doc, record.getOrigin(), false, false)
                                );
                            } else
                                logger.warn("record["+id+"] is marked as deleted");
                        } catch (XPathExpressionException ex) {
                            logger.error(ex);
                        }
                    }
                } else
                    logger.warn("No content was found in this envelope["+record.getId()+"]");
            } else {
                XMLEventReader reader = null;
                XMLEventWriter writer = null;
                try {
                    XMLInputFactory2 xmlInputFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
                    xmlInputFactory.configureForConvenience();
                    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
                    xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
                    
                    ByteArrayOutputStream baos = null;
                    int i = 0;
                    
                    reader = xmlInputFactory.createXMLEventReader(record.getStream());
                    writer = null;
                    
                    if (reader.hasNext()) {
                        XMLEvent event = reader.nextEvent();

                        int state = 1; // 1:START 2:RECORD 3:HEADER 4:ID 0:STOP -1:ERROR
                        int depth = 0;
                        String status = null;
                        String id = null;
                        while (state > 0) {
                            //logger.debug("BEGIN loop: state["+state+"] event["+event+"]["+event.getEventType()+"]");
                            int eventType = event.getEventType();
                            switch (state) {
                                case 1://START
                                    //logger.debug("state[START]");
                                    switch (eventType) {
                                        case XMLEvent2.START_ELEMENT:
                                            QName qn = event.asStartElement().getName();
                                            if (qn.getLocalPart().equals("record")) {
                                                state = 2;//RECORD
                                                i++;
                                                baos = new ByteArrayOutputStream();
                                                writer = xmlOutputFactory.createXMLEventWriter(baos);
                                                writer.add(event);
                                                status = null;
                                                id = null;
                                                depth = 1;
                                            }
                                            break;
                                    }
                                    break;
                                case 2://RECORD
                                    //logger.debug("state[RECORD] depth["+depth+"]");
                                    switch (eventType) {
                                        case XMLEvent2.START_ELEMENT:
                                            depth++;
                                            //logger.debug("start["+event.asStartElement().getName()+"] depth["+depth+"]");
                                            if (depth==2 && event.asStartElement().getName().getLocalPart().equals("header")) { //record/header
                                                Attribute attr = event.asStartElement().getAttributeByName(new QName("status"));
                                                if (attr!=null) {
                                                    status = attr.getValue();//record/header/@status
                                                    //logger.debug("status["+status+"]");
                                                }
                                                state = 3;//HEADER
                                            }
                                            break;
                                        case XMLEvent2.END_ELEMENT:
                                            //logger.debug("end["+event.asEndElement().getName()+"] depth["+depth+"]");
                                            if (depth==1) { 
                                                if (event.asEndElement().getName().getLocalPart().equals("record")) {
                                                    state = 1;//START
                                                } else {
                                                    logger.error("record XML element out of sync! Expected record got ["+event.asEndElement().getName()+"]");
                                                    state = -1;//ERROR
                                                }
                                            }
                                            depth--;
                                            break;
                                    }
                                    writer.add(event);
                                    if (state==1) {//START
                                        writer.close();
                                        if (status == null || !status.equals("deleted")) {
                                            logger.debug("split off XML stream["+i+"]["+id+"] with ["+baos.size()+"] bytes");
                                            newRecords.add(new Metadata(
                                                id, record.getPrefix(),
                                                new ByteArrayInputStream(baos.toByteArray()),
                                                record.getOrigin(),
                                                false, false)
                                            );
                                        }
                                        writer = null;
                                        baos = null;
                                        status = null;
                                        id = null;
                                    }
                                    break;
                                case 3://HEADER
                                    //logger.debug("state[HEADER] depth["+depth+"]");
                                    switch (eventType) {
                                        case XMLEvent2.START_ELEMENT:
                                            depth++;
                                            //logger.debug("start["+event.asStartElement().getName()+"] depth["+depth+"]");
                                            if (event.asStartElement().getName().getLocalPart().equals("identifier")) {//record/header/identifier
                                                state = 4;//ID
                                            }
                                            break;
                                        case XMLEvent2.END_ELEMENT:
                                            //logger.debug("end["+event.asEndElement().getName()+"] depth["+depth+"]");
                                            if (depth==2) { 
                                                if (event.asEndElement().getName().getLocalPart().equals("header")) {
                                                    state = 2;//RECORD
                                                } else {
                                                    logger.error("header XML element out of sync! Expected header got ["+event.asEndElement().getName()+"]");
                                                    state = -1;//ERROR
                                                }
                                            }
                                            depth--;
                                            break;
                                    }
                                    writer.add(event);
                                    break;
                                case 4://ID
                                    //logger.debug("state[ID] depth["+depth+"]");
                                    switch (eventType) {
                                        case XMLEvent2.CHARACTERS:
                                            id = event.asCharacters().getData();//record/header/identifier/text()
                                            //logger.debug("id["+id+"]");
                                            state = 3;//HEADER
                                            break;
                                        default:
                                            state = -1;//ERROR
                                            logger.error("identifier XML element out of sync!");
                                            break;
                                    }
                                    writer.add(event);
                                    break;
                            }
                            if (reader.hasNext())
                                event = reader.nextEvent();
                            else
                                state = state == 1? 0: -1;// if START then STOP else ERROR
                            //logger.debug("END loop: state["+state+"] event["+event+"]["+event.getEventType()+"]");
                        }
                        if (state < 0)
                            logger.error("the XML was not properly processed!");
                    }
                    if (i==0) {
                        logger.error("No content was found in this envelope["+record.getId()+"]");
                    }                                       
                } catch (XMLStreamException ex) {
                    logger.error("",ex);
                } finally {
                    try {
                        if (reader != null)
                            reader.close();
                        if (writer != null)
                            writer.close();
                    } catch (XMLStreamException ex) {
                    }
                }
            }
        }
        records.clear();
        records.addAll(newRecords);
        return true;
    }

    @Override
    public String toString() {
	return "split";
    }

    // All split actions are equal.
    @Override
    public int hashCode() {
	return 1;
    }
    @Override
    public boolean equals(Object o) {
	if (o instanceof SplitAction) {
	    return true;
	}
	return false;
    }

    @Override
    public Action clone() {
	try {
	    // All split actions are the same. This is effectively a "deep"
	    // copy since it has its own XPath object.
	    return new SplitAction();
	} catch (ParserConfigurationException ex) {
	    logger.error(ex);
	}
	return null;
    }
}
