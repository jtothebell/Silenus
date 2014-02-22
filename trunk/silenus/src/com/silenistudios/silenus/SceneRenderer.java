package com.silenistudios.silenus;

import java.util.Collection;
import java.util.Stack;
import java.util.Vector;

import com.silenistudios.silenus.dom.BitmapInstance;
import com.silenistudios.silenus.dom.Instance;
import com.silenistudios.silenus.dom.Keyframe;
import com.silenistudios.silenus.dom.Layer;
import com.silenistudios.silenus.dom.ShapeInstance;
import com.silenistudios.silenus.dom.Timeline;
import com.silenistudios.silenus.raw.ColorManipulation;
import com.silenistudios.silenus.raw.TransformationMatrix;

/**
 * A scene renderer will take a loaded scene from an XFLParser, and will
 * render it to an interface called through RenderInterface.
 * @author Karel
 *
 */
public class SceneRenderer {
	
	// the scene
	Timeline fScene;
	
	// the renderer
	RenderInterface fRenderer;
	
	// the highest frame found in this scene - can be seen as the animation length
	int fMaxFrameIndex;
	
	// stack of color manipulations - because they propagate through the symbol tree
	Stack<ColorManipulation> fColorManipulationStack = new Stack<ColorManipulation>();
	
	// are we currently drawing masks?
	boolean fMask = false;
	
	// are we currently drawing masked instances?
	boolean fMasked = false;
	
	
	// constructor
	public SceneRenderer(Timeline scene, RenderInterface renderer) {
		fRenderer = renderer;
		fScene = scene;
	}
	
