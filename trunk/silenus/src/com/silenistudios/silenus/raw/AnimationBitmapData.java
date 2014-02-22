package com.silenistudios.silenus.raw;

import com.silenistudios.silenus.dom.BitmapInstance;


/**
 * This class contains all information about one particular bitmap on one frame of an animation.
 * @author Karel
 *
 */
public class AnimationBitmapData extends AnimationInstanceData {
	private static final long serialVersionUID = 4750362074054547165L;
	
	// the color manipulation
	ColorManipulation fColorManipulation = null;
	
	
	// constructor
	public AnimationBitmapData(BitmapInstance bitmap, TransformationMatrix matrix, ColorManipulation colorManipulation) {
		super(bitmap, matrix);
		fColorManipulation = colorManipulation;
	}
	
	// get color manipulation
	public ColorManipulation getColorManipulation() {
		return fColorManipulation;
	}
	
	
	// is there color manipulation?
	public boolean hasColorManipulation() {
		return fColorManipulation != null;
	}
	
	
	// get the JSON
	@Override
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append(getBasicJSON());
		if (fColorManipulation != null) ss.append(",\"colorManipulation\":").append(getColorManipulation().getJSON());
		ss.append("}");
		return ss.toString();
	}
}
