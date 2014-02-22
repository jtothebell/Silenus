package com.silenistudios.silenus.raw;

import java.io.Serializable;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.xml.XMLUtility;

/**
 * A transformation matrix is a 3x3 matrix containing scaling, shearing and translation components, like this:
 * [ a b tx ]
 * [ c d ty ]
 * [ 0 0 1  ]
 * This is the typical definition of an affine transformation.
 * @author Karel
 *
 */
public class TransformationMatrix implements Serializable {
	private static final long serialVersionUID = -8782592959755072441L;

	// the scale/rotation matrix (a,b,c,d)
	double[][] fMatrix = new double[2][2];
	
	// translation x value (tx)
	double fTranslateX;
	
	// translation y value (ty)
	double fTranslateY;
	
	
	// default transformation matrix that does nothing
	public TransformationMatrix() {
		fMatrix[0][0] = 1.0; fMatrix[0][1] = 0.0;
		fMatrix[1][0] = 0.0; fMatrix[1][1] = 1.0;
		fTranslateX = 0.0;
		fTranslateY = 0.0;
	}
	
	
	// create a transformation matrix from a node
	public TransformationMatrix(XMLUtility XMLUtility, Node matrix) throws ParseException {
		
		// load translate values if they are set
		setTranslateX(XMLUtility.getDoubleAttribute(matrix, "tx", 0.0));
		setTranslateY(XMLUtility.getDoubleAttribute(matrix, "ty", 0.0));
		
		// set matrix values
		setMatrixElement(0, 0, XMLUtility.getDoubleAttribute(matrix,  "a", 1.0));
		setMatrixElement(0, 1, XMLUtility.getDoubleAttribute(matrix,  "b", 0.0));
		setMatrixElement(1, 0, XMLUtility.getDoubleAttribute(matrix,  "c", 0.0));
		setMatrixElement(1, 1, XMLUtility.getDoubleAttribute(matrix,  "d", 1.0));
	}
	
	
	// create a transformation matrix
	public TransformationMatrix(double[][] matrix, double translateX, double translateY) {
		fMatrix[0][0] = matrix[0][0];
		fMatrix[0][1] = matrix[0][1];
		fMatrix[1][0] = matrix[1][0];
		fMatrix[1][1] = matrix[1][1];
		fTranslateX = translateX;
		fTranslateY = translateY;
	}
	
	
	// create a transformation matrix from scale, rotate and translate values
	public TransformationMatrix(double translateX, double translateY, double scaleX, double scaleY, double rotation) {
		fTranslateX = translateX;
		fTranslateY = translateY;
		fMatrix[0][0] = Math.cos(rotation) * scaleX;
		fMatrix[0][1] = -Math.sin(rotation) * scaleY;
		fMatrix[1][0] = Math.sin(rotation) * scaleX;
		fMatrix[1][1] = Math.cos(rotation) * scaleY;
	}
	
	
	// get the transformed x-value for a point
	public double computeX(double x, double y) {
		//return getScaleX() * Math.cos(-getRotation()) * x - getScaleY() * Math.sin(-getRotation()) * y + fTranslateX;
		return fMatrix[0][0] * x + fMatrix[1][0] * y + fTranslateX;
	}
	
	
	// get the transformed y-value for a point
	public double computeY(double x, double y) {
		//return getScaleX() * Math.sin(-getRotation()) * x  + getScaleY() * Math.cos(-getRotation()) * y + fTranslateY;
		return fMatrix[0][1] * x + fMatrix[1][1] * y + fTranslateY;
	}
	
	
	
