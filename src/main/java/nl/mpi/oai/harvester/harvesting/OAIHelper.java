package nl.mpi.oai.harvester.harvesting;

import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.ctc.wstx.exc.WstxUnexpectedCharException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nl.mpi.oai.harvester.utils.DocumentSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.evt.XMLEvent2;

/**
 * <br> Helper implementing operations on documents transported by OAI
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class OAIHelper {
    private static Logger logger = LogManager.getLogger(OAIHelper.class);

    // only keep one XPath object for querying
    static public XPath xpath = null;

    /**
     * <br> Get the metadata prefixes referenced in a document <br><br>
     *
     * Note: since the metadata itself might not contain a reference to the
     * prefix, the document needs to be an OAI envelope.
     *
     * @param document the document
     * @return the metadata prefix
     */
    static public String getPrefix (DocumentSource document){
        // metadata prefix
        String prefix = null;

        if (document.hasDocument()) {
            // node in the document
            Node node = null;

            if (xpath == null){
                // set up XPath querying
                XPathFactory xpf = XPathFactory.newInstance();
                xpath = xpf.newXPath();
            }

            // look for the prefix in the request node
            try {
                node = (Node) xpath.evaluate(
                        "//*[local-name()='request']",
                        document.getDocument(), XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }

            if (node == null){
                // no request node, no metadata prefix
                prefix = null;
            } else {
                // found the request node, get the prefix attribute value
                if (node.getAttributes().getNamedItem("metadataPrefix") == null){
                    prefix = null; // no metadata prefix
                } else {
                    prefix = node.getAttributes().getNamedItem("metadataPrefix").getNodeValue();
                }
            }
        } else {
            int state = 1; // 1:START 0:STOP -1:ERROR
            try {
                XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
                xmlif.configureForConvenience();
                XMLStreamReader2 xmlr = (XMLStreamReader2) xmlif.createXMLStreamReader(document.getStream());
                while (state > 0) {
                    int eventType = xmlr.getEventType();
                    switch (state) {
                        case 1://START
                            switch (eventType) {
                                case XMLEvent2.START_ELEMENT:
                                    QName qn = xmlr.getName();
                                    if (qn.getLocalPart().equals("request")) {
                                        prefix = xmlr.getAttributeValue(null,"metadataPrefix");
                                        if (prefix != null)
                                            state = 0;//STOP
                                    }
                                    break;
                            }
                            break;
                    }
                    outer:
                    if (xmlr.hasNext())
                        try {
                            xmlr.next();
                        } catch (WstxUnexpectedCharException ex) {
                            logger.info("Invalid char found in XML, skipping the current one and look for next one");
                        }
                    else
                        state = state == 1? 0: -1;// if START then STOP else ERROR
                }
            } catch (XMLStreamException ex) {
                logger.error("problem finding prefix in the XML stream!",ex);
                state = -1;//ERROR
            }
            if (state < 0 || prefix == null)
                logger.debug("couldn't find prefix in the XML stream!");
            else
                logger.debug("found prefix["+prefix+"] in the XML stream!");
        }

        return prefix;
    }
}
