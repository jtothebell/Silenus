package com.silenistudios.silenus.raw;

import java.io.Serializable;
import java.util.Vector;

import com.silenistudios.silenus.dom.Instance;


/**
 * Base class that contains data about one particular instance (bitmap, shape, ...) for one particular frame.
 * @author Karel
 *
 */
public abstract class AnimationInstanceData implements Serializable {
	
	// the instance
	Instance fInstance;
	
	// the index of the instance in the library
	int fIndex;
	
	// the transformation matrix
	TransformationMatrix fTransformationMatrix;
	
	// mask?
	boolean fMask;
	
	// masked?
	boolean fMasked;
	
	// masks
	Vector<Integer> fMasks = null;
	
	
	// constructor
	public AnimationInstanceData(Instance instance, TransformationMatrix matrix) {
		
		// we copy the transformation matrix because it will be further manipulated later
		fInstance = instance;
		fTransformationMatrix = matrix.clone();
		fMask = instance.isMask();
		fMasked = instance.isMasked();
	}
	
	
	// set the index for the instance
	public void setIndex(int index) {
		fIndex = index;
	}
	
	
	// get index into the instance array
	public int getIndex() {
		return fIndex;
	}
	
	
	// is mask?
	public boolean isMask() {
		return fMask;
	}
	
	
	// is masked?
	public boolean isMasked() {
		return fMasked;
	}
	
	
	// set masks
	public void setMasks(Vector<Integer> masks) {
		fMasks = new Vector<Integer>();
		for (Integer i : masks) fMasks.add(i);
	}
	
	
	// get instance
	public Instance getInstance() {
		return fInstance;
	}
	
	
	// get matrix
	public TransformationMatrix getTransformationMatrix() {
		return fTransformationMatrix;
	}
	
	
	// get basic JSON to inject in the JSON of the subclasses - is not self-contained JSON!
	protected String getBasicJSON() {
		StringBuilder ss = new StringBuilder();
		TransformationMatrix m = getTransformationMatrix();
		ss.append("\"translate\":[").append(m.getTranslateX()).append(",").append(m.getTranslateY()).append("],");
		ss.append("\"scale\":[").append(m.getScaleX()).append(",").append(m.getScaleY()).append("],");
		ss.append("\"rotation\":").append(m.det() < 0 ? -m.getRotation() : m.getRotation()).append(",");
		ss.append("\"instanceIndex\":").append(fIndex);
		if (fMask) ss.append(",\"mask\":true");
		if (fMasked) {
			ss.append(",\"masked\":true");
			ss.append(",\"masks\":[");
			for (int i = 0; i < fMasks.size(); ++i) {
				if (i != 0) ss.append(",");
				ss.append(fMasks.get(i));
			}
			ss.append("]");
		}
		return ss.toString();
	}
	
	
	// get the real json
	public abstract String getJSON();
}
