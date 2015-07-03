package nl.mpi.oai.harvester.harvesting;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * <br> Helper implementing operations on documents transported by OAI
 *
 * @author Kees Jan van de Looij (Max Planck Institute for Psycholinguistics)
 */
public class OAIHelper {

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
    static public String getPrefix (Document document){

        // node in the document
        Node node = null;

        // metadata prefix
        String prefix;

        if (xpath == null){
            // set up XPath querying
            XPathFactory xpf = XPathFactory.newInstance();
            xpath = xpf.newXPath();
        }

        // look for the prefix in the request node
        try {
            node = (Node) xpath.evaluate(
                    "//*[local-name()='request']",
                    document, XPathConstants.NODE);
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

        return prefix;
    }
}
