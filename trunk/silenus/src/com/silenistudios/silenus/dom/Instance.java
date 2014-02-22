package com.silenistudios.silenus.dom;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.SceneRenderer;
import com.silenistudios.silenus.raw.ColorManipulation;
import com.silenistudios.silenus.raw.TransformationMatrix;
import com.silenistudios.silenus.xml.XMLUtility;

import com.silenistudios.silenus.xml.Node;


/**
 * Base instance class. Contains all transformation data.
 * @author Karel
 *
 */
public abstract class Instance {
	
	// transformation matrix
	TransformationMatrix fMatrix = new TransformationMatrix();
	
	// transformation point
	double fTransformationX = 0, fTransformationY = 0;
	
	// library item name - can refer to an image or to an xml, depending on the type
	String fLibraryItemName;
	
	// there is color manipulation
	// TODO does this really belong here in the base class, or is this one only applicable to BitmapInstances?
	ColorManipulation fColorManipulation = null;
	
	// in-between matrices, these are set when IK pose tweening is used
	Vector<TransformationMatrix> fInBetweenMatrices;
	
	// frame index of this instance
	int fFrameIndex;
	
	// reference ID - only used for IK pose
	String fReferenceId;
	
	// is this instance masked by previous masks?
	// this is only set temporarily when we are in mask mode, to allow the renderers to take this into account
	public boolean fMasked = false;
	
	
	// is this instance a mask for later drawings?
	// this is only set temporarily when we are in mask mode, to allow the renderers to take this into account
	public boolean fMask = false;
	
	
	// load an instance
	public Instance(XMLUtility XMLUtility, Node root, int frameIndex) throws ParseException {
		
		// library item name
		fLibraryItemName = XMLUtility.getAttribute(root, "libraryItemName", "");
		
		// frame index (same for all instances in the same parent frame)
		fFrameIndex = frameIndex;
		
		// get reference ID (if it exists)
		// this is only used in IK pose motion tweens
		fReferenceId = XMLUtility.getAttribute(root,  "referenceID", "");
		
		// alpha
		try {
			Node color = XMLUtility.findNode(root, "Color");
			fColorManipulation = new ColorManipulation();
			fColorManipulation.setAlphaMultiplier(XMLUtility.getDoubleAttribute(color, "alphaMultiplier", 1.0));
			fColorManipulation.setRedMultiplier(XMLUtility.getDoubleAttribute(color, "redMultiplier", 1.0));
			fColorManipulation.setGreenMultiplier(XMLUtility.getDoubleAttribute(color, "greenMultiplier", 1.0));
			fColorManipulation.setBlueMultiplier(XMLUtility.getDoubleAttribute(color, "blueMultiplier", 1.0));
			fColorManipulation.setAlphaOffset(XMLUtility.getDoubleAttribute(color, "alphaOffset", 0.0));
			fColorManipulation.setRedOffset(XMLUtility.getDoubleAttribute(color, "redOffset", 0.0));
			fColorManipulation.setGreenOffset(XMLUtility.getDoubleAttribute(color, "greenOffset", 0.0));
			fColorManipulation.setBlueOffset(XMLUtility.getDoubleAttribute(color, "blueOffset", 0.0));
		}
		catch (ParseException e) {
			// <Color> does not exist
		}
		
		// get matrix subnode
		try {
			Node matrixParent = XMLUtility.findNodeNonRecursive(root, "matrix");
			Node matrix = XMLUtility.findNodeNonRecursive(matrixParent, "Matrix");
			fMatrix = new TransformationMatrix(XMLUtility, matrix);
		}
		catch (ParseException e) {
			// do nothing, this just means <matrix> doesn't exist and no values should be set
		}
		
		// get the transformation point
		try {
			Node transformationPoint = XMLUtility.findNode(root, "transformationPoint");
			Node point = XMLUtility.findNode(transformationPoint, "Point");
			
			// load transformation point
			fTransformationX = XMLUtility.getDoubleAttribute(point,  "x");
			fTransformationY = XMLUtility.getDoubleAttribute(point,  "y");
		}
		catch (ParseException e) {
			// do nothing, this just means <transformationPoint> doesn't exist and no values should be set
		}
	}
	
	
	// get reference id
	public String getReferenceId() {
		return fReferenceId;
	}
	
	
	// get translate x
	public double getTranslateX() {
		return fMatrix.getTranslateX();
	}


