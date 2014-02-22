package com.silenistudios.silenus.memory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream that reads its file data from a virtual file in memory.
 * @author Karel
 *
 */
public class MemoryInputStream extends InputStream {

	// datastore file
	byte[] fData;
	
	// index in the byte array
	int fIndex = 0;
	
	
	// create a datastore input stream for a given fla context and filename
	public MemoryInputStream(MemoryFile file) {
		fData = file.getData();
	}
	
	@Override
	public int read() throws IOException {
		int b = fData[fIndex++];
		return (fIndex == fData.length) ? -1 : b; 
	}
	
	
	@Override
	public int read(byte[] b) {
		if (b.length == 0) return 0;
		if (fIndex == fData.length) return -1;
		int i;
		for (i = 0; i < b.length; ++i, ++fIndex) {
			if (fIndex == fData.length) return i; 
			b[i] = fData[fIndex];
		}
		return i;
	}
	
	@Override
	public int read(byte[] b, int off, int len) {
		if (b.length == 0) return 0;
		if (fIndex == fData.length) return -1;
		int i;
		for (i = 0; i < len; ++i, ++fIndex) {
			if (fIndex == fData.length) return i; 
			b[i+off] = fData[fIndex];
		}
		return i;
	}
}
