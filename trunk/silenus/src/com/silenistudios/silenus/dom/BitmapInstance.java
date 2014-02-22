package com.silenistudios.silenus.dom;

import java.util.HashSet;
import java.util.Set;

import com.silenistudios.silenus.xml.Node;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.SceneRenderer;
import com.silenistudios.silenus.XFLLibrary;
import com.silenistudios.silenus.xml.XMLUtility;

public class BitmapInstance extends Instance {
	
	// the bitmap
	Bitmap fBitmap;

	// constructor
	public BitmapInstance(XMLUtility XMLUtility, XFLLibrary library, Node root, int frameIndex) throws ParseException {
		super(XMLUtility, root, frameIndex);
		
		// load the bitmap
		String libraryItemName = XMLUtility.getAttribute(root, "libraryItemName");
		
		// load the bitmap
		fBitmap = library.getBitmap(libraryItemName);
	}
	
	
	// return bitmap
	public Bitmap getBitmap() {
		return fBitmap;
	}
	
	
	// render this bitmap instance to the screen
	@Override
	public void render(SceneRenderer renderer, int frame) {
		renderer.renderBitmap(this);
	}
	
	
	// add the bitmap
	@Override
	public Set<Bitmap> getUsedImages(Set<String> symbolInstancesAlreadyChecked) {
		Set<Bitmap> v = new HashSet<Bitmap>();
		v.add(getBitmap());
		return v;
	}
	
	
	// just return the path
	@Override
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append("\"type\":\"bitmap\",");
		ss.append("\"path\":\"").append(fBitmap.getSourceHref()).append("\"");
		ss.append("}");
		return ss.toString();
	}


	@Override
	public int getFirstFrame() {
		return 0; // always 0 for bitmaps
	}
}
