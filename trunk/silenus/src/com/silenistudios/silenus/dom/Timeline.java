package com.silenistudios.silenus.dom;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.XFLLibrary;
import com.silenistudios.silenus.xml.XMLUtility;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.silenistudios.silenus.xml.Node;


/**
 * A timeline contains all info regarding transformations of child objects attached
 * to its graphic. A timeline in the DOMDocument.xml file corresponds to a scene in flash.
 * @author Karel
 *
 */
public class Timeline {
	
	// layers
	Vector<Layer> fLayers = new Vector<Layer>();
	
	// name of the time line
	String fName;
	
	// the max frame index found in this time line - can be seen as animation length
	int fMaxFrameIndex = 0;
	
	
	// parse the time line
	public Timeline(XMLUtility XMLUtility, XFLLibrary library, Node root) throws ParseException {
		
		// name of the timeline (= scene name)
		fName = XMLUtility.getAttribute(root, "name");
		
		// get the different layers - 
		Vector<Node> layers = XMLUtility.findNodes(root,  "DOMLayer");
		//for (int i = layers.size()-1; i >= 0; --i) {
		for (int i = 0; i < layers.size(); ++i) {
			Node node = layers.get(i);
			
			// create layer
			Layer layer = new Layer(XMLUtility, library, fLayers, node);
			
			// update max frame index
			if (layer.getMaxFrameIndex() > fMaxFrameIndex) fMaxFrameIndex = layer.getMaxFrameIndex();
			
			// add to list
			fLayers.add(layer);
		}
		
		// we invert the order of the layers for more logical drawing
		Collections.reverse(fLayers);
	}
	
	
	// get name
	public String getName() {
		return fName;
	}
	
	
	// get animation length
	public int getAnimationLength() {
		return fMaxFrameIndex+1;
	}
	
	
	// get layers
	// note: layers are returned from lowest (drawn first) to highest (drawn last)
	// this is opposite to the way flash represents its layers, with the last drawn layer on top
	public Vector<Layer> getLayers() {
		return fLayers;
	}
	
	
	// get all images used for animation in this timeline
	public Set<Bitmap> getUsedImages() {
		return getUsedImages(new HashSet<String>());
	}
	
	
	// get all images used for animations in this timeline, while ignoring the symbols in the list
	// TODO this trick is used to avoid symbols from being traversed twice... there is probably a cleaner way to do this.
	public Set<Bitmap> getUsedImages(Set<String> symbolInstancesAlreadyChecked) {
		Set<Bitmap> v = new HashSet<Bitmap>();
		for (Layer layer : fLayers) v.addAll(layer.getUsedImages(symbolInstancesAlreadyChecked));
		return v;
	}
	
	
	// get max frame index for all underlying layers
	// note: no depth search is performed through the symbol tree, this also doesn't happen in flash
	public int getMaxFrameIndex() {
		return fMaxFrameIndex;
	}
	
}
