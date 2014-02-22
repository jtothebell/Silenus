package com.silenistudios.silenus.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.dom.lines.Line;
import com.silenistudios.silenus.dom.lines.QuadraticCurve;
import com.silenistudios.silenus.dom.lines.StraightLine;
import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.xml.XMLUtility;

/**
 * This helper class will parse subsequent <Edge> nodes, and generate the appropriate path
 * objects from the somewhat strange data format that is used in the XML file.
 * @author Karel
 *
 */
public class PathGenerator {
	
	// list of completed fill paths
	Vector<Path> fFillPaths = new Vector<Path>();
	
	// list of completed stroke paths
	Vector<Path> fStrokePaths = new Vector<Path>();
	
	// open paths - paths that still have to be closed
	List<Path> fOpenPaths = new LinkedList<Path>();
	
	// constructor
	public PathGenerator() {
	}
	
	
	// generate the appropriate paths
	public void generate(XMLUtility XMLUtility, Node root) throws ParseException {
		
		// list of all edges, sorted by fill type and then mapped by a hash defined by their endpoint for easy connection
		Map<Integer, Map<String, List<Line>>> pathsByColor = new HashMap<Integer, Map<String, List<Line>>>();
		
		// get all Edge nodes
		Vector<Node> edges = XMLUtility.findNodes(root, "Edge");
		int[] fillTypes = new int[2];
		for (Node edge : edges) {
			
			// get points of this edge
			Vector<Line> lines = null;
			try {
				lines = getLines(XMLUtility, edge);
			}
			catch (ParseException e) {
				//e.printStackTrace();
				// we really couldn't parse this line - we skip it
				continue;
			}
			
			// cubics or other editor-only lines
			if (lines == null)	continue;
			
			// walk over all lines in this edge and sort them by color and start point
			for (int i = 0; i < lines.size(); ++i) {
				
				// get the different types
				// IMPORTANT
				// IMPORTANT note that we subtract 1 here - the indices are counted starting from 1, while we store fill and stroke styles starting from 0 in an array
				// IMPORTANT
				fillTypes[0] = XMLUtility.getIntAttribute(edge, "fillStyle0", 0) - 1;
				fillTypes[1] = XMLUtility.getIntAttribute(edge, "fillStyle1", 0) - 1;
				int strokeType = XMLUtility.getIntAttribute(edge, "strokeStyle", 0) - 1; // -1 is "invalid"
				
				// we simply add the stroke paths
				if (strokeType != -1) {
					Path path = new Path(strokeType);
					path.add(lines.get(i));
					fStrokePaths.add(path);
				}
				
				// we consider all open paths
				for (int fillType = 0; fillType < 2; ++fillType) {
					
					// no no fill type set
					if (fillTypes[fillType] == -1) continue;
					
					// create the path - inverted if it's a fillType1
					Line line = lines.get(i);
					if (fillType == 1) line = line.invert();
					
					// add to the list of paths
					String hash = getPointHash(line.getStart());
					if (!pathsByColor.containsKey(fillTypes[fillType])) pathsByColor.put(fillTypes[fillType], new HashMap<String, List<Line>>());
					Map<String, List<Line>> paths = pathsByColor.get(fillTypes[fillType]);
					if (!paths.containsKey(hash)) paths.put(hash, new ArrayList<Line>());
					paths.get(hash).add(line);
				}
			}
		}
		
		// now we walk over all paths with the same color and try to merge them
		for (Entry<Integer, Map<String, List<Line>>> entry : pathsByColor.entrySet()) {
			Integer fillType = entry.getKey();
			Map<String, List<Line>> hash = entry.getValue();
			
			// keep going until the hash is empty
			while (hash.size() > 0) {
				
				// get the first list of paths in the hash
				List<Line> paths = hash.values().iterator().next();
				
				// get the first point in there and remove it from the list, and turn it into a path
				Line firstLine = paths.get(0);
				Path path1 = new Path(fillType);
				path1.add(firstLine);
				paths.remove(0);
				
				// this list is now empty - delete it from the map
				if (paths.size() == 0) hash.remove(getPointHash(firstLine.getStart()));
				
				// compute the hash for the endpoint
				String endHash = getPointHash(firstLine.getStop());
				
				// find all points that match this hash in the first point
				List<Line> connections = hash.get(endHash);
				
				// invalid connection - might be unknown edge type
				if (connections == null) continue;
				
				// keep going until we can't find any connections anymore
				Line connection = firstLine;
				while (connections.size() > 0) {
					
					// add the last connection to the collection for sorting, but only do so when the inverse is not already present
					// this entire structure is needed for when two identically coloured fills lie next to each other
					// in this case, the endHash will contain connection itself... and we don't want to match ourselves,
					// or we'll get stuck in an infinite loop
					boolean alreadyInverse = false;
					Line connectionInverted = null;
					for (Line p : connections) {
						if (p.getStart().equals(connection.getStop()) && p.getStop().equals(connection.getStart())) {
							alreadyInverse = true;
							connectionInverted = p;
						}
					}
					
					
					// invert the last connection and add it to the list, so we can find the next angle
					if (!alreadyInverse) {
						connectionInverted = connection.invert();
						connections.add(connectionInverted);
					}
					
					// sort the connections by angle
					Collections.sort(connections);
					
					// now find our own point in the list - the point previous to it is our connection!
					ListIterator<Line> it = connections.listIterator(connections.size());
					//while (it.hasPrevious() && !it.previous().equals(connectionInverted));
					while (it.hasPrevious()) {
						Line p = it.previous();
						if (!p.equals(connectionInverted)) {
						}
						else {
							if (!alreadyInverse) it.remove();
							break;
						}
					}
					
					// there is a previous path - this is the one!
					if (it.hasPrevious()) {
						connection = it.previous();
						it.remove();
					}
					
					// no previous path - our own line is the smallest angle, get the largest one
					else {
						connection = connections.get(connections.size()-1);
						connections.remove(connections.size()-1);
					}					
					
					// no connections in here anymore
					if (connections.size() == 0) {
						hash.remove(endHash);
					}
					
					// we add this point to the path
					path1.add(connection);
					
					// closed?
					if (path1.isClosed()) {
						fFillPaths.add(path1);
						break;
					}
					
					// recompute the hash
					endHash = getPointHash(connection.getStop());
					
					// find all points that match this hash in the first point
					connections = hash.get(endHash);
					
					// invalid connection - might be unknown edge type
					if (connections == null) break;
				}
			}
		}
	}
	
	
	// compute a hash for a point
	private static String getPointHash(Point p) {
		return p.getTwipX() + "_" + p.getTwipY();
	}
	

