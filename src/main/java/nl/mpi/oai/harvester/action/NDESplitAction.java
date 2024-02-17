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

import nl.mpi.oai.harvester.metadata.Metadata;
import nl.mpi.oai.harvester.metadata.Record;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.evt.XMLEvent2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * This action corresponds to splitting the OAI-PMH envelope with multiple records
 * into multiple ones with each one harvested metadata record.
 *
 * @author Menzo Windhouwer (CLARIN-ERIC)
 */
public class NDESplitAction implements Action {

    private final Logger logger = LogManager.getLogger(NDESplitAction.class);

    private final XPath xpath;
    private final DocumentBuilder db;

    private enum State {
        START, RESULTS, RESULT, DATASET, URI, END_RESULT, STOP, ERROR
    }

    public NDESplitAction() throws ParserConfigurationException {
        XPathFactory xpf = XPathFactory.newInstance();
        xpath = xpf.newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();
    }

    @Override
    public boolean perform(List<Record> records) {
        logger.info("hi, i am in nde split action");
        logger.info("Splitting " + records.size() + " records");
        List<Metadata> newRecords = new ArrayList<>();

        for (Record rec : records) {
            Metadata record = (Metadata) rec;

            XMLEventReader reader = null;
            XMLEventWriter writer = null;
            try {
                XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
                xmlif.configureForConvenience();
                XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
                xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
                XMLEventFactory xmlef = XMLEventFactory.newInstance();
                reader = xmlif.createXMLEventReader(record.getStream());
                writer = null;
                Stack<XMLEvent> outer = new Stack<>();
                Stack<XMLEvent> inner = new Stack<>();

                if (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();

                    State state = State.START;
                    int depth = 0;
                    String status = null;
                    String ds = null;
                    ByteArrayOutputStream baos = null;
                    while (!state.equals(state.STOP) && !state.equals(state.ERROR)) {
                        logger.debug("BEGIN loop: state["+state+"] event["+event+"]["+event.getEventType()+"]");
                        int eventType = event.getEventType();
                        switch (state) {
                            case START:
                                logger.debug("state[START]");
                                switch (eventType) {
                                    case XMLEvent2.START_ELEMENT:
                                        QName qn = event.asStartElement().getName();
                                        if (qn.getLocalPart().equals("results")) {
                                            state = State.RESULTS;
                                        }
                                        outer.push(event);
                                        break;
                                    default:
                                        outer.push(event);
                                }
                            case RESULTS:
                                logger.debug("state[RESULTS]");
                                switch (eventType) {
                                    case XMLEvent.START_ELEMENT:
                                        QName qn = event.asStartElement().getName();
                                        if (qn.getLocalPart().equals("result")) {
                                            state = State.RESULT;
                                        }
                                        inner.push(event);
                                        break;
                                    default:
                                        inner.push(event);
                                }
                            case RESULT:
                                logger.debug("state[RESULT]");
                                switch (eventType) {
                                    case XMLEvent.START_ELEMENT:
                                        QName qn = event.asStartElement().getName();
                                        if (qn.getLocalPart().equals("binding")) {
                                            if (event.asStartElement().getAttributeByName(new QName("name")).equals("dataset"))
                                               state = State.DATASET;
                                        }
                                        inner.push(event);
                                        break;
                                    default:
                                        inner.push(event);
                                }
                            case DATASET:
                                logger.debug("state[DATASET]");
                                switch (eventType) {
                                    case XMLEvent.START_ELEMENT:
                                        QName qn = event.asStartElement().getName();
                                        if (qn.getLocalPart().equals("uri")) {
                                            state = State.URI;
                                        }
                                        inner.push(event);
                                        break;
                                    default:
                                        inner.push(event);
                                }
                            case URI:
                                logger.debug("state[URI]");
                                String uri = null;
                                switch (eventType) {
                                    case XMLEvent.CHARACTERS:
                                        uri = event.asCharacters().getData();
                                        inner.push(event);
                                        break;
                                    default:
                                        inner.push(event);
                                }
                                if (ds == null || !ds.equals(uri)) {
                                    // close record (if any)
                                    if (writer != null) {
                                        writer.add(xmlef.createEndElement("","http://www.w3.org/2005/sparql-results#","results"));
                                        writer.add(xmlef.createEndElement("","http://www.w3.org/2005/sparql-results#","sparql"));
                                        writer.close();
                                        newRecords.add(new Metadata(ds, "", new ByteArrayInputStream(baos.toByteArray()), record.getOrigin(),false, false));
                                    }
                                    // new record
                                    writer = xmlof.createXMLEventWriter(new ByteArrayOutputStream());
                                    // pop outer stack
                                    Stack<XMLEvent> o = new Stack<>();
                                    while(!outer.isEmpty()) {
                                        o.push(outer.peek());
                                        writer.add(outer.pop());
                                    }
                                    outer = o;
                                    // pop inner stack
                                    while(!inner.isEmpty()) {
                                        writer.add(inner.pop());
                                    }
                                } else {
                                    // add to record
                                    // pop inner stack
                                    while(!inner.isEmpty()) {
                                        writer.add(inner.pop());
                                    }
                                }
                                state = State.END_RESULT;
                                break;
                            case END_RESULT:
                                logger.debug("state[END_RESULT]");
                                switch (eventType) {
                                    case XMLEvent.END_ELEMENT:
                                        QName qn = event.asEndElement().getName();
                                        if (qn.getLocalPart().equals("result")) {
                                            state = State.RESULTS;
                                        }
                                        writer.add(event);
                                        break;
                                    default:
                                        writer.add(event);
                                }
                        }
                        if (reader.hasNext())
                            event = reader.nextEvent();
                        else
                            state = (state == State.START ? State.STOP : State.ERROR);// if START then STOP else ERROR
                        logger.debug("END loop: state["+state+"] event["+event+"]["+event.getEventType()+"]");
                    }
                    if (state.equals(State.ERROR))
                        logger.error("the XML was not properly processed!");
                }
                if (newRecords.isEmpty()) {
                    logger.error("No records were found in this resultset[" + record.getId() + "]");
                }
            } catch (Throwable ex) {
//            } catch (XMLStreamException ex) {
                logger.error("Hi, Throwable", ex);
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
        records.clear();
        records.addAll(newRecords);
        return true;
    }

    @Override
    public String toString() {
        return "nde-split";
    }

    // All split actions are equal.
    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NDESplitAction;
    }

    @Override
    public Action clone() {
        try {
            // All split actions are the same. This is effectively a "deep"
            // copy since it has its own XPath object.
            return new NDESplitAction();
        } catch (ParserConfigurationException ex) {
            logger.error(ex);
        }
        return null;
    }
}
