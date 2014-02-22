package com.silenistudios.silenus.dat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * This class takes in RGBA data from another source (inflater, for example), and writes
 * it to a file.
 * @author Karel
 *
 */
public class PNGOutputStream extends OutputStream {
	
	// buffered image
	BufferedImage fImage;
	
	// the internal pixel array
	byte[] fPixelArray;
	
	// width, height
	int fWidth;
	int fHeight;
	
	// current x/y we're drawing to
	int fX = 0;
	int fY = 0;
	
	// bytes read (need 4 to write rgba data)
	int fBytesRead = 0;
	
	// data
	byte fRed, fGreen, fBlue, fAlpha;
	
	// output stream
	OutputStream fOut;
	
	
	// constructor
	public PNGOutputStream(OutputStream out, int width, int height) {
		fWidth = width;
		fHeight = height;
		fOut = out;
		fImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
		fPixelArray = ((DataBufferByte)fImage.getRaster().getDataBuffer()).getData();
	}

	@Override
	public void write(int b) throws IOException {
		
		// set data
		switch (fBytesRead) {
			case 0: fAlpha = (byte)b; break;
			case 1: fRed = (byte)b; break;
			case 2: fGreen = (byte)b; break;
			case 3: fBlue = (byte)b; break;
		}
		++fBytesRead;
		// flush byte if we're fully loaded
		if (fBytesRead == 4) {
			fBytesRead = 0;
			fPixelArray[fY*fWidth*4 + fX*4] = fAlpha;
			fPixelArray[fY*fWidth*4 + fX*4 + 1] = fBlue;
			fPixelArray[fY*fWidth*4 + fX*4 + 2] = fGreen;
			fPixelArray[fY*fWidth*4 + fX*4 + 3] = fRed;
			
			//fImage.setRGB(fX, fY, rgb);
			++fX;
			if (fX == fWidth) {
				fX = 0;
				++fY;
			}
		}
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
	
	
	@Override
	public void flush() throws IOException {
		super.flush();
		ImageIO.write(fImage, "png", fOut);
	}
	
	
	@Override
	public void close() throws IOException {
		super.flush();
		ImageIO.write(fImage, "png", fOut);
		fImage = null;
	}

}
