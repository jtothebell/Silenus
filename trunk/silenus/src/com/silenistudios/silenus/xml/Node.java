package com.silenistudios.silenus.xml;

/**
 * Bare-bones implementation of an XML node that provides only the necessary
 * methods that Silenus uses. Create your subclass of this interface to
 * provide a gateway to the XML parser of your choice.
 * @author Karel
 *
 */
public interface Node {
	
	// get node name
	String getNodeName();
	
	
	
}
