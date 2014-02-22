package com.silenistudios.silenus.raw;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * This structure contains all transformation matrices for each 
 * @author Karel
 *
 */
public class AnimationFrameData implements Serializable {
	private static final long serialVersionUID = -7111305910456988265L;
	
	// list of animation instance data objects, refering all the objects drawn during this frame
	List<AnimationInstanceData> fInstances = new LinkedList<AnimationInstanceData>();
	
	
	
	// constructor
	public AnimationFrameData() {
	}
	
	
	// get instances
	public List<AnimationInstanceData> getInstances() {
		return fInstances;
	}
	
	
	// add an animation instance data object and return the index
	public int addAnimationInstanceData(AnimationInstanceData data) {
		fInstances.add(data);
		return fInstances.size()-1;
	}
	
	
	// get json
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append("\"instances\":[");
		for (int i = 0; i < fInstances.size(); ++i) {
			if (i != 0) ss.append(",");
			ss.append(fInstances.get(i).getJSON());
		}
		ss.append("]");
		ss.append("}");
		return ss.toString();
	}
}
