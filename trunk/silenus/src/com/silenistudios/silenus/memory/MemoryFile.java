package com.silenistudios.silenus.memory;


/**
 * A virtual file in memory. Just keeps track of a byte array.
 * @author Karel
 *
 */
public class MemoryFile {
	
	// set?
	private boolean fSet = false;
	
	// data array
	private byte[] fData;
	
	// path
	String fPath;
	
	
	// create a new memory file
	public MemoryFile(String path) {
		fPath = path;
	}
	
	
	// update
	public void update(byte[] data) {
		fData = data;
		fSet = true;
	}
	
	
	// set?
	public boolean isSet() {
		return fSet;
	}
	
	
	// data
	public byte[] getData() {
		return fData;
	}
	
	
	// get path
	public String getPath() {
		return fPath;
	}
}