	// get matrix
	public double[][] getMatrix() {
		return fMatrix;
	}
	
	
	// get translate x
	public double getTranslateX() {
		return fTranslateX;
	}
	
	
	// get translate y
	public double getTranslateY() {
		return fTranslateY;
	}
	
	
	// get x scale value
	public double getScaleX() {
		
		// get the q-matrix
		double det = det();
		double q00 = fMatrix[0][0] + Math.signum(det) * fMatrix[1][1];
		double q01 = fMatrix[0][1] - Math.signum(det) * fMatrix[1][0];
		double q10 = fMatrix[1][0] - Math.signum(det) * fMatrix[0][1];
		double q11 = fMatrix[1][1] + Math.signum(det) * fMatrix[0][0];
		
		// compute the rotation matrix qs from q
		double denom = Math.sqrt(q00*q00 + q10*q10);
		q00 /= denom;
		q01 /= denom;
		q10 /= denom;
		q11 /= denom;
		
		// we invert the 
		double scaleX = q00 * fMatrix[0][0] + q10 * fMatrix[1][0];
		if (det < 0 && q00 * q11 - q10 * q01 > 0) {
			scaleX = -scaleX;
		}
		return scaleX;
		// we return the x-scale, but transposed (as the qs-matrix is transposed in the code)
		//return Math.sqrt(fMatrix[0][0]*fMatrix[0][0] + fMatrix[0][1]*fMatrix[0][1]);
	}
	
	
	// get y scale
	public double getScaleY() {
		
		// get the q-matrix
		double det = det();
		double q00 = fMatrix[0][0] + Math.signum(det) * fMatrix[1][1];
		double q01 = fMatrix[0][1] - Math.signum(det) * fMatrix[1][0];
		double q10 = fMatrix[1][0] - Math.signum(det) * fMatrix[0][1];
		double q11 = fMatrix[1][1] + Math.signum(det) * fMatrix[0][0];
		
		// compute the rotation matrix qs from q
		double denom = Math.sqrt(q00*q00 + q10*q10);
		q00 /= denom;
		q01 /= denom;
		q10 /= denom;
		q11 /= denom;
		
		// we return the x-scale, but transposed (as the qs-matrix is transposed in the code)
		double scaleY =  q01 * fMatrix[0][1] + q11 * fMatrix[1][1];
		if (det < 0 && q00 * q11 - q10 * q01 < 0) {
			scaleY = -scaleY;
		}
		return scaleY;
		//return Math.sqrt(fMatrix[1][0]*fMatrix[1][0] + fMatrix[1][1]*fMatrix[1][1]);
	}
	
	
	// determinant of the matrix
	public double det() {
		return fMatrix[0][0]*fMatrix[1][1] - fMatrix[0][1]*fMatrix[1][0];
	}
	
	
	// sign
	private double sign(double x) {
		return (Math.abs(x) < 0.0000001) ? 1 : x / Math.abs(x);
		//return x / Math.abs(x);
	}
	
	
	// get rotation
	// http://stoney.sb.org/mm/2dMatrixDecomposition.html
	public double getRotation() {
		
		// get the q-matrix
		/*double det = det();
		double q00 = fMatrix[0][0] + Math.signum(det) * fMatrix[1][1];
		double q01 = fMatrix[0][1] - Math.signum(det) * fMatrix[1][0];
		double q10 = fMatrix[1][0] - Math.signum(det) * fMatrix[0][1];
		double q11 = fMatrix[1][1] + Math.signum(det) * fMatrix[0][0];
		
		// compute the rotation matrix qs from q
		double denom = Math.sqrt(q00*q00 + q10*q10);
		q00 /= denom;
		q01 /= denom;
		q10 /= denom;
		q11 /= denom;
		
		// finally, return the real decomposed rotation
		return Math.atan2(q10, q11); // inverted from the example on the site, as ArcTan has different parameter ordering from Math.atan2
		*/
		return Math.atan2(fMatrix[1][0], fMatrix[0][0]);
	}
	
	
	// set matrix element
	public void setMatrixElement(int x, int y, double value) {
		fMatrix[x][y] = value;
	}
	
	
	// set translate x
	public void setTranslateX(double tx) {
		fTranslateX = tx;
	}
	
	
	// set translate y
	public void setTranslateY(double ty) {
		fTranslateY = ty;
	}
	
	
	// add two matrices
	public static TransformationMatrix compose(TransformationMatrix B, TransformationMatrix A) {
		TransformationMatrix C = new TransformationMatrix();
		C.fMatrix[0][0] = B.fMatrix[0][0] * A.fMatrix[0][0] + B.fMatrix[0][1] * A.fMatrix[1][0];
		C.fMatrix[0][1] = B.fMatrix[0][0] * A.fMatrix[0][1] + B.fMatrix[0][1] * A.fMatrix[1][1];
		C.fMatrix[1][0] = B.fMatrix[1][0] * A.fMatrix[0][0] + B.fMatrix[1][1] * A.fMatrix[1][0];
		C.fMatrix[1][1] = B.fMatrix[1][0] * A.fMatrix[0][1] + B.fMatrix[1][1] * A.fMatrix[1][1];
		C.fTranslateX = B.fMatrix[0][0] * A.fTranslateX + B.fMatrix[0][1] * A.fTranslateY + B.fTranslateX;
		C.fTranslateY = B.fMatrix[1][0] * A.fTranslateX + B.fMatrix[1][1] * A.fTranslateY + B.fTranslateY;
		return C;
	}
	
	
	// to string for convenience
	@Override
	public String toString() {
		return "[" + fMatrix[0][0] + " " + fMatrix[0][1] + " ; " + fMatrix[1][0] + " " + fMatrix[1][1] + "] + [" + fTranslateX + " " + fTranslateY + "]";
	}
	
	
	// is this matrix flipped?
	// TODO this computation actually works, but I still do not know how to correct scaleX, scaleY and rotation IF it is flipped
	public boolean isFlipped() {
		double r = Math.atan2(fMatrix[1][0], fMatrix[0][0]) - Math.atan2(fMatrix[1][1], fMatrix[0][1]);
		if (r < Math.PI) r += 2 * Math.PI;
		if (r > Math.PI) r -= 2 * Math.PI;
		return r > 0.0;
	}
	
	
	// clone
	@Override
	public TransformationMatrix clone() {
		return new TransformationMatrix(getMatrix(), getTranslateX(), getTranslateY());
	}
}
