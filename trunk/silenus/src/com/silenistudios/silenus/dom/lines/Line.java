package com.silenistudios.silenus.dom.lines;

import com.silenistudios.silenus.ShapeRenderInterface;
import com.silenistudios.silenus.dom.Point;

/**
 * A single line piece used within a shape. Determines how to go from one point to the next.
 * I know this is not the best use for the word "line", but path is already taken so this is the best I could come up with.
 * This is not necessarily a straight line; it may also be a more complex shape, such as a curve.
 * @author Karel
 *
 */
public abstract class Line implements Comparable<Line> {
	
	// start and end coordinates
	Point fStart;
	
	
	// default constructor used for inversion
	protected Line() {
	}
	
	// constructor
	public Line(Point start) {
		fStart = start;
	}
	
	
	// set start manually (used for inversion)
	public void setStart(Point p) {
		fStart = p;
	}
	
	
	// get start
	public Point getStart() {
		return fStart;
	}
	
	// get stop
	public abstract Point getStop();
	
	
	// get the json for a basic line
	public abstract String getJSON();
	
	// invert the line
	public abstract Line invert();
	
	// execute instruction
	public abstract void render(ShapeRenderInterface renderer);
	
	
	// to string - temp
	// TODO remove
	@Override
	public String toString() {
		return fStart.toString() + " -> " + getStop().toString();
	}
	
	
	// this function will sort paths in terms of their angle between first and end point, from 0° to 360° in a highly efficient manner
	// source: http://stackoverflow.com/questions/7774241/sort-points-by-angle-from-given-axis
	// note: in flash, the topleft is the origin, so we need to invert some stuff to make sure we get the correct angle ordening
	@Override
	public int compareTo(Line line) {
		
		// translate both vectors to the origin - should be the same firstLine for both!
		int ax = this.getStop().getTwipX() - this.getStart().getTwipX();
		int ay = this.getStop().getTwipY() - this.getStart().getTwipY();
		int bx = line.getStop().getTwipX() - line.getStart().getTwipX();
		int by = line.getStop().getTwipY() - line.getStart().getTwipY();
		
		if (ay > 0) { // a between 180 and 360
			if (by < 0)  // b between 0 and 360
				return 1;
			return ax < bx ? -1 : 1; // both between 180 and 360 
		} else { // a between 0 and 180
			if (by > 0) // b between 180 and 360
				return -1;
			return ax > bx ? -1 : 1; // both between 0 and 180
		}
	}
}
