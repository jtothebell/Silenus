package com.silenistudios.silenus.dom.fillstyles;

import java.util.Vector;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.xml.XMLUtility;

/**
 * A fill style represents how a vector face is filled. It contains a particular type of paint that
 * owns a render method, that can be called to invoke the right functions on the ShapeRenderInterface.
 * @author Karel
 *
 */
public class FillStyle {
	
	// index
	int fIndex;
	
	// the paint
	Paint fPaint;
	
	
	// constructor
	public FillStyle(XMLUtility XMLUtility, Node root) throws ParseException {
		
		// get index
		fIndex = XMLUtility.getIntAttribute(root, "index");
		
		// get the color definition
		Vector<Node> nodes = XMLUtility.getChildElements(root);
		Node node = nodes.get(0);
		
		// this is a simple solid color - perfect!
		if (node.getNodeName().equals("SolidColor")) {
			Color color = Color.parseColor(XMLUtility.getAttribute(node, "color", "#000000"));
			color.setAlpha(XMLUtility.getDoubleAttribute(node, "alpha", 1.0));
			fPaint = color;
		}

		// this is a linear gradient
		else if (node.getNodeName().equals("LinearGradient")) {
			fPaint = new LinearGradient(XMLUtility, node);
		}

        // this is a linear gradient
        else if (node.getNodeName().equals("RadialGradient")) {
            fPaint = new RadialGradient(XMLUtility, node);
        }
		
		// unsupported fill style
		else {
			fPaint = new Color(255, 0, 0);
		}
	}
	
	
	// get color
	public Paint getPaint() {
		return fPaint;
	}
	
	
	// get the index
	public int getIndex() {
		return fIndex;
	}
	
	
	// to json
	public String getJSON() {
		return fPaint.getJSON();
	}
}
