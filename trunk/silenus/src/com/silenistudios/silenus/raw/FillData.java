package com.silenistudios.silenus.raw;

import com.silenistudios.silenus.dom.Path;
import com.silenistudios.silenus.dom.fillstyles.FillStyle;

/**
 * Contains data for drawing a fill shape.
 * @author Karel
 *
 */
public class FillData {
	
	// the style
	FillStyle fStyle;
	
	// the path
	Path fPath;
	
	// transformation matrix
	TransformationMatrix fMatrix;
	
	
	// constructor
	public FillData(FillStyle style, Path path, TransformationMatrix matrix) {
		fStyle = style;
		fPath = path;
		fMatrix = matrix;
	}
	
	
	// get style
	public FillStyle getStyle() {
		return fStyle;
	}
	
	
	// get path
	public Path getPath() {
		return fPath;
	}
	
	
	// the matrix
	public TransformationMatrix getTransformationMatrix() {
		return fMatrix;
	}
	
	
	// get json
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append("\"path\":").append(fPath.getJSON()).append(",");
		ss.append("\"style\":").append(fStyle.getJSON());
		ss.append("}");
		return ss.toString();
	}
}
