package com.silenistudios.silenus.xml;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.StreamFactory;

import java.util.Vector;


import com.silenistudios.silenus.xml.Node;

/**
 * Helper class which provides some convenience functions for handling the DOM.
 * @author Karel
 *
 */
public interface XMLUtility {
	
	// parse a document and return the root node (typically the first child of the Document obtained!)
	public Node parseXML(StreamFactory streamFactory, String root, String fileName) throws ParseException;
	
	// get the first element with a given tag - this looks recursively down the tree
	public Node findNode(Node root, String nodeName) throws ParseException;
	
	// find a node non-recursively
	public Node findNodeNonRecursive(Node root, String nodeName) throws ParseException;
	
	// is there such a node?
	public boolean hasNode(Node root, String nodeName) throws ParseException;
	
	
	// get all subnodes with the given tag - this looks recursively down the tree
	public Vector<Node> findNodes(Node root, String nodeName) throws ParseException;
	
	// get all child elements - ignoring other types of nodes
	public Vector<Node> getChildElements(Node root);
	
	
	// does the node have this attribute?
	public boolean hasAttribute(Node node, String attributeName);
	
	
	// get the attribute
	public String getAttribute(Node node, String attributeName) throws ParseException;
	
	// get boolean attribute
	public boolean getBooleanAttribute(Node node, String attributeName) throws ParseException;
	
	
	// get double attribute
	public double getDoubleAttribute(Node node, String attributeName) throws ParseException;
	
	
	// get int attribute
	public int getIntAttribute(Node node, String attributeName) throws ParseException;
	
	
	// get string attribute with default value
	public String getAttribute(Node node, String attributeName, String defaultValue) throws ParseException;
	
	
	// get boolean attribute with default value
	public boolean getBooleanAttribute(Node node, String attributeName, boolean defaultValue) throws ParseException;
	
	
	// get double attribute with default value
	public double getDoubleAttribute(Node node, String attributeName, double defaultValue) throws ParseException;
	
	
	// get int attribute with default value
	public int getIntAttribute(Node node, String attributeName, int defaultValue) throws ParseException;

}