	// the pattern for parsing set of path instructions
	// TODO what does the "[" that sometimes occurs instead of "|" mean?
	// TODO sometimes letters come behind the numbers, such as "!895 -3557S1|134 -3366!134 -3366|135 -2925". What does it mean?
	// TODO another weird construct appearing in the edges list: "!10214.5 2608.5[#27EC.6F #A5D.06 10226.5 2697.5"
	private static Pattern LinePattern = Pattern.compile("([-]?[0-9]*[\\.]?[0-9]+)\\s+([-]?[0-9]*[\\.]?[0-9]+)[S]?[0-9]*\\s*[\\|\\[]{1}\\s*([-]?[0-9]*[\\.]?[0-9]+)\\s+([-]?[0-9]*[\\.]?[0-9]+).*");
	//private static Pattern InstructionPattern = Pattern.compile("[!\\/\\|\\[\\]S][^!\\/\\|\\[\\]S]+");
	private static Pattern InstructionPattern = Pattern.compile("[!\\x7C\\[\\]\\/][^!\\x7C\\[\\]\\/]+");
	
	// get all the points in this edge
	private Vector<Line> getLines(XMLUtility XMLUtility, Node edge) throws ParseException {
		
		String edgesString = null;
		try {
			edgesString = XMLUtility.getAttribute(edge, "edges");
		} catch (ParseException e) {
			if (XMLUtility.hasAttribute(edge, "cubics"))
			{
				return null;
			}
			throw new ParseException("Cannot parse line");
		}
		
		// split the edges string up into different instructions
		Matcher matcher = InstructionPattern.matcher(edgesString);
		Vector<Line> lines = new Vector<Line>();
		Point lastStop = null;
		
		while (matcher.find()) {
			
			// find the appropriate instruction
			String s = edgesString.substring(matcher.start(), matcher.end());
			char instruction = s.charAt(0);
			
			// add the different instructions
			switch (instruction) {
				
				// move to - special case, we do not store a line for this, but just update the last stop point
				case '!':
					lastStop = parseMoveTo(s);
					break;
					
				case '|':
				case '/':
					lines.add(new StraightLine(lastStop, s));
					break;
					
				case '[':
				case ']':
					lines.add(new QuadraticCurve(lastStop, s));
					break;
			}
		}
		
		// done
		return lines;
	}
	
	
	// parse moveTo
	private Point parseMoveTo(String s) throws ParseException {
		Matcher matcher = Point.getRegExpCompiled().matcher(s);
		if (!matcher.find() || matcher.groupCount() != 2) throw new ParseException("Invalid move to instruction found in DOMShape: \"" + s + "\"");
		return new Point(matcher.group(1), matcher.group(2));
	}
	
	
	// get stroke paths
	public Vector<Path> getStrokePaths() {
		return fStrokePaths;
	}
	
	
	// get fill paths
	public Vector<Path> getFillPaths() {
		return fFillPaths;
	}
}
