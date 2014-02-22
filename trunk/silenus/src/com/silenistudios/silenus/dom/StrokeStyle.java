package com.silenistudios.silenus.dom;

import java.util.Vector;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.dom.fillstyles.Color;
import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.xml.XMLUtility;

/**
 * Stroke style for a vector shape.
 * @author Karel
 *
 */
public class StrokeStyle {

	// index
	int fIndex;
	
	// color
	public Color fColor = new Color();
	
	// solid style
	String fSolidStyle;
	
	// stroke type
	String fStrokeType;
	
	// weight (width)
	public double fWeight;
	
	// caps setting
	public String fCaps;
	
	// joints setting
	public String fJoints;
	
	
	public StrokeStyle() {
		
	}
	
	
	// constructor
	public StrokeStyle(XMLUtility XMLUtility, Node root) throws ParseException {
		
		// get index
		fIndex = XMLUtility.getIntAttribute(root, "index");
		
		// get solid stroke style - hairline or not set
		fSolidStyle = XMLUtility.getAttribute(root, "solidStyle", "");
		
		// get solid color
		Node node = XMLUtility.findNode(root, "SolidColor");
		fColor = Color.parseColor(XMLUtility.getAttribute(node, "color", "#000000"));
		fColor.setAlpha(XMLUtility.getDoubleAttribute(node, "alpha", 1.0));
		
		// get the child of the style - its name represents the type of stroke
		Vector<Node> children = XMLUtility.getChildElements(root);
		if (children.size() != 1) throw new ParseException("StrokeStyle does not have exactly one child defining the stroke.");
		Node stroke = children.get(0);
		
		// get type
		// TODO support different stroke types
		fStrokeType = stroke.getNodeName();
		
		// get weight
		fWeight = XMLUtility.getDoubleAttribute(stroke, "weight", 1.0);
		
		// get caps
		fCaps = XMLUtility.getAttribute(stroke, "caps", "round");
		
		// get joints
		fJoints = XMLUtility.getAttribute(stroke, "joints", "round");
	}
	
	
	// get color
	public Color getColor() {
		return fColor;
	}
	
	
	// get the index
	public int getIndex() {
		return fIndex;
	}
	
	
	// is this a hairline style?
	public boolean isHairline() {
		return fSolidStyle.equals("hairline");
	}
	
	
	// get weight
	public double getWeight() {
		return fWeight;
	}
	
	
	// to json
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append("\"red\":").append(getColor().getRed()).append(",");
		ss.append("\"green\":").append(getColor().getGreen()).append(",");
		ss.append("\"blue\":").append(getColor().getBlue()).append(",");
		ss.append("\"alpha\":").append(getColor().getAlpha()).append(",");
		ss.append("\"solidStyle\":\"").append(fSolidStyle).append("\",");
		ss.append("\"strokeType\":\"").append(fStrokeType).append("\",");
		ss.append("\"weight\":").append(fWeight).append(",");
		ss.append("\"caps\":\"").append(fCaps).append("\",");
		ss.append("\"joints\":\"").append(fJoints).append("\"");
		ss.append("}");
		return ss.toString();
	}
}
