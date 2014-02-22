package com.silenistudios.silenus.dat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.silenistudios.silenus.StreamFactory;
import com.silenistudios.silenus.ParseException;

import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Reader that reads and extracts png data from .dat files in the bin directory.
 * Based on: http://stackoverflow.com/questions/4082812/xfl-what-are-the-bin-dat-files
 * @author Karel
 *
 */
public class DatPNGReader implements DatReader {
	
	// width and height
	short fWidth;
	short fHeight;
	
	// stream
	LittleEndianDataInputStream stream;
	
	// outputstream
	OutputStream outStream;
	
	// buffer size
	static final int BufferSize = 512;
	
	// output stream factory
	StreamFactory fStreamFactory;
	
	
	// constructor
	public DatPNGReader(StreamFactory factory) {
		fStreamFactory = factory;
	}
	
	// read a dat file and produce a png
	@Override
	public void parse(String inputFileName, String outputFileName) throws ParseException {
		
		try {
			
			// file
			InputStream file = fStreamFactory.createInputStream(new File(inputFileName));
			
			// pass to data input stream
			stream = new LittleEndianDataInputStream(file);
			
			// check the bitmap type
			if (stream.readShort() != 0x0503) { // 0
				throw new ParseException("Invalid bitmap identifier: no png format");
			}
			
			// length of decompressed row data
			// TODO what is this?
			short length = stream.readShort(); // 2
			
			// width
			fWidth = stream.readShort(); // 4
			fHeight = stream.readShort(); // 6
			
			// output png file
			outStream = new PNGOutputStream(fStreamFactory.createOutputStream(new File(outputFileName)), fWidth, fHeight);
			
			// skip some empty bytes
			stream.skipBytes(4); // 8
			
			// width in twips
			int twipsWidth = stream.readInt(); // 12
			
			// unknown
			stream.skipBytes(4); // 16
			
			// height in twips
			int twipsHeight = stream.readInt(); // 20
			
			// flag
			int flag = stream.readByte(); // 24
			
			// data compressed or uncompressed?
			int compressed = stream.readByte(); // 25
			if (compressed == 1) {
				parseCompressed();
			}
			else parseUncompressed();
		}
		catch (DataFormatException e) {
			throw new ParseException(e.getMessage(), e);
		}
		catch (IOException e) {
			throw new ParseException(e.getMessage(), e);
		}
	}
	
	
	// compressed
	private void parseCompressed() throws DataFormatException, ParseException, IOException {
		
		// inflater
		Inflater decompresser = new Inflater();
		
		// read length of compressed chunk
		short length = stream.readShort();
		
		// buffer
		byte[] buffer = new byte[BufferSize];
		
		// keep going until we reach the end
		while (length > 0) {
			
			// read the compressed data into a buffer
			byte[] input = new byte[length];
			stream.read(input, 0, length);
			
			// decompress
			decompresser.setInput(input, 0, length);
			int n = decompresser.inflate(buffer,  0, BufferSize);
			while (n != 0) {
				outStream.write(buffer, 0, n);
				n = decompresser.inflate(buffer,  0, BufferSize);
			}
			
			// next piece
			length = stream.readShort();
		}
		
		// save output stream
		outStream.flush();
	}

	static final String HEXES = "0123456789ABCDEF";
	public static String getHex(byte[] raw, int n) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * n);
		for (int i = 0; i < n; ++i) {
			final byte b = raw[i];
			hex.append(HEXES.charAt((b & 0xF0) >> 4))
			.append(HEXES.charAt((b & 0x0F))).append(" ");
			if ((i+1) % 16 == 0) hex.append("\r\n");
		}
		return hex.toString();
	}


	// uncompressed
	private void parseUncompressed() throws ParseException, ParseException, IOException {
		
		// buffer
		byte[] buffer = new byte[BufferSize];
		
		// keep going until we reach the end of the file
		int n = stream.read(buffer, 0, BufferSize);
		while (n > 0) {
			outStream.write(buffer, 0, n);
			n = stream.read(buffer, 0, BufferSize);
		}
		
		// save output stream
		outStream.flush();
	}

}
