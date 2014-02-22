package com.silenistudios.silenus.dom;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.SceneRenderer;
import com.silenistudios.silenus.ShapeRenderInterface;
import com.silenistudios.silenus.dom.fillstyles.FillStyle;
import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.xml.XMLUtility;

/**
 * A shape represents a vector drawing in flash.
 * @author Karel
 *
 */
public class ShapeInstance extends Instance {
	
	// shapes have a unique id, which is used as a reference point in the RawDataRender,
	// to make sure each shape is only stored once. This is similar to the libraryMap for Bitmaps,
	// but since shapes are inherently not saved in a separate structure in the DOM, we generate an id ourselves.
	long fId;
	
	// the internal static counter used to generate unique id's
	private static long IdCounter = 0;
	
	// fill styles defined for this shape
	Vector<FillStyle> fFillStyles = new Vector<FillStyle>();
	
	// stroke styles defined for this shape
	Vector<StrokeStyle> fStrokeStyles = new Vector<StrokeStyle>();
	
	// list of completed fill paths
	Vector<Path> fFillPaths = new Vector<Path>();
	
	// list of completed stroke paths
	Vector<Path> fStrokePaths = new Vector<Path>();
	
	
	// constructor
	public ShapeInstance(XMLUtility XMLUtility, Node root, int frameIndex) throws ParseException {
		super(XMLUtility, root, frameIndex);
		
		// get all the fills
		if (XMLUtility.hasNode(root, "fills")) {
			Node node = XMLUtility.findNode(root,  "fills");
			Vector<Node> fills = XMLUtility.findNodes(node, "FillStyle");
			
			for (Node fillNode : fills) {
				FillStyle style = new FillStyle(XMLUtility, fillNode);
				fFillStyles.add(style);
			}
		}
		
		// get all the strokes
		if (XMLUtility.hasNode(root,  "strokes")) {
			Node node = XMLUtility.findNode(root,  "strokes");
			Vector<Node> strokes = XMLUtility.findNodes(node, "StrokeStyle");
			for (Node strokeNode : strokes) {
				StrokeStyle style = new StrokeStyle(XMLUtility, strokeNode);
				fStrokeStyles.add(style);
			}
		}
		
		// generate the paths from the edges using PathGenerator
		Node node = XMLUtility.findNode(root,  "edges");
		PathGenerator pathGenerator = new PathGenerator();
		pathGenerator.generate(XMLUtility, node);
		fStrokePaths = pathGenerator.getStrokePaths();
		fFillPaths = pathGenerator.getFillPaths();
		// verify all the links with fill/stroke styles
		for (Path path : fStrokePaths) if (path.getIndex() >= fStrokeStyles.size()) throw new ParseException("Non-existing stroke style refered in path");
		for (Path path : fFillPaths) if (path.getIndex() >= fFillStyles.size()) throw new ParseException("Non-existing stroke style refered in path");
		
		// generate a unique id
		fId = IdCounter++;
		
		// set the id with some random gibberish as the hash - let's hope nobody ever uses this exact library name
		setLibraryItemName("########SHAPE#########+++" + fId + "+++");
		
	}
	
	
	// get id
	public long getId() {
		return fId;
	}
	
	
	// get a stroke style
	public StrokeStyle getStrokeStyle(int index) {
		return fStrokeStyles.get(index);
	}
	
	
	// get a fill style
	public FillStyle getFillStyle(int index) {
		return fFillStyles.get(index);
	}
	
	
	// get stroke paths
	public Vector<Path> getStrokePaths() {
		return fStrokePaths;
	}
	
	
	// get fill paths
	public Vector<Path> getFillPaths() {
		return fFillPaths;
	}
	
	
	// render the shape
	public void render(ShapeRenderInterface renderer) {
		
		// draw all the fill paths
		Vector<Path> fillPaths = getFillPaths();
		for (Path path : fillPaths) {
			path.render(renderer);
			
			// depending on whether we're in mask mode or not, we fill or clip
			if (!isMask()) getFillStyle(path.getIndex()).getPaint().render(renderer);
			else renderer.clip();
		}
		
		
		// draw all the stroke paths
		Vector<Path> strokePaths = getStrokePaths();
		for (Path path : strokePaths) {
			path.render(renderer);
			renderer.stroke(getStrokeStyle(path.getIndex()));
		}
	}
	
	
	// get JSON
	@Override
	public String getJSON() {
		
		// first, construct a straight vector of the fill and stroke styles, 
		
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append("\"type\":\"shape\",");
		ss.append("\"strokeStyles\":[");
		for (int i = 0; i < fStrokeStyles.size(); ++i) {
			if (i != 0) ss.append(",");
			ss.append(fStrokeStyles.get(i).getJSON());
		}
		ss.append("],");
		ss.append("\"fillStyles\":[");
		for (int i = 0; i < fFillStyles.size(); ++i) {
			if (i != 0) ss.append(",");
			ss.append(fFillStyles.get(i).getJSON());
		}
		ss.append("],");
		ss.append("\"strokePaths\":[");
		for (int i = 0; i < fStrokePaths.size(); ++i) {
			if (i != 0) ss.append(",");
			ss.append(fStrokePaths.get(i).getJSON());
		}
		ss.append("],");
		ss.append("\"fillPaths\":[");
		for (int i = 0; i < fFillPaths.size(); ++i) {
			if (i != 0) ss.append(",");
			ss.append(fFillPaths.get(i).getJSON());
		}
		ss.append("]");
		ss.append("}");
		return ss.toString();
	}
	
	
	// render the shape to the screen
	@Override
	public void render(SceneRenderer renderer, int frame) {
		renderer.renderShape(this);
	}
	
	
	// no bitmaps used by shapes
	@Override
	public Set<Bitmap> getUsedImages(Set<String> symbolInstancesAlreadyChecked) {
		
		// none used
		return new HashSet<Bitmap>();
	}

	// TODO it is probably possible to define a first frame for shapes too - implement this later
	@Override
	public int getFirstFrame() {
		return 0;
	}
}
