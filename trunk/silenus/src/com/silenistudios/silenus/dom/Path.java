package com.silenistudios.silenus.dom;

import java.util.LinkedList;
import java.util.List;

import com.silenistudios.silenus.ShapeRenderInterface;
import com.silenistudios.silenus.dom.lines.Line;

/**
 * A path represents a set of points that follow each other, and that can be filled or stroked.
 * @author Karel
 *
 */
public class Path {
	
	// internal ID counter
	private static int IdCounter = 0;
	
	// unique ID of this path, used for quick comparison
	long fId;
	
	// set of lines in this path
	List<Line> fLines = new LinkedList<Line>();
	
	// index - can represent a stroke or fill index
	int fIndex;
	
	
	// constructor
	public Path(int index) {
		fIndex = index;
		fId = ++IdCounter;
	}
	
	
	// add a point at the end
	public void add(Line p) {
		fLines.add(p);
	}
	
	
	// add a point at the specified index
	public void add(int index, Line p) {
		fLines.add(index, p);
	}
	
	
	// get number of points
	public int getNLines() {
		return fLines.size();
	}
	
	
	// get all points
	public List<Line> getLines() {
		return fLines;
	}
	
	
	// get stroke or fill index
	public int getIndex() {
		return fIndex;
	}
	
	
	// closed?
	public boolean isClosed() {
		return fLines.get(0).getStart().equals(fLines.get(fLines.size()-1).getStop());
	}
	
	
	// equals for list
	public boolean equals(Path path) {
		if (path == null) return false;
		return fId == path.fId;
	}
	
	
	// render the path to a renderer - use some magic to take into account the moveTo actions
	public void render(ShapeRenderInterface renderer) {
		
		// first, move to the starting position
		Point prevStop = fLines.get(0).getStart();
		renderer.moveTo(prevStop.getX(), prevStop.getY());
		
		// walk over all lines, and render them in subsequent order
		for (Line line : fLines) {
			
			// HOWEVER, if the stop position of the previous line differs from the start position of the current one,
			// we issue another moveTo command. I have not seen this happen in .fla files yet, but it might.
			if (!line.getStart().equals(prevStop)) renderer.moveTo(line.getStart().getX(), line.getStart().getY());
			
			// render the line
			line.render(renderer);
			prevStop = line.getStop();
		}
	}
	
	
	// to string
	@Override
	public String toString() {
		StringBuilder ss = new StringBuilder();
		boolean first = true;
		for (Line p : fLines) {
			if (first) first = false;
			else ss.append(" -> ");
			ss.append(p.toString());
		}
		return ss.toString();
	}
	
	
	// get JSON
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append("\"points\":[");
		
		// first, move to the start position
		Point prevStop = fLines.get(0).getStart();
		ss.append(getMoveToJSON(prevStop));
		for (Line line : fLines) {
			ss.append(",");
			if (!line.getStart().equals(prevStop)) ss.append(getMoveToJSON(line.getStart())).append(",");
			ss.append(line.getJSON());
			prevStop = line.getStop();
		}
		ss.append("],");
		ss.append("\"index\":").append(fIndex);
		ss.append("}");
		return ss.toString();
	}
	
	
	// append move to
	private String getMoveToJSON(Point p) {
		StringBuilder ss = new StringBuilder();
		ss.append("{\"type\":\"moveTo\",");
		ss.append("\"p\":").append(p.getJSON());
		ss.append("}");
		return ss.toString();
	}
}
