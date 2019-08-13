package com.ibagroup.wf.intelia.core.utils.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibagroup.wf.intelia.core.config.ConfigurationManager;

/**
 * SOAP service utility class.
 */
public class SoapServiceUtil {
    // A - Abort
    // V - Validation error
    // E - Error
    // X - Abort
    // P - Partial
    // F - Failure
    private static final String[] SOAP_RESPONSE_ERROR_TYPES = {"A", "V", "E", "X", "P", "F"};
    // S - Success
    // I - Informational
    // W - Warning
    private static final String[] SOAP_RESPONSE_SUCCESS_TYPES = {"S"/* , "I", "W" */ };

    /**
     * Handle SOAP response and check if it was successful or not.
     *
     * @param <T> - String or SoapMessage
     *
     * @param soapResponse String representing SOAP XML response
     * @return True if response doesn't contain any errors AND has explicit informaton about
     *         success, false - otherwise
     */
    public static <T> boolean handleSoapResponse(T soapResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;

            builder = factory.newDocumentBuilder();

            String soapResponseStr = soapResponse.toString();
            if (soapResponse instanceof SOAPMessage) {
                SOAPMessage soapMsg = (SOAPMessage) soapResponse;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                soapMsg.writeTo(out);
                soapResponseStr = new String(out.toByteArray(), StandardCharsets.UTF_8);
            }

            ByteArrayInputStream input = new ByteArrayInputStream(soapResponseStr.getBytes(StandardCharsets.UTF_8));
            Document document = builder.parse(input);

            // Evaluate XPath against Document itself
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("//message/messageType", document.getDocumentElement(), XPathConstants.NODESET);

            boolean successFound = false;
            boolean errorFound = false;
            for (int i = 0; i < nodes.getLength(); ++i) {
                Element e = (Element) nodes.item(i);
                if (ArrayUtils.contains(SOAP_RESPONSE_ERROR_TYPES, e.getTextContent())) {
                    errorFound = true; 
                    break;
                } else if (ArrayUtils.contains(SOAP_RESPONSE_SUCCESS_TYPES, e.getTextContent())) {
                    successFound = true;
                }
            }
            boolean isSuccessfulResponse = !errorFound && successFound;

            return isSuccessfulResponse;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getNowInServiceString() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
    }

    public static String getNowInServiceStringWithoutTimeZone() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
    }

    public static Object evaluate(String xpathStr, String doc, QName returnType, Map<String, String> namespaceBinding) {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext();
        namespaces.setBindings(namespaceBinding);
        xpath.setNamespaceContext(namespaces);
        
        try {
            InputSource source = new InputSource(new StringReader(doc));

            XPathExpression xpathExpr = xpath.compile(xpathStr);

            return xpathExpr.evaluate(source, returnType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object evaluate(String xpathStr, String doc, QName returnType) {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        try {
            InputSource source = new InputSource(new StringReader(doc));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(source);

            return xpath.evaluate(xpathStr, document, returnType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	public static Object evaluateSoapMessage(String xpathStr, SOAPMessage soapMessage, QName returnType) {
		try {
			DOMSource source = new DOMSource(soapMessage.getSOAPBody());
			StringWriter stringResult = new StringWriter();
			TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
			String message = stringResult.toString();
			return evaluate(xpathStr, message, returnType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Deprecated
    public static void populateDefaultSoapFields(ConfigurationManager cfg, Map<String, String> valuesMap) {
        valuesMap.put("sender_id", cfg.getConfigItem("service.header.sender_id"));
        valuesMap.put("source_system", cfg.getConfigItem("service.header.source_system"));
        valuesMap.put("source_application", cfg.getConfigItem("service.header.source_application"));
        valuesMap.put("application_session_id", cfg.getConfigItem("service.header.application_session_id"));
        valuesMap.put("source_location", cfg.getConfigItem("service.header.source_location"));
        valuesMap.put("traceId", UUID.randomUUID().toString());
        valuesMap.put("enterpriseUUID", UUID.randomUUID().toString());
    }
    
	public static Map<String, String> mergeMap(Map<String, String> firstMap, Map<String, String> secondMap) {
		return Stream.concat(firstMap.entrySet().stream(), secondMap.entrySet().stream()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
}
