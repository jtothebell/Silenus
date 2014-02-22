package com.silenistudios.silenus.raw;

import com.silenistudios.silenus.dom.Instance;

/**
 * This class contains all information about one particular shape on one frame of an animation.
 * For now, no shape-specific information is stored.
 * @author Karel
 *
 */
public class AnimationShapeData extends AnimationInstanceData {

	public AnimationShapeData(Instance instance, TransformationMatrix matrix) {
		super(instance, matrix);
	}
	
	
	// get the JSON
	@Override
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append(getBasicJSON());
		ss.append("}");
		return ss.toString();
	}
}
