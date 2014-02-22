package com.silenistudios.silenus.dom;

import java.util.Vector;

import com.silenistudios.silenus.raw.TransformationMatrix;
import com.silenistudios.silenus.xml.Node;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.xml.XMLUtility;

/**
 * An IK child node contains a reference ID which refers to a symbol or bitmap instance,
 * and a set of x/y/rotation values for each frame of the animation.
 * Important note: for some reason, the x/y values are stored in the XML with a *20 multiplier,
 * compared to all other x/y values. We divide by 20 here to avoid this strange issue.
 * @author Karel
 *
 */
public class IKChildNode {
	
	// reference ID
	String fReferenceId;
	
	// transformation matrix
	Vector<TransformationMatrix> fTransformationMatrices = new Vector<TransformationMatrix>();
	
	// x translate values for each frame in the animation
	double[] fTranslateX;
	
	// y translate values for each frame in the animation
	double[] fTranslateY;
	
	// rotation values for each frame in the animation
	double[] fRotation;
	
	
	// constructor
	public IKChildNode(XMLUtility XMLUtility, Node root) throws ParseException {
		
		// get reference ID
		fReferenceId = XMLUtility.getAttribute(root, "referenceID");
		
		// get array values
		String xArray = XMLUtility.getAttribute(root, "xArray");
		String yArray = XMLUtility.getAttribute(root, "yArray");
		String angleArray = XMLUtility.getAttribute(root, "angleArray");
		
		// split into different values and add them to the real array
		String[] xArraySplit = xArray.split(",");
		String[] yArraySplit = yArray.split(",");
		String[] angleArraySplit = angleArray.split(",");
		
		// must be same size
		if (xArraySplit.length != yArraySplit.length || yArraySplit.length != angleArraySplit.length) throw new ParseException("Failed to parse IK ChildNode: xArray, yArray and angleArray must contain the same number of values");
		
		// create the matrix array
		//fTransformationMatrices = new TransformationMatrix[xArraySplit.length];
		
		// go over all values, and create appropriate transformation matrices
		for (int i = 0; i < xArraySplit.length; ++i) {
			TransformationMatrix mx = new TransformationMatrix(Double.parseDouble(xArraySplit[i]) / 20.0, Double.parseDouble(yArraySplit[i]) / 20.0, 1.0, 1.0, Double.parseDouble(angleArraySplit[i]));
			//fTransformationMatrices[i] = mx;
			fTransformationMatrices.add(mx);
		}
	}
	
	
	// get reference ID
	public String getReferenceId() {
		return fReferenceId;
	}
	
	
	// get the transformation matrix for given frame (starting at the initial frame index defined by the parent DOMFrame)
	public Vector<TransformationMatrix> getTransformationMatrices() {
		return fTransformationMatrices;
	}
	
	
	// get translate X for frame (starting at the initial frame index defined by the parent DOMFrame)
	/*public double getTranslateX(int frame) {
		return fTranslateX[frame];
	}
	
	
	// get translate X for frame (starting at the initial frame index defined by the parent DOMFrame)
	public double getTranslateY(int frame) {
		return fTranslateY[frame];
	}
	
	
	// get rotation for frame (starting at the initial frame index defined by the parent DOMFrame)
	public double getRotation(int frame) {
		return fRotation[frame];
	}*/
}
