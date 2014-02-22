package com.silenistudios.silenus.xml.java;

import com.silenistudios.silenus.xml.Node;

/**
 * Java adapter for Node.
 * @author Karel
 *
 */
public class JavaNode implements Node {
	
	// the real node
	org.w3c.dom.Node fNode;
	
	
	// create a node
	public JavaNode(org.w3c.dom.Node node) {
		fNode = node;
	}
	
	
	// get node
	public org.w3c.dom.Node getNode() {
		return fNode;
	}
	

	@Override
	public String getNodeName() {
		return fNode.getNodeName();
	}
	
}
