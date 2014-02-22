package com.silenistudios.silenus.dom.lines;

import java.util.regex.Matcher;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.ShapeRenderInterface;
import com.silenistudios.silenus.dom.Point;


/**
 * A quadratic curve between two points.
 * @author Karel
 *
 */
public class QuadraticCurve extends Line {
	
	// control point
	Point fControlPoint;
	
	// the end point
	Point fStop;
	
	
	// default constructor
	protected QuadraticCurve() {
	}
	
	
	// parse the string
	public QuadraticCurve(Point start, String s) throws ParseException {
		super(start);
		
		// parse the instruction
		Matcher matcher = Point.getRegExpCompiled().matcher(s);
		
		// find the two points
		boolean found = matcher.find();
		if (!found || matcher.groupCount() != 2) throw new ParseException("Invalid quadratic curve instruction found in DOMShape: \"" + s + "\"");
		fControlPoint = new Point(matcher.group(1), matcher.group(2));
		found = matcher.find();
		if (!found || matcher.groupCount() != 2) throw new ParseException("Invalid quadratic curve instruction found in DOMShape: \"" + s + "\"");
		fStop = new Point(matcher.group(1), matcher.group(2));
	}

	@Override
	public Point getStop() {
		return fStop;
	}

	@Override
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{\"type\":\"quadraticCurveTo\",");
		ss.append("\"control\":").append(fControlPoint.getJSON()).append(",");
		ss.append("\"p\":").append(fStop.getJSON());
		ss.append("}");
		return ss.toString();
	}

	@Override
	public Line invert() {
		QuadraticCurve inverse = new QuadraticCurve();
		inverse.setStart(fStop);
		inverse.fStop = getStart();
		inverse.fControlPoint = fControlPoint;
		return inverse;
	}


	@Override
	public void render(ShapeRenderInterface renderer) {
		renderer.quadraticCurveTo(fControlPoint.getX(), fControlPoint.getY(), fStop.getX(), fStop.getY());
	}
}
