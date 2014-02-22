package com.silenistudios.silenus.memory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.silenistudios.silenus.StreamFactory;

/**
 * This factory represents a virtual file system that can be used to unzip and parse an FLA file
 * and generate the appropriate subfiles (png's), without having to save them on disk.
 * This stream factory can be used if you want to generate only JSON or raw animation data,
 * without changing anything to the file system.
 * You will first have to create an output stream to copy the original FLA to, so that you
 * can then call set the stream factory on an XFLDocument and call XFLDocument.parse with the
 * correct filename for the output stream you wrote the FLA to.
 * XFLDocument will then work completely within this virtual filesystem.
 * @author Karel
 *
 */
public class MemoryStreamFactory implements StreamFactory {
	
	// map of string (filename) to byte array
	Map<String, MemoryFile> fFiles = new HashMap<String, MemoryFile>();
	

	@Override
	public OutputStream createOutputStream(File file) throws IOException {
		
		// create a new memory file and set it
		MemoryFile memoryFile = new MemoryFile(file.getPath());
		fFiles.put(file.getPath(), memoryFile);
		return new MemoryOutputStream(memoryFile);
	}

	@Override
	public InputStream createInputStream(File file) throws IOException {
		
		// does not exist :(
		if (!exists(file)) throw new IOException("File not found: " + file.getPath());
		
		// create a new memory file and set it
		MemoryFile memoryFile = fFiles.get(file.getPath());
		return new MemoryInputStream(memoryFile);
	}

	@Override
	public boolean exists(File file) {
		return fFiles.containsKey(file.getPath()) && fFiles.get(file.getPath()).isSet();
	}

}
