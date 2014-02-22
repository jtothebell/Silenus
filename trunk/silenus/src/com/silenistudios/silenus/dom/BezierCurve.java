package com.silenistudios.silenus.dom;

import com.silenistudios.silenus.ParseException;

/**
 * This class generates cubic bezier curves for animation ease.
 * Two methods are supported: simple ease (with an acceleration value) and
 * custom ease (defined by points).
 * @author Karel
 *
 */
public class BezierCurve {
	
	// the points
	double[] fX;
	double[] fY;
	
	// create a custom ease
	public BezierCurve(double[] x, double[] y) throws ParseException {
		
		// check number of points
		if (x.length != y.length) throw new ParseException("Failed to load custom ease: invalid x/y data provided");
		if ((x.length-1) % 3 != 0) throw new ParseException("Failed to load custom ease: invalid number of points");
		
		// ok
		fX = x;
		fY = y;
	}
	
	
	// create a simple ease
	public BezierCurve(int acceleration) throws ParseException {
		
		// TODO these are visual estimations derived from the flash graph. You cannot read the exact
		// coordinate numbers from the graph so it is impossible to interpolate the true curve,
		// as the separate points are not stored anywhere.
		// This could probably be fixed by moving one point by a minor amount (convert to custom ease)
		// and then reading the data from the DOMDocument, but this estimation should suffice.
		
		// first, we calculate t from the acceleration
		double t = Math.abs(acceleration) / 100.0;
		
		// negative acceleration from -100 to 0
		double x1, y1, x2, y2;
		if (acceleration < 0) {
			double theta1 = 45 * (1 - t); // from 0° to 45°
			double theta2 = 247.5 + (225 - 247.5) * (1 - t); // from 247.5° to 225°
			x1 = 0.3333 * Math.cos(theta1 * Math.PI / 180); // approximate length is 0.3333
			y1 = 0.3333 * Math.sin(theta1 * Math.PI / 180); // approximate length is sqrt(2) / 2
			x2 = 1.0 + 0.7452 * Math.cos(theta2 * Math.PI / 180); // placed in top right
			y2 = 1.0 + 0.7452 * Math.sin(theta2 * Math.PI / 180);
		}
		
		// positive acceleration from 0 to 100
		else {
			double theta1 = 45 + (67.5 - 45) * t; // from 45° to 67.5°
			double theta2 = 225 + (180 - 225) * t; // from 225° to 180°
			x1 = 0.7452 * Math.cos(theta1 * Math.PI / 180);
			y1 = 0.7452 * Math.sin(theta1 * Math.PI / 180);
			x2 = 1.0 + 0.3333 * Math.cos(theta2 * Math.PI / 180); // placed in top right
			y2 = 1.0 + 0.3333 * Math.sin(theta2 * Math.PI / 180);
		}
		
		// create the matrix
		fX = new double[4];
		fY = new double[4];
		fX[0] = 0; fY[0] = 0;
		fX[1] = x1; fY[1] = y1;
		fX[2] = x2; fY[2] = y2;
		fX[3] = 1; fY[3] = 1;
	}
	
	
	// use newton to compute the t-value for a given x
	// More info: http://st-on-it.blogspot.com/2011/05/calculating-cubic-bezier-function.html
	private double computeT(double x, int i) {
		
		// the four points
		double x0 = fX[i];
		double x1 = fX[i+1];
		double x2 = fX[i+2];
		double x3 = fX[i+3];
		
		// compute the parameters
		double cx = 3 * (x1 - x0);
		double bx = 3 * (x2 - x1) - cx;
		double ax = x3 - x0 - cx - bx;
		
		// use 5 iterations of Newton
		double t = x;
		for (int k = 0; k < 5; ++k) {
			double f = ax*t*t*t + bx*t*t + cx*t + x0 - x;
			double df = 3*ax*t*t + 2*bx*t + cx;
			t = t - (f / df);
		}
		
		return t;
	}
	
	
	// interpolate y from an x value between [0,1]
	// because the points are not distributed evenly over the curve,
	// we first need to calculate the t-parameter, and then compute x from there.
	// follows http://www.moshplant.com/direct-or/bezier/math.html
	public double interpolate(double x) {
		
		// find the points we have to interpolate between
		int i = 0;
		while (fX[i+3] < x) ++i;
		
		// the four points
		double y0 = fY[i];
		double y1 = fY[i+1];
		double y2 = fY[i+2];
		double y3 = fY[i+3];
		
		// compute the parameters
		double cy = 3 * (y1 - y0);
		double by = 3 * (y2 - y1) - cy;
		double ay = y3 - y0 - cy - by;
		
		// compute t
		double t = computeT(x, i);
		
		// get the result
		return ay * t * t * t + by * t * t + cy * t + y0;
	}
	
	
	// interpolate x
	/*public double interpolate(double t) {
		
		// find the points we have to interpolate between
		int i = 0;
		while (fX[i+3] < t) ++i;
		
		// the four points
		double x0 = fX[i];
		double x1 = fX[i+1];
		double x2 = fX[i+2];
		double x3 = fX[i+3];
		
		// compute the parameters
		double cx = 3 * (x1 - x0);
		double bx = 3 * (x2 - x1) - cx;
		double ax = x3 - x0 - cx - bx;
		
		// get the result
		return ax * t * t * t + bx * t * t + cx * t + x0;
	}*/
}
