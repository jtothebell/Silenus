package com.silenistudios.silenus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Factory used for producing output streams.
 * Allows the user to overwrite the default FileOutputStream by something custom.
 * @author Karel
 *
 */
public interface StreamFactory {
	
	// create output stream
	public OutputStream createOutputStream(File file) throws IOException;
	
	// create input stream
	public InputStream createInputStream(File file) throws IOException;
	
	// see if a file exists
	public boolean exists(File file);
}
