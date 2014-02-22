package com.silenistudios.silenus.dom;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.silenistudios.silenus.raw.TransformationMatrix;
import com.silenistudios.silenus.xml.Node;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.xml.XMLUtility;

/**
 * An IK Tree contains bone information for a set of different symbols contained
 * in a layer. For now, we only get the x/y/rotation data, as we don't care about
 * simulating bone limitations.
 * @author Karel
 *
 */
public class IKTree {
	
	// map of reference ID to child nodes, so that we can get the data
	Map<String, IKChildNode> fChildren = new HashMap<String, IKChildNode>();
	
	
	// constructor
	public IKTree(XMLUtility XMLUtility, Node root) throws ParseException {
		
		// get all child nodes and set them up
		Vector<Node> childNodes = XMLUtility.findNodes(root, "ChildNode");
		for (Node node : childNodes) {
			IKChildNode childNode = new IKChildNode(XMLUtility, node);
			fChildren.put(childNode.getReferenceId(), childNode);
		}
	}
	
	
	// get the transformation matrix for a given reference ID
	public Vector<TransformationMatrix> getTransformationMatrices(String referenceId) throws ParseException {
		if (!fChildren.containsKey(referenceId)) throw new ParseException("Failed to find IK ChildNode with reference ID '" + referenceId + "'");
		return fChildren.get(referenceId).getTransformationMatrices();
	}

}
