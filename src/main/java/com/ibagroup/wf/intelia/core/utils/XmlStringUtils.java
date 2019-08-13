package com.ibagroup.wf.intelia.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlStringUtils {

	public static synchronized DocumentBuilderFactory createNewDocumentBuilderFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		return factory;
	}

	public static synchronized XPathFactory createNewXPathFactory() {
		return XPathFactory.newInstance();
	}

	public static synchronized TransformerFactory createNewTransformerFactory() {
		return TransformerFactory.newInstance();
	}

	public static DocumentBuilder createNewDocumentBuilder(DocumentBuilderFactory factory) {
		try {
			return factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Cant create new xml document builder", e);
		}
	}

	public static Transformer createNewTransformer(TransformerFactory factory) {
		try {
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			return transformer;
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException("Cant create new transformer", e);
		}
	}

	public static Transformer createNewTransformerOmitXmlDeclaration(TransformerFactory factory) {
		Transformer transformer = createNewTransformer(factory);
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		return transformer;
	}

	public static XPath createNewXPath(XPathFactory factory) {
		return factory.newXPath();
	}

	public static Document parseHtmlToXmlDocument(String html, DocumentBuilder builder) {
		HtmlCleaner cleaner = new HtmlCleaner();
		cleaner.getProperties().setOmitComments(true);
		String cleanedHtml = new SimpleXmlSerializer(new CleanerProperties()).getXmlAsString(cleaner.clean(html));
		return parseXmlToDocument(cleanedHtml, builder);
	}

	public static Document parseXmlToDocument(String xml, DocumentBuilder builder) {
		try {
			builder.reset();
			ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
			return builder.parse(input);
		} catch (SAXException | IOException e) {
			throw new RuntimeException("Cant parse xml " + xml, e);
		}
	}

	public static NodeList xpathXmlToNodeList(Document doc, String xpathString, XPath xPath) {
		try {
			xPath.reset();
			XPathExpression expr = xPath.compile(xpathString);
			return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (Exception e) {
			throw new RuntimeException("Cant xpath " + xpathString, e);
		}
	}

	public static String transformXmlToString(Document document, Transformer transformer) {
		try {
			StringWriter sw = new StringWriter();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(sw);

			transformer.reset();
			transformer.transform(source, result);
			return sw.toString();
		} catch (TransformerException e) {
			throw new RuntimeException("Tranformation XML Document to String FAILED", e);
		}
	}
}
