package com.silenistudios.silenus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.silenistudios.silenus.dom.*;
import com.silenistudios.silenus.xml.XMLUtility;
import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.xml.java.JavaXMLUtility;

/**
 * This class will parse an entire XFL scene and generate keyframe data for all objects.
 * It will load the different graphics and call the RenderInterface to draw the animation on screen.
 * @author Karel
 *
 */
public class XFLDocument implements XFLLibrary{
	
	// root directory
	String fRoot;
	
	// renderer
	RenderInterface fRenderer;
	
	// XML Utility
	XMLUtility XMLUtility = new JavaXMLUtility();
	
	// map of all bitmaps
	Map<String, Bitmap> fBitmaps = new HashMap<String, Bitmap>();
	
	// graphics
	Map<String, Graphic> fGraphics = new HashMap<String, Graphic>();
	
	// scenes
	Map<String, Timeline> fScenes = new HashMap<String, Timeline>();
	
	// width of the document
	int fWidth;
	
	// height of the document
	int fHeight;
	
	// frame rate
	int fFrameRate;
	
	// number of includes not yet loaded
	int fNIncludesLeft;
	
	// the output stream factory
	StreamFactory fStreamFactory = new DefaultStreamFactory();
	
	// create an XFL parser
	public XFLDocument() {
	}
	
	
	// set the stream factory
	public void setStreamFactory(StreamFactory factory) {
		fStreamFactory = factory;
	}
	
	
	// set XML utility
	public void setXMLUtility(XMLUtility XMLUtility) {
		this.XMLUtility = XMLUtility;
	}
	
	
	// parse an XFL directory or CS5 .FLA file
	// must be passed either the directory name (with no file at the end) or the entire path to the .FLA file, including the filename
	public void parseXFL(String pathName) throws ParseException {
		
		// strip "/" from the path at the end if necessary
		if (pathName.charAt(pathName.length()-1) == '/') {
			pathName = pathName.substring(0, pathName.length()-2);
		}
		
		// see if the directory ends with .fla
		String[] splitPathName = pathName.split("\\.");
		if (splitPathName.length > 1) {
			
			// this is an FLA file - unzip it first, then parse it
			if (splitPathName[splitPathName.length-1].equalsIgnoreCase("fla")) {
				
				// update path name to remove the filename
				splitPathName = pathName.split("[\\/]+");
				StringBuilder ss = new StringBuilder();
				for (int i = 0; i < splitPathName.length-1; ++i) {
					ss.append(splitPathName[i]).append("/");
				}
				
				// unzip
				unzipFLA(pathName, ss.toString());
				
				// update path name
				pathName = ss.toString();
			}
		}
		
		// root dir
		fRoot = pathName;
		
		// read DOMDocument.xml, the root document
		Node rootNode = XMLUtility.parseXML(fStreamFactory, fRoot, "DOMDocument.xml");
		loadDOMDocument(rootNode);
	}
	
	
	// unzip an FLA fipe
	private void unzipFLA(String fileName, String pathName) throws ParseException {
		try {
			
			// read zip file
			InputStream fis = fStreamFactory.createInputStream(new File(fileName));
			ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fis));
			
			// get all entries in the zip file
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				
				// set up a buffer
				int BUFFER = 2048;
				byte data[] = new byte[BUFFER];
				
				// it's a directory - create it
				if (entry.isDirectory()) {
					//new File(pathName, entry.getName()).mkdirs();
				}
				