	// get translate y
	public double getTranslateY() {
		return fMatrix.getTranslateY();
	}
	
	
	// get x scale value
	public double getScaleX() {
		return fMatrix.getScaleX();
	}
	
	
	// get y scale
	public double getScaleY() {
		return fMatrix.getScaleY();
	}
	
	
	// get rotation
	public double getRotation() {
		return fMatrix.getRotation();
	}
	
	
	// get transformation matrix
	public TransformationMatrix getTransformationMatrix() {
		return fMatrix;
	}
	
	
	// get transformation x in scaled/rotated/translated form
	public double getRelativeTransformationPointX() {
		return fTransformationX;
	}
	
	
	// get transformation y in scaled/rotated/translated form
	public double getRelativeTransformationPointY() {
		return fTransformationY;
	}
	
	
	// get transformation x in scaled/rotated/translated form
	public double getTransformationPointX() {
		return fMatrix.computeX(fTransformationX, fTransformationY);
	}
	
	
	// get transformation y in scaled/rotated/translated form
	public double getTransformationPointY() {
		return fMatrix.computeY(fTransformationX, fTransformationY);
	}
	
	
	// get library item name - can refer to an image or to a symbol xml, depending on the type
	public String getLibraryItemName() {
		return fLibraryItemName;
	}
	
	
	// is there color manipulation?
	public boolean hasColorManipulation() {
		return fColorManipulation != null;
	}
	
	
	// get the color manipulation object
	public ColorManipulation getColorManipulation() {
		return fColorManipulation;
	}
	
	
	// set in-between matrix
	public void setInBetweenMatrices(List<TransformationMatrix> matrices) {
		fInBetweenMatrices = new Vector<TransformationMatrix>();
		for (TransformationMatrix matrix : matrices) fInBetweenMatrices.add(matrix);
	}
	
	
	// does this keyframe have in-between matrices?
	public boolean hasInBetweenMatrices() {
		return fInBetweenMatrices != null;
	}
	
	
	// get the in-between matrix for a given frame
	public TransformationMatrix getInBetweenMatrix(int frame) {
		return fInBetweenMatrices.get(fFrameIndex + frame);
	}
	
	
	// set the library item name - can only be done by subclasses who don't have a library item name by themselves, and therefore
	// generate one of their own
	protected void setLibraryItemName(String name) {
		fLibraryItemName = name;
	}
	
	
	// get max frame index for IK transformation
	public int getMaxIKFrameIndex() {
		return fFrameIndex + fInBetweenMatrices.size() - 1;
	}
	
	
	// set mask
	public void setMask(boolean mask) {
		fMask = mask;
	}
	
	
	// is this instance a mask for later rendering?
	public boolean isMask() {
		return fMask;
	}
	
	
	// set masked
	public void setMasked(boolean masked) {
		fMasked = masked;
	}
	
	
	// is this instance a mask for later rendering?
	public boolean isMasked() {
		return fMasked;
	}
	
	
	// render this instance
	// this trick is used to recover the subclass without having to resort to enums
	// this allows us to deal with all instances in the same way
	public abstract void render(SceneRenderer renderer, int frame);
	
	
	// get used instances
	public abstract Set<Bitmap> getUsedImages(Set<String> symbolInstancesAlreadyChecked);
	
	
	// get JSON for the subclass
	public abstract String getJSON();
	
	// get first frame - used for defining at which point the symbol instance should start animating
	public abstract int getFirstFrame();
}
