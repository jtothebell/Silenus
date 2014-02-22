package com.silenistudios.silenus.dom;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.XFLLibrary;
import com.silenistudios.silenus.raw.TransformationMatrix;
import com.silenistudios.silenus.xml.XMLUtility;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.silenistudios.silenus.xml.Node;


/**
 * A keyframe contains a set of symbols and/or bitmaps that it animates,
 * and transformation data for these symbols.
 * @author Karel
 *
 */
public class Keyframe {
	
	// list of instances in this keyframe, mapped by their library name for quick access
	Map<String, Instance> fInstancesByLibraryName = new HashMap<String, Instance>();
	
	// flat list of all instances
	Vector<Instance> fInstances = new Vector<Instance>();
	
	// index
	int fIndex;
	
	// the bezier curve describing the motion ease
	BezierCurve fEaseCurve = null;
	
	// is there a tween defined in this keyframe?
	boolean fIsTween = false;
	
	// IK tween duration
	int fDuration;
	
	// IK Tree used for IK motion
	IKTree fIKTree = null;
	
	// next key frame in the chain
	Keyframe fNextKeyframe = null;
	
	
	// load a keyframe
	public Keyframe(XMLUtility XMLUtility, XFLLibrary library, Node root) throws ParseException {
		
		// get frame index
		fIndex = XMLUtility.getIntAttribute(root, "index");
		
		// get duration
		fDuration = XMLUtility.getIntAttribute(root,  "duration", 1);
		
		// is there a tween here?
		fIsTween = XMLUtility.hasAttribute(root,  "tweenType");
		
		// get the tween type
		String tweenType = XMLUtility.getAttribute(root,  "tweenType", "");
		
		// if it's an IK tween, we do some preparation by loading the in-between matrices
		Vector<TransformationMatrix> inBetweenMatrices = null;
		if (tweenType.equals("IK pose")) {
			
			// get duration of the animation
			try {
				
				// get the IK Tree
				Node tree = XMLUtility.findNode(root, "IKTree");
				fIKTree = new IKTree(XMLUtility, tree);
				
				// get the list of in between matrices
				/*Node frameList = XMLUtility.findNode(root, "betweenFrameMatrixList");
				Vector<Node> matrices = XMLUtility.findNodes(frameList, "Matrix");
				inBetweenMatrices = new Vector<TransformationMatrix>();
				for (Node node : matrices) inBetweenMatrices.add(new TransformationMatrix(XMLUtility, node));*/
			}
			catch (ParseException e) {
				// invalid IK pose - we don't have betweenFrameMatrixList or duration, and
				// for now we're not going to simulate the bones real-time, so no animation :(
				fIsTween = false;
			}
		}
		
		// get all instances
		Node elements = XMLUtility.findNodeNonRecursive(root,  "elements");
		Vector<Node> instances = XMLUtility.getChildElements(elements);
		for (Node node : instances) {
			addInstance(XMLUtility, library, node);
		}
		
		// motion tween
		if (tweenType.equals("motion")) {
		
			// see if we got a custom ease
			boolean customEase = XMLUtility.getBooleanAttribute(root, "hasCustomEase", false);
			if (customEase) {
				
				// load the custom ease
				Node ease = XMLUtility.findNode(root, "CustomEase");
				
				// get all sub-points
				Vector<Node> points = XMLUtility.findNodes(ease, "Point");
				double[] x = new double[points.size()];
				double[] y = new double[points.size()];
				for (int i = 0; i < points.size(); ++i) {
					x[i] = XMLUtility.getDoubleAttribute(points.get(i), "x", 0);
					y[i] = XMLUtility.getDoubleAttribute(points.get(i), "y", 0);
				}
				fEaseCurve = new BezierCurve(x, y);
			}
			
			// no custom ease - use acceleration value to set up the bezier curve
			else {
				int acceleration = XMLUtility.getIntAttribute(root,  "acceleration", 0);
				
				// note that the acceleration flips sign here - this is because apparently flash
				// stores the acceleration value with the opposite sign displayed in the flash IDE
				fEaseCurve = new BezierCurve(-acceleration);
			}
		}
	}
	
	
	// add an instance
	private void addInstance(XMLUtility XMLUtility, XFLLibrary library, Node node) {
		
		// if adding this fails, it means we found an invalid reference
		Instance instance = null;
		try {
		
			// it's a bitmap instance
			if (node.getNodeName().equals("DOMBitmapInstance")) {
				instance = new BitmapInstance(XMLUtility, library, node, fIndex);
			}
			
			// it's a symbol instance
			else if (node.getNodeName().equals("DOMSymbolInstance")) {
				instance = new SymbolInstance(XMLUtility, library, node, fIndex);
			}
			
			// it's a shape
			else if (node.getNodeName().equals("DOMShape")) {
				instance = new ShapeInstance(XMLUtility, node, fIndex);
			}
			
			// it's a group - just add the underlying members
			else if (node.getNodeName().equals("DOMGroup")) {
				Node members = XMLUtility.findNodeNonRecursive(node,  "members");
				Vector<Node> instances = XMLUtility.getChildElements(members);
				for (Node member : instances) {
					addInstance(XMLUtility, library, member);
				}
			}
			
			// unknown instance - skip
			if (instance == null) {
				return;
			}
			
			// add to list
			fInstances.add(instance);
			
			// add to map
			if (instance.getLibraryItemName().length() > 0) fInstancesByLibraryName.put(instance.getLibraryItemName(), instance);
			
			// if there's in-between matrices, we add them to the instance
			if (fIKTree != null) {
				instance.setInBetweenMatrices(fIKTree.getTransformationMatrices(instance.getReferenceId()));
			}
		}
		catch (ParseException e) {
			// ignore this object
			e.printStackTrace();
			return;
		}
	}
	
	
	// is there a tween here?
	public boolean isTween() {
		return fIsTween;
	}
	
	
	// frame index
	public int getIndex() {
		return fIndex;
	}
	
	
	// get all symbol instances
	public Collection<Instance> getlInstances() {
		return fInstances;
	}
	
	
	
	// get an instance by library name
	public Instance getInstance(String libraryItemName) {
		return fInstancesByLibraryName.get(libraryItemName);
	}
	
	
	// compute ease
	public double computeEase(double t) {
		return (fEaseCurve != null ? fEaseCurve.interpolate(t) : t);
	}
	
	
	// get all images used for animation in this timeline
	Set<Bitmap> getUsedImages(Set<String> symbolInstancesAlreadyChecked) {
		Set<Bitmap> v = new HashSet<Bitmap>();
		for (Instance instance : fInstances) v.addAll(instance.getUsedImages(symbolInstancesAlreadyChecked));
		return v;
	}
	
	
	// is this an IK tween?
	public boolean isIKTween() {
		return fIKTree != null;
	}
	
	
	// get the duration
	public int getDuration() {
		return fDuration;
	}
	
	
	// set next keyframe
	public void setNextKeyframe(Keyframe next) {
		fNextKeyframe = next;
	}
	
	
	// is there a next keyframe?
	public boolean hasNextKeyframe() {
		return fNextKeyframe != null;
	}
	
	
	// get next keyframe
	public Keyframe getNextKeyframe() {
		return fNextKeyframe;
	}
}
