package com.silenistudios.silenus.raw;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.silenistudios.silenus.dom.Instance;

/**
 * This data structure contains all points and locations for one animation (scene).
 * Can be easily serialized and recovered later for re-use.
 * @author Karel
 *
 */
public class AnimationData implements Serializable {
	private static final long serialVersionUID = 2283183510219011650L;
	
	// list of all instances used in this scene
	Vector<Instance> fInstances = new Vector<Instance>();
	
	// convenient map for finding back the instances by library item name
	Map<String, Integer> fLibraryItemNameToInstanceIndex = new HashMap<String, Integer>();
	
	// animation frames
	AnimationFrameData[] fFrames;
	
	// current frame
	int fCurrentFrame = 0;
	
	// length of the animation
	int fAnimationLength;
	
	// width of the animation
	int fWidth;
	
	// height
	int fHeight;
	
	// frame rate
	int fFrameRate;
	
	// current list of masks
	Vector<Integer> fMasks = new Vector<Integer>();
	
	// constructor
	public AnimationData(int animationLength, int width, int height, int frameRate) {
		fAnimationLength = animationLength;
		fWidth = width;
		fHeight = height;
		fFrameRate = frameRate;
		fFrames = new AnimationFrameData[fAnimationLength];
		for (int i = 0; i < fAnimationLength; ++i) fFrames[i] = new AnimationFrameData();
	}
	
	
	// add an instance to the current frame
	public void addInstance(AnimationInstanceData data) {
		
		// add to the correct frame
		int idx = fFrames[fCurrentFrame].addAnimationInstanceData(data);
		
		// see if we encounter this instance for the first time - if yes, add to the library
		Instance instance = data.getInstance();
		String libraryItemName = instance.getLibraryItemName();
		
		// is this a mask?
		if (data.isMask()) {
			fMasks.add(idx);
		}
		
		
		// is this instance masked?
		if (data.isMasked()) {
			data.setMasks(fMasks);
		}
		
		// not yet in the library
		if (!fLibraryItemNameToInstanceIndex.containsKey(libraryItemName)) {
			fInstances.add(instance);
			fLibraryItemNameToInstanceIndex.put(libraryItemName, fInstances.size()-1);
		}
		
		// set the correct index
		data.setIndex(fLibraryItemNameToInstanceIndex.get(libraryItemName));
	}
	
	
	// advance a frame
	public void advanceFrame() {
		++fCurrentFrame;
	}
	
	
	// set frame
	public void setFrame(int frame) {
		fCurrentFrame = frame;
	}
	
	
	// add a frame
	public void setFrame(int frameIndex, AnimationFrameData data) {
		assert(0 <= frameIndex && frameIndex < fFrames.length);
		fFrames[frameIndex] = data;
	}
	
	
	// get frame data
	public AnimationFrameData getFrameData(int frameIndex) {
		assert(0 <= frameIndex && frameIndex < fFrames.length);
		return fFrames[frameIndex];
	}
	
	
	// get animation length
	public int getAnimationLength() {
		return fFrames.length;
	}
	
	
	// get FPS
	public int getFrameRate() {
		return fFrameRate;
	}
	
	
	// get width
	public int getWidth() {
		return fWidth;
	}
	
	
	// height
	public int getHeight() {
		return fHeight;
	}
	
	
	// reset mask
	public void resetMask() {
		fMasks.clear();
	}
	
	
	// export to json
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append("\"frameRate\":").append(fFrameRate).append(",");
		ss.append("\"width\":").append(fWidth).append(",");
		ss.append("\"height\":").append(fHeight).append(",");
		ss.append("\"instances\":[");
		for (int i = 0; i < fInstances.size(); ++i) {
			if (i != 0) ss.append(",");
			Instance instance = fInstances.get(i);
			ss.append(instance.getJSON());
		}
		ss.append("],");
		ss.append("\"frames\":[");
		for (int i = 0; i < fFrames.length; ++i) {
			if (i != 0) ss.append(",");
			ss.append(fFrames[i].getJSON());
		}
		ss.append("]}");
		return ss.toString();
	}
}
