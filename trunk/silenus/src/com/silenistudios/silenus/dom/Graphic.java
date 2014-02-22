package com.silenistudios.silenus.dom;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.XFLLibrary;
import com.silenistudios.silenus.xml.XMLUtility;

import com.silenistudios.silenus.xml.Node;


/**
 * A graphic represents an instance of a bitmap in a scene, and contains transformation data,
 * sub-graphics etc for each sub-item that it contains. It does not contain any transformation
 * data about itself, as this is managed by the parent object it is attached to, or by the dom document.
 * @author Karel
 *
 */
public class Graphic {
	
	// name
	String fName;
	
	// different timelines for the graphic
	Timeline fTimeline;
	
	
	// constructor - does not load data yet
	public Graphic() {
		
		
	}
		
	
	// actually load the graphic
	public void loadGraphic(XMLUtility XMLUtility, XFLLibrary library, Node node) throws ParseException {
		
		// get the name
		fName = XMLUtility.getAttribute(node, "name");
		
		// get the timelines
		// TODO how do you get more than one?
		Node timelineNode = XMLUtility.findNode(node, "DOMTimeline");
		Timeline timeline = new Timeline(XMLUtility, library, timelineNode);
		fTimeline = timeline;
	}
	
	
	// get name
	public String getName() {
		return fName;
	}
	
	
	// get timeline
	public Timeline getTimeline() {
		return fTimeline;
	}
}
