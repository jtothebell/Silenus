package com.silenistudios.silenus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultStreamFactory implements StreamFactory {

	@Override
	public OutputStream createOutputStream(File file) throws IOException {
		
		// make sure the file exists
		if (file.getParentFile() != null && !file.getParentFile().exists()) file.getParentFile().mkdirs();
		
		// set up the file output stream
		return new FileOutputStream(file);
	}

	@Override
	public InputStream createInputStream(File file) throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public boolean exists(File file) {
		return file.exists();
	}

}
