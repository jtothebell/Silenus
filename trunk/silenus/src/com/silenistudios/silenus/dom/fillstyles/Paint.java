package com.silenistudios.silenus.dom.fillstyles;

import com.silenistudios.silenus.ShapeRenderInterface;

/**
 * Paint base interface. This interface just has a render function that allows different subclasses to call
 * the appropriate rendering function for itself. It also contains a getJSON method for exporting.
 * @author Karel
 *
 */
public interface Paint {
	public void render(ShapeRenderInterface renderer);
	public String getJSON();
}
