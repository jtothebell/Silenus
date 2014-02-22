package com.silenistudios.silenus.dom.fillstyles;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silenistudios.silenus.ShapeRenderInterface;


/**
 * A simple color object. Doubles as Paint, so that it can "render" itself using the ShapeRenderInterface.
 * @author Karel
 *
 */
public class Color implements Paint {
	
	// red
	int fRed = 0;
	
	// green
	int fGreen = 0;
	
	// blue
	int fBlue = 0;
	
	// alpha
	double fAlpha = 1.0;
	
	
	/**
	 * Default constructor.
	 */
	public Color() {
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param red red channel [0-255]
	 * @param green green channel [0-255]
	 * @param blue blue channel [0-255]
	 * @param alpha alpha channel [0.0, 1.0]
	 */
	public Color(int red, int green, int blue, double alpha) {
		fRed = red;
		fGreen = green;
		fBlue = blue;
		fAlpha = alpha;
	}
	
	
	/**
	 * Construct a web-safe color. The color code is represented in the form
	 * "#RRGGBB" which represents an RGB color using hexadecimal numbers.
	 * 
	 * @param red red channel [0-255]
	 * @param green green channel [0-255]
	 * @param blue blue channel [0-255]
	 */
	public Color(int red, int green, int blue) {
		fRed = red;
		fGreen = green;
		fBlue = blue;
		fAlpha = 1.0;
	}
	
	
	/**
	 * Gets the value of the red channel.
	 * 
	 * @return a value between 0 to 255, inclusive.
	 */
	public final int getRed() {
		return fRed;
	}
	
	/**
	 * Gets the value of the green channel.
	 * 
	 * @return a value between 0 to 255, inclusive.
	 */
	public final int getGreen() {
		return fGreen;
	}
	
	/**
	 * Gets the value of the blue channel.
	 * 
	 * @return a value between 0 to 255, inclusive.
	 */
	public final int getBlue() {
		return fBlue;
	}
	
	/**
	 * Gets the value of the alpha channel.
	 * 
	 * @return a value between 0.0 to 1.0, inclusive.
	 */
	public final double getAlpha() {
		return fAlpha;
	}
	
	
	/**
	 * Sets the alpha value.
	 */
	public void setAlpha(double alpha) {
		fAlpha = alpha;
	}
	
	
	// regular expressions for parsing a color
	private static Pattern PatternRGBA = Pattern.compile("rgba\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]*\\.?[0-9]+)\\s*\\)"); // rgba
	private static Pattern PatternRGB = Pattern.compile("rgb\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)"); // rgb
	private static Pattern PatternHex = Pattern.compile("\\#([0-9a-fA-F][0-9a-fA-F])([0-9a-fA-F][0-9a-fA-F])([0-9a-fA-F][0-9a-fA-F])"); // html color code #XXXXXX	
	
	
	
	/**
	 * Parse a color from a color code.
	 */
	public static final Color parseColor(String s) {
		
		// rgba pattern
		Matcher res = PatternRGBA.matcher(s);
		if (res.matches() && res.groupCount() == 4) return new Color(Integer.parseInt(res.group(1)), Integer.parseInt(res.group(2)), Integer.parseInt(res.group(3)), Double.parseDouble(res.group(4)));
		
		// rgb pattern
		res = PatternRGB.matcher(s);
		if (res.matches() && res.groupCount() == 3) return new Color(Integer.parseInt(res.group(1)), Integer.parseInt(res.group(2)), Integer.parseInt(res.group(3)));
		
		// hex pattern
		res = PatternHex.matcher(s);
		if (res.matches() && res.groupCount() == 3) return new Color(Integer.parseInt(res.group(1), 16), Integer.parseInt(res.group(2), 16), Integer.parseInt(res.group(3), 16));
		
		// no match
		return null;
	}
	
	
	/**
	 * Convert to string for printing.
	 */
	@Override
	public String toString() {
		return new StringBuilder(21)
		.append("rgba(")
		.append(fRed).append(',')
		.append(fGreen).append(',')
		.append(fBlue).append(',')
		.append(fAlpha).append(')')
		.toString();
	}
	
	
	/**
	 * Get JSON string of this color
	 */
	@Override
	public String getJSON() {
		StringBuilder ss = new StringBuilder();
		ss.append("{");
		ss.append("\"type\":\"solidColor\",");
		ss.append("\"red\":").append(fRed).append(",");
		ss.append("\"green\":").append(fGreen).append(",");
		ss.append("\"blue\":").append(fBlue).append(",");
		ss.append("\"alpha\":").append(fAlpha);
		ss.append("}");
		return ss.toString();
	}
	
	
	// fill with a basic color
	@Override
	public void render(ShapeRenderInterface renderer) {
		renderer.fillSolidColor(this);
	}
}
