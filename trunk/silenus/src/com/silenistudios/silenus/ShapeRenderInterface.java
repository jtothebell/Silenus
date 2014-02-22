package com.silenistudios.silenus;

import java.util.Vector;

import com.silenistudios.silenus.dom.StrokeStyle;
import com.silenistudios.silenus.dom.fillstyles.Color;
import com.silenistudios.silenus.dom.fillstyles.ColorStop;


/**
 * This interface is used to render a shape (using the pen tool for drawing) directly in java. It can be passed to a Shape object
 * and it will then draw the entire shape in the correct color and shape.
 * @author Karel
 *
 */
public interface ShapeRenderInterface {
	
	// move the pen to a given position
	public void moveTo(double x, double y);
	
	// draw a line from the current pen position to this new position
	public void lineTo(double x, double y);
	
	// draw a quadratic line to this position, using the given control point
	public void quadraticCurveTo(double controlX, double controlY, double targetX, double targetY);
	
	// fill the path with a solid color
	public void fillSolidColor(Color color);
	
	// fill the path with a linear gradient
	public void fillLinearGradient(double startX, double startY, double stopX, double stopY, Vector<ColorStop> colorStops);
	
	// stroke the path with the given stroke style
	public void stroke(StrokeStyle strokeStyle);
	
	// clip
	public void clip();
}