				// it's a file - extract & save the file
				else {
					
					// create parent directory
			        File outputFile = new File(pathName, entry.getName());
			        
			        // extract & save the file
					OutputStream fos = fStreamFactory.createOutputStream(outputFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
					int count;
					while ((count = zin.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
				}
			}
			zin.close();
		}
		catch (FileNotFoundException e) {
			throw new ParseException("File not found: '" + fileName + "'", e);
		}
		catch (IOException e) {
			throw new ParseException("Failed to read FLA (zip) file '" + fileName + "'. Perhaps you are uploading a CS4 or older FLA file?", e);
		}
	}
	
	
	// load the DOM document
	private void loadDOMDocument(Node root) throws ParseException {
		
		// width and height
		fWidth = XMLUtility.getIntAttribute(root, "width", 550);
		fHeight = XMLUtility.getIntAttribute(root, "height", 400);
		
		// frame rate
		fFrameRate = XMLUtility.getIntAttribute(root, "frameRate", 24);
		
		// load the media and convert the binary files back to png
		if (XMLUtility.hasNode(root, "media")) {
			Node media = XMLUtility.findNode(root,  "media");
			Vector<Node> bitmaps = XMLUtility.findNodes(media, "DOMBitmapItem");
			for (Node node : bitmaps) {
				Bitmap bitmap = new Bitmap(XMLUtility, fStreamFactory, fRoot, node);
				fBitmaps.put(bitmap.getName(), bitmap);
			}
		}
		
		
		// read all symbols - don't load them yet!
		if (XMLUtility.hasNode(root,  "symbols")) {
			Node symbols = XMLUtility.findNode(root, "symbols");
			Vector<Node> includes = XMLUtility.findNodes(symbols, "Include");
			Map<String, Node> nameToNode = new HashMap<String, Node>();
			for (Node node : includes) 	loadInclude(nameToNode, node);
			
			// now, load the graphics
			// by using this trick, we can resolve references immediately
			for (Entry<String, Node> entry : nameToNode.entrySet()) {
				getGraphic(entry.getKey()).loadGraphic(XMLUtility, this, entry.getValue());
			}
		}
		
		// read the scenes
		Vector<Node> scenes = XMLUtility.findNodes(root, "DOMTimeline");
		for (Node node : scenes) {
			Timeline timeline = new Timeline(XMLUtility, this, node);
			fScenes.put(timeline.getName(), timeline);
		}
	}
	
	
	// load an include from a separate XML
	private void loadInclude(final Map<String, Node> nameToNode, Node node) throws ParseException {
		
		// get the href
		String href = XMLUtility.getAttribute(node, "href");
		
		// load the XML file
		Node include = XMLUtility.parseXML(fStreamFactory, fRoot, "LIBRARY/" + href);
		
		// get name
		String name = XMLUtility.getAttribute(include, "name", "");
		if (name.equals("")) throw new ParseException("Invalid filename found for include: '" + href + "'");
		
		// add this item to the map
		nameToNode.put(name, include);
		
		// see if the symbol type exists
		/*if (!XMLUtility.hasAttribute(include, "symbolType")) {
			// unknown type, skip
			nameToNode.remove(name);
			return;
		}*/
		
		// get the symbol type
		String symbolType = XMLUtility.getAttribute(include, "symbolType", "graphic");
		
		// graphic - only one supported right now
		if (symbolType.equals("graphic")) {
			
			// create a new graphic from this data - this is just a dummy at the moment
			Graphic graphic = new Graphic();
			
			// add to list
			fGraphics.put(name, graphic);
		}
	}
	
	
	// get a graphic
	@Override
	public Graphic getGraphic(String href) throws ParseException {
		
		// try to find the graphic
		Graphic graphic = fGraphics.get(href);
		
		// not found - throw exception
		if (graphic == null) throw new ParseException("Graphic reference could not be resolved: " + href);
		
		// all ok
		return graphic;
	}
	
	
	// get a bitmap
	@Override
	public Bitmap getBitmap(String href) throws ParseException {
		
		// try to find the graphic
		Bitmap bitmap = fBitmaps.get(href);
		
		// not found - throw exception
		if (bitmap == null) throw new ParseException("Bitmap reference could not be resolved: " + href);
		
		// all ok
		return bitmap;
	}
	
	
	// get a scene
	public Timeline getScene(String name) {
		return fScenes.get(name);
	}
	
	
	// get the first scene
	public Timeline getScene() {
		return fScenes.values().iterator().next();
	}
	
	
	// get width
	public int getWidth() {
		return fWidth;
	}
	
	
	// get height
	public int getHeight() {
		return fHeight;
	}
	
	
	// get frame rate
	public int getFrameRate() {
		return fFrameRate;
	}
}
