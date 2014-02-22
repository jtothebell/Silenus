package com.silenistudios.silenus.dom.lines;

import java.util.regex.Matcher;
import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.ShapeRenderInterface;
import com.silenistudios.silenus.dom.Point;

public class StraightLine extends Line {
	
	// the end point
	Point fStop;
	
	
	// default constructor
	protected StraightLine() {
	}
	
	
	// constructor
	public StraightLine(Point start, String s) throws ParseException {
		super(start);
		
		// parse the instruction
		Matcher matcher = Point.getRegExpCompiled().matcher(s);
		
		// invalid string
		if (!matcher.find() || matcher.groupCount() != 2) throw new ParseException("Invalid line instruction found in DOMShape: \"" + s + "\"");
		
		// all ok
		fStop = new Point(matcher.group(1), matcher.group(2));
	}
	
	
	@Override
	public Point getStop() {
		return fStop;
	}
	

	@Override
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{\"type\":\"lineTo\",");
		ss.append("\"p\":").append(fStop.getJSON());
		ss.append("}");
		return ss.toString();
	}


	@Override
	public Line invert() {
		StraightLine inverse = new StraightLine();
		inverse.fStart = fStop;
		inverse.fStop = fStart;
		return inverse;
	}
	

	@Override
	public void render(ShapeRenderInterface renderer) {
		renderer.lineTo(fStop.getX(), fStop.getY());
	}
}