	// render the scene at a given frame
	public void render(int frame) {
		
		// draw the different layers in order
		Vector<Layer> layers = fScene.getLayers();
		for (Layer layer : layers) {
			drawLayer(layer, frame, frame, false);
		}
	}
	
	
	// draw a layer
	public void drawLayer(Layer layer, int frame, int correctedFrame, boolean drawMasked) {
		
		// this is a masked layer - skip drawing it directly
		if (!drawMasked && layer.isMaskedLayer()) return;
		
		// this is a mask layer - we draw everything below this layer as a mask
		if (layer.isMaskLayer()) fMask = true;
		
		// get the appropriate keyframe for this "real frame", based on the loop type
		Keyframe f1 = layer.getKeyframe(correctedFrame);
		
		// no frame available - don't draw anything!
		if (f1 == null) {
			return;
		}
		
		// there is a next frame - interpolate
		if (f1.hasNextKeyframe() && f1.isTween()) {
			interpolateFrames(layer, f1, f1.getNextKeyframe(), frame, correctedFrame);
		}
		
		// there is no next keyframe - just draw the first frame
		else {
			interpolateFrames(layer, f1, f1, frame, correctedFrame);
		}
		
		// we drew the mask - now draw the actual masked layers
		if (layer.isMaskLayer()) {
			
			// these layers shouldn't be masked
			fMask = false;
			fMasked = true;
		}
		
		// draw children if we have any
		Vector<Layer> children = layer.getChildLayers();
		for (Layer child : children) {
			drawLayer(child, frame, correctedFrame, true);
		}
		
		// let the renderer know we're done masking
		if (layer.isMaskLayer()) {
			fMasked = false;
			fRenderer.resetMask();
		}
	}
	
	
	// interpolate between two frames f1 and f2 (f0 is the frame already rendered - the previous frame)
	private void interpolateFrames(Layer layer, Keyframe f1, Keyframe f2, int frame, int correctedFrame) {
		
		// compute the distance between the two, unless it's the same frame (aka, there is no tween)
		double d = 0;
		if (f1.getIndex() != f2.getIndex()) d = (double)(correctedFrame - f1.getIndex()) / (double)(f2.getIndex() - f1.getIndex());
		
		// update d for ease
		d = f1.computeEase(d);
		
		// walk over all instances
		Collection<Instance> instances = f1.getlInstances();
		for (Instance i1 : instances) {
			
			// get the instance in the second frame
			Instance i2 = f2.getInstance(i1.getLibraryItemName());
			
			// no instance found in second frame, or no tween - just tween between ourselves
			if (i2 == null) i2 = i1;
			
			// we find the first occurence of this instance in the layer - so we can correct the frame
			Keyframe f0 = layer.getFirstKeyframe(i1.getLibraryItemName());
			
			// we get the first frame defined in this keyframe - this defines the offset at which we render the symbol
			int firstFrame = f0.getInstance(i1.getLibraryItemName()).getFirstFrame();
			
			// correct the frame to match the first frame
			int instanceCorrectedFrame = frame - f0.getIndex() + firstFrame;
			
			// save transformation matrix
			fRenderer.save();
			
			// move/scale/rotate to the correct position
			transformInstance(i1, i2, d, correctedFrame);
			
			// render the image
			i1.render(this, instanceCorrectedFrame);
			
			// done
			resetInstance(i1, i2);
			fRenderer.restore();
		}
		
		/*Collection<BitmapInstance> bitmapInstances = f1.getBitmapInstances();
		for (BitmapInstance i1 : bitmapInstances) {
			
			// get the instance in the second frame
			Instance i2 = f2.getInstance(i1.getLibraryItemName());
			
			// save transformation matrix
			fRenderer.save();
			
			// move/scale/rotate to the correct position
			transformInstance(i1, i2, d, correctedFrame);
			
			// render the image
			renderBitmap(i1);
			
			// done
			resetInstance(i1, i2);
			fRenderer.restore();
		}
		
		
		// walk over all symbol instances
		Collection<SymbolInstance> symbolInstances = f1.getSymbolInstances();
		for (SymbolInstance i1 : symbolInstances) {
			
			// get the instance in the second frame
			Instance i2 = f2.getInstance(i1.getLibraryItemName());
			
			// save transformation matrix
			fRenderer.save();
						
			// move/scale/rotate to the correct position
			transformInstance(i1, i2, d, correctedFrame);
			
			// render all sub-layers
			Timeline timeline = i1.getGraphic().getTimeline();
			Vector<Layer> layers = timeline.getLayers();
			for (Layer layer : layers) {
				drawLayer(layer, frame, i1.getCorrectFrame(frame), false);
			}
			
			// done
			resetInstance(i1, i2);
			fRenderer.restore();
		}
		
		
		// walk over all shapes - shapes have no tweens
		// TODO is this always true?
		Collection<Shape> shapes = f1.getShapes();
		for (Shape i1 : shapes) {
			
			// save transformation matrix
			fRenderer.save();
			
			// move/scale/rotate to the correct position
			transformInstance(i1, i1, d, correctedFrame);
			
			// render the shape
			renderShape(i1);
			
			// done
			resetInstance(i1, i1);
			fRenderer.restore();
		}*/
	}
	
	
	// render an instance
	private void transformInstance(Instance i1, Instance i2, double d, int frame) {
		
		/**
		 * STEP 1: compute scaling and rotation interpolation
		 */
		
		/**
		 * STEP 2: interpolate the transformation point and move there for scaling and rotation
		 */
		
		
		/**
		 * STEP 1: interpolate the registration point by rotating it around the 
		 */
		// translate to the transformation point
		// NOTE: this is actually not necessary, since this is contained within the transformation matrix itself
		//fRenderer.translate(i1.getTransformationPointX(), i1.getTransformationPointY());
		
		/**
		 * STEP 2: perform transformations
		 * Note: if we are not interpolating, i1 = i2 and this still works
		 */
		
		// normal tween animation
		if (!i1.hasInBetweenMatrices()) {
			
			// interpolate scaling
			double scaleX = interpolateValues(i1.getScaleX(), i2.getScaleX(), d);
			double scaleY = interpolateValues(i1.getScaleY(), i2.getScaleY(), d);
			
			// interpolate rotation, and make sure we always rotate an angle smaller than 180° (shortest path)
			double r1 = -i1.getRotation();
			double r2 = -i2.getRotation();
			if (Math.abs(r1-r2) > Math.PI) {
				if (r1 < r2) r1 += 2 * Math.PI;
				else r2 += 2 * Math.PI;
			}
			double rotation = interpolateValues(r1, r2, d);
			
			// perform linear interpolation between the two transformation points
			// these points have already been placed, scaled and rotated in their "final" position
			double translateX = interpolateValues(i1.getTransformationPointX(), i2.getTransformationPointX(), d);
			double translateY = interpolateValues(i1.getTransformationPointY(), i2.getTransformationPointY(), d);
			
			// move to this position
			fRenderer.translate(translateX, translateY);
			
			// translate to the correct location
			//fRenderer.translate(interpolateValues(i1.getTranslateX(), i2.getTranslateX(), d), interpolateValues(i1.getTranslateY(), i2.getTranslateY(), d));
			
			// scale
			fRenderer.scale(scaleX, scaleY);
			
			// rotate
			fRenderer.rotate(rotation);
			
			// move back to registration point
			fRenderer.translate(-i1.getRelativeTransformationPointX(), -i1.getRelativeTransformationPointY());
		}
		
		/**
		 * STEP 3: perform IK transformations if available
		 */
		
		// there is an IK pose in here
		if (i1.hasInBetweenMatrices()) {
			
			// get max frame index and make sure we don't cross it
			int maxFrameIndex = i1.getMaxIKFrameIndex();
			if (frame > maxFrameIndex) frame = maxFrameIndex;
			
			// get transformation matrix
			TransformationMatrix matrix = i1.getInBetweenMatrix(frame);
			
			// perform the transformation
			// scale values are not provided by the between matrices, so we take those from our original transformation
			fRenderer.translate(matrix.getTranslateX(), matrix.getTranslateY());
			fRenderer.scale(i1.getScaleX(), i1.getScaleY());
			fRenderer.rotate(matrix.getRotation());
		}
		
		/**
		 * STEP 4: perform color transformations
		 */
		
		// there is color manipulation
		if (i1.hasColorManipulation() || i2.hasColorManipulation()) {
			
			// we interpolate between default or set color manipulation
			ColorManipulation col = ColorManipulation.interpolate(i1.getColorManipulation(), i2.getColorManipulation(), d);
			fColorManipulationStack.push(col);
		}
	}
	
	
	// restore the internal state to its original form after all subobjects are rendered
	private void resetInstance(Instance i1, Instance i2) {

		// we pop the color manipulation from the stack
		if (i1.hasColorManipulation() || i2.hasColorManipulation()) {
			fColorManipulationStack.pop();
		}
	}
	
	
	// render the bitmap
	public void renderBitmap(BitmapInstance bitmap) {
		
		// set color manipulation
		if (!fColorManipulationStack.empty()) fRenderer.applyColorManipulation(fColorManipulationStack.peek());
		
		// draw the bitmap
		bitmap.setMask(fMask);
		bitmap.setMasked(fMasked);
		fRenderer.drawBitmapInstance(bitmap);
	}
	
	
	// render a shape
	public void renderShape(ShapeInstance shape) {
		shape.setMask(fMask);
		shape.setMasked(fMasked);
		fRenderer.drawShapeInstance(shape);
	}
	
	
	// interpolate two values
	private double interpolateValues(double p1, double p2, double d) {
		return p1 + (p2 - p1) * d;
	}
}
