package com.silenistudios.silenus.dat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.silenistudios.silenus.StreamFactory;
import com.silenistudios.silenus.ParseException;

/**
 * Will take care of handling .bin files that refer to jpeg's.
 * Jpeg's are strangely not compressed at all, so they are just copied.
 * @author Karel
 *
 */
public class DatJPEGReader implements DatReader {

	// output stream factory
	StreamFactory fStreamFactory;
	
	
	// constructor
	public DatJPEGReader(StreamFactory factory) {
		fStreamFactory = factory;
	}
	
	
	@Override
	public void parse(String inputFileName, String outputFileName) throws ParseException {
		
		// just perform a simple copy & paste
		try {
			
			// load both streams
			InputStream in = fStreamFactory.createInputStream(new File(inputFileName));
			OutputStream out = fStreamFactory.createOutputStream(new File(outputFileName));
			
			// set up a buffer and copy everything
			int BUFFERSIZE = 2048;
			byte data[] = new byte[BUFFERSIZE];
			int count = 0;
			while ((count = in.read(data, 0, BUFFERSIZE)) != -1) {
				out.write(data, 0, count);
			}
			out.flush(); out.close();
			in.close();
		}
		catch (IOException e) {
			throw new ParseException(e.getMessage(), e);
		}
	}
}
