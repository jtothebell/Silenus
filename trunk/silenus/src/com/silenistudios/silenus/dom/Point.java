package com.silenistudios.silenus.dom;

import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * A simple point used in vector graphics.
 * @author Karel
 *
 */
public class Point {
	
	// regular expression for parsing a point - works with hex representation
	private static final Pattern RegExp = Pattern.compile("((?:[-]?[0-9]*[\\.]?[0-9]+)|(?:[-]?[#]?[0-9A-F]*[\\.]?[0-9A-F]+))\\s+((?:[-]?[0-9]*[\\.]?[0-9]+)|(?:[-]?[#]?[0-9A-F]*[\\.]?[0-9A-F]+))");
	
	
	// x-value in double twips (cause half-twips can appear)
	int fX;
	
	// y in double twips (cause half-twips can appear)
	int fY;
	
	// constructor - these are strings because sometimes the points are written in hexadecimal
	public Point(String xTwip, String yTwip) {
		
		// hex or int?
		if (xTwip.startsWith("#")) fX = (int)(parseHex(xTwip.substring(1)) * 2);
		else {
			try {
				fX = (int)(Double.parseDouble(xTwip) * 2);
			}
			catch (NumberFormatException e) {
				fX = (int)(parseHex(xTwip) * 2); 
			}
		}
		if (yTwip.startsWith("#")) fY = (int)(parseHex(yTwip.substring(1)) * 2);
		else {
			try {
				fY = (int)(Double.parseDouble(yTwip) * 2);
			}
			catch (NumberFormatException e) {
				fY = (int)(parseHex(yTwip) * 2);
			}
		}
	}
	
	
	// parse hex value
	private double parseHex(String twip) {
		
		// split up the integer and fraction
		String[] split = twip.split("\\.");
		String fullString = split[0];
		
		// no fraction
		if (split.length == 1) fullString += "00";
		
		// there is a fraction - make sure it is appended properly
		else {
			fullString += split[1];
			if (split[1].length() == 1) fullString += "0"; // not sure if this ever happens, but you never know with adobe
		}
		
		// use some fancy magic to convert the two's complement number to its proper integer representation
		int value = (int)Long.parseLong(fullString, 16);
		
		// finally, convert to double and divide by 256.0 to compensate for moving the fixed point to the right 2 places
		return (double)value / 256.0;
	}
	/*private double parseHex(String twip) {


		// split up the integer and fraction
		String[] split = twip.split("\\.");



		//some weird large points - not sure what to do 
		//
		//eg #FFFFA4
		//#FFD4
		double a = Long.valueOf(split[0],16).longValue();
		        double b = Long.valueOf(split[1],16).longValue();	
		if (a > 0xFFFF)
		a = -(0xFFFF00 - a);
		if (b > 0xFFFF)
		b = -(0xFFFF00 - b);


		double c = a + b / 100.0;



		// no fraction
		if (split.length == 1) return Integer.parseInt(split[0], 16);
		// fraction
		//else return Double.parseDouble(Integer.parseInt(split[0], 16) + "." + Integer.parseInt(split[1], 16));
		return c;
		}*/
	
	
	// get the reg exp for parsing a point
	public static String getRegExp() {
		return RegExp.pattern();
	}
	
	
	// get the regular expression in compiled version
	public static Pattern getRegExpCompiled() {
		return RegExp;
	}
	
	
	// get x
	public double getX() {
		return fX / 20.0 / 2.0;
	}
	
	
	// get y
	public double getY() {
		return fY / 20.0 / 2.0;
	}
	
	
	// get twip x
	public int getTwipX() {
		return fX;
	}
	
	
	// get twip y
	public int getTwipY() {
		return fY;
	}
	
	
	// equals
	public boolean equals(Point p) {
		//return Math.abs(fX - p.fX) < 0.0001 && Math.abs(fY - p.fY) < 0.0001;  
		return fX == p.fX && fY == p.fY;
	}
	
	
	// to string
	@Override
	public String toString() {
		return new StringBuilder().append("[").append(getX()).append(",").append(getY()).append("]").toString();
	}
	
	
	// just forward to toString
	public String getJSON() {
		return toString();
	}
}
