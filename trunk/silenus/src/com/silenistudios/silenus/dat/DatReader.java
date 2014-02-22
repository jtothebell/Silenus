package com.silenistudios.silenus.dat;

import com.silenistudios.silenus.ParseException;

/**
 * A DatReader takes a .dat file for a particular extension, and reconstructs the original image
 * so that it can be used by an external source.
 * @author Karel
 *
 */
public interface DatReader {
	
	// parse a .dat file and convert it to the original format
	public void parse(String inputFileName, String outputFileName) throws ParseException;
}
