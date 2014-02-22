package com.silenistudios.silenus;

import com.silenistudios.silenus.dom.Bitmap;
import com.silenistudios.silenus.dom.Graphic;

/**
 * This interface allows access to the library,
 * allowing graphics to resolve their references.
 * @author Karel
 *
 */
public interface XFLLibrary {
	
	// get a graphic
	public Graphic getGraphic(String href) throws ParseException;
	
	// get a bitmap
	public Bitmap getBitmap(String href) throws ParseException;

}
