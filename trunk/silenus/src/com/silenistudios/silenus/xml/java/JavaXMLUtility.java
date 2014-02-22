package com.silenistudios.silenus.xml.java;

import java.io.File;
import java.io.IOException;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.StreamFactory;

import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.xml.XMLUtility;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper class which provides some convenience functions for handling the DOM.
 * @author Karel
 *
 */
public class JavaXMLUtility implements XMLUtility {
	
	// parse a document
	@Override
	public Node parseXML(StreamFactory streamFactory, String root, String fileName) throws ParseException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			return new JavaNode(builder.parse(streamFactory.createInputStream(new File(root, fileName))).getFirstChild());
		} catch (ParserConfigurationException e) {
			throw new ParseException("Parser configuration exception occured when parsing file '" + fileName + "': " + e.getMessage(), e);
		} catch (SAXException e) {
			throw new ParseException("SAX exception occured when parsing file '" + fileName + "': " + e.getMessage(), e);
		} catch (IOException e) {
			throw new ParseException("IO exception occured when parsing file '" + fileName + "': " + e.getMessage(), e);
		}
		
	}
	
	// get the first element with a given tag
	@Override
	public Node findNode(Node nodeRoot, String nodeName) throws ParseException {
		org.w3c.dom.Node root = ((JavaNode)nodeRoot).getNode();
		if (root.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) throw new ParseException("XMLUtility: node " + nodeName + " is not an element");
		NodeList nodes = ((Element)root).getElementsByTagName(nodeName);
		if (nodes.getLength() != 1) throw new ParseException("XMLUtility: " + nodeName + " not found or too many found");
		return new JavaNode(nodes.item(0));
	}
	
	
	// get the first element with a given tag, without searching recursively down the tree
	@Override
	public Node findNodeNonRecursive(Node nodeRoot, String nodeName) throws ParseException {
		Vector<Node> children = getChildElements(nodeRoot);
		for (Node child : children) {
			if (child.getNodeName().equals(nodeName)) return child;
		}
		throw new ParseException("XMLUtility: " + nodeName + " not found");
	}
	
	
	// is there a node with this name?
	@Override
	public boolean hasNode(Node nodeRoot, String nodeName) throws ParseException {
		org.w3c.dom.Node root = ((JavaNode)nodeRoot).getNode();
		if (root.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) throw new ParseException("XMLUtility: node " + nodeName + " is not an element");
		NodeList nodes = ((Element)root).getElementsByTagName(nodeName);
		return nodes.getLength() > 0;
	}
	
	
	// get all subnodes with the given tag
	@Override
	public Vector<Node> findNodes(Node nodeRoot, String nodeName) throws ParseException {
		org.w3c.dom.Node root = ((JavaNode)nodeRoot).getNode();
		Vector<Node> v = new Vector<Node>();
		if (root.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) throw new ParseException("XMLUtility: node '" + nodeName + "' is not an element");
		NodeList nodes = ((Element)root).getElementsByTagName(nodeName);
		for (int i = 0; i < nodes.getLength(); ++i) v.add(new JavaNode(nodes.item(i)));
		return v;
	}
	
	
	// get all subnodes with the given tag
	@Override
	public Vector<Node> getChildElements(Node nodeRoot) {
		org.w3c.dom.Node root = ((JavaNode)nodeRoot).getNode();
		NodeList children = root.getChildNodes();
		Vector<Node> v = new Vector<Node>();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				v.add(new JavaNode(children.item(i)));
		}
		return v;
	}
	
	
	// does the node have this attribute?
	@Override
	public boolean hasAttribute(Node nodeRoot, String attributeName) {
		org.w3c.dom.Node node = ((JavaNode)nodeRoot).getNode();
		NamedNodeMap attributes = node.getAttributes();
		return attributes.getNamedItem(attributeName) != null;
	}
	
	
	// get the attribute
	@Override
	public String getAttribute(Node nodeRoot, String attributeName) throws ParseException {
		org.w3c.dom.Node node = ((JavaNode)nodeRoot).getNode();
		NamedNodeMap attributes = node.getAttributes();
		if (attributes == null) throw new ParseException("XMLUtility: no attributes found in node");
		org.w3c.dom.Node attribute = attributes.getNamedItem(attributeName);
		if (attribute == null) throw new ParseException("XMLUtility: attribute '" + attributeName + "' does not exist in node '" + node.getNodeName() + "'");
		return attribute.getNodeValue();
	}
	
	
	// true/false strings
	private final static String[] TRUE_STRINGS = new String[]{"yes", "true", "1", "ok"};
	
	// get boolean attribute
	@Override
	public boolean getBooleanAttribute(Node node, String attributeName) throws ParseException {
		String s = getAttribute(node, attributeName);
		for (String match : TRUE_STRINGS) {
			if (s.equalsIgnoreCase(match)) return true;
		}
		return false;
	}
	
	
	// get double attribute
	@Override
	public double getDoubleAttribute(Node node, String attributeName) throws ParseException {
		String s = getAttribute(node, attributeName);
		return Double.parseDouble(s);
	}
	
	
	// get int attribute
	@Override
	public int getIntAttribute(Node node, String attributeName) throws ParseException {
		String s = getAttribute(node, attributeName);
		return Integer.parseInt(s);
	}
	
	
	// get string attribute with default value
	@Override
	public String getAttribute(Node node, String attributeName, String defaultValue) throws ParseException {
		if (!hasAttribute(node, attributeName)) return defaultValue;
		return getAttribute(node, attributeName);
	}
	
	
	// get boolean attribute with default value
	@Override
	public boolean getBooleanAttribute(Node node, String attributeName, boolean defaultValue) throws ParseException {
		if (!hasAttribute(node, attributeName)) return defaultValue;
		return getBooleanAttribute(node, attributeName);
	}
	
	
	// get double attribute with default value
	@Override
	public double getDoubleAttribute(Node node, String attributeName, double defaultValue) throws ParseException {
		if (!hasAttribute(node, attributeName)) return defaultValue;
		return getDoubleAttribute(node, attributeName);
	}
	
	
	// get int attribute with default value
	@Override
	public int getIntAttribute(Node node, String attributeName, int defaultValue) throws ParseException {
		if (!hasAttribute(node, attributeName)) return defaultValue;
		return getIntAttribute(node, attributeName);
	}

}
