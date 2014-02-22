package com.silenistudios.silenus.dom;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.XFLLibrary;
import com.silenistudios.silenus.xml.XMLUtility;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.silenistudios.silenus.xml.Node;


/**
 * A layer contains a graphic (or set of graphics) and animation data
 * that is applied to these graphics.
 * @author Karel
 *
 */
public class Layer {
	
	// set of keyframes
	Vector<Keyframe> fKeyframes = new Vector<Keyframe>();
	
	// child layers
	Vector<Layer> fChildLayers = new Vector<Layer>();
	
	// name of the layer
	String fName;
	
	// visible?
	boolean fVisible;
	
	// highest frame index
	int fMaxFrameIndex = 0;
	
	// animation type - for now only used for IK animations
	String fAnimationType;
	
	// layer type
	String fLayerType;
	
	
	// create a layer
	public Layer(XMLUtility XMLUtility, XFLLibrary library, Vector<Layer> prevLayers, Node root) throws ParseException {
		
		// set type
		fAnimationType = XMLUtility.getAttribute(root, "animationType", "");
		
		// name of the layer
		fName = XMLUtility.getAttribute(root, "name");
		
		// type of the layer - mask means this layer must mask subsequent layers
		fLayerType = XMLUtility.getAttribute(root, "layerType", "normal");
		
		// parent layer index - used for clipping
		// this index refers to the index of the layer in the timeline, as written in the XML
		int parentLayerIndex = XMLUtility.getIntAttribute(root, "parentLayerIndex", -1);
		
		// the parent index is set - define our parent
		if (parentLayerIndex != -1) {
			if (parentLayerIndex < 0 || parentLayerIndex >= prevLayers.size()) throw new ParseException("Failed to get parent layer with parentLayerIndex " + parentLayerIndex + ": only " + prevLayers.size() + " layers exist");
			Layer parentLayer = prevLayers.get(parentLayerIndex);
			if (parentLayer.isMaskLayer()) fLayerType = "masked";
			parentLayer.addChild(this);
		}
		
		// visible or not?
		fVisible = XMLUtility.getBooleanAttribute(root,  "visible", true);
		
		// get the different keyframes
		Vector<Node> frames = XMLUtility.findNodes(root,  "DOMFrame");
		for (Node node : frames) {
			Keyframe frame = new Keyframe(XMLUtility, library, node);
			fKeyframes.add(frame);
			
			// note that we subtract 1 frame - this is because the frame itself is also counted in the duration!
			fMaxFrameIndex = frame.getIndex() + frame.getDuration() - 1;
			
			// set next key frame for the previous frame
			if (fKeyframes.size() > 1) fKeyframes.get(fKeyframes.size()-2).setNextKeyframe(frame);
			
			// if we're a mask, we signal this to all our instances
			
		}
	}
	
	
	// name
	public String getName() {
		return fName;
	}
	
	
	// visible?
	public boolean isVisible() {
		return fVisible;
	}
	
	
	// get the keyframes
	public Vector<Keyframe> getKeyframes() {
		return fKeyframes;
	}
	
	
	// get all images used for animation in this timeline
	public Set<Bitmap> getUsedImages(Set<String> symbolInstancesAlreadyChecked) {
		Set<Bitmap> v = new HashSet<Bitmap>();
		for (Keyframe frame : fKeyframes) v.addAll(frame.getUsedImages(symbolInstancesAlreadyChecked));
		return v;
	}
	
	
	// get animation length
	public int getMaxFrameIndex() {
		return fMaxFrameIndex;
	}
	
	
	// get type
	public String getAnimationType() {
		return fAnimationType;
	}
	
	
	// get the keyframe for a corrected frame number - this number is first pulled through SymbolInstance.getCorrectFrame
	// to account for the different loop types
	public Keyframe getKeyframe(int correctedFrame) {
		if (correctedFrame < 0) return null; // happens when polling for previous versions of an instance
		
		// look among all keyframes for a match
		for (Keyframe keyframe : fKeyframes) {
			if (keyframe.getIndex() <= correctedFrame && correctedFrame < keyframe.getIndex() + keyframe.getDuration()) return keyframe;
		}
		
		// no match found
		return null;
	}
	
	
	// get first keyframe that contains a given symbol
	public Keyframe getFirstKeyframe(String libraryItemName) {
		for (Keyframe keyframe : fKeyframes) {
			if (keyframe.getInstance(libraryItemName) != null) return keyframe;
		}
		return null; // this should NEVER occur, as this function is only called whenever an instance is found in a subsequent keyframe
	}
	
	
	// add a child layer - this one is masked
	public void addChild(Layer layer) {
		fChildLayers.add(layer);
	}
	
	
	// is this a mask layer
	public boolean isMaskLayer() {
		return fLayerType.equals("mask");
	}
	
	
	// is this a masked layer?
	// masked layers should not be drawn directly - instead, they should be drawn through the child mechanic
	public boolean isMaskedLayer() {
		return fLayerType.equals("masked");
	}
	
	
	// get child layers - the ones that should be drawn with the mask
	public Vector<Layer> getChildLayers() {
		return fChildLayers;
	}
}
