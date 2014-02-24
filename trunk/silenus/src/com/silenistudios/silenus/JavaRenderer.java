package com.silenistudios.silenus;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.silenistudios.silenus.dom.Bitmap;
import com.silenistudios.silenus.dom.BitmapInstance;
import com.silenistudios.silenus.dom.ShapeInstance;
import com.silenistudios.silenus.dom.StrokeStyle;
import com.silenistudios.silenus.dom.Timeline;
import com.silenistudios.silenus.dom.fillstyles.Color;
import com.silenistudios.silenus.dom.fillstyles.ColorStop;
import com.silenistudios.silenus.raw.ColorManipulation;

/**
 * Example renderer for Java. Implements the RenderInterface, allowing it to render
 * any scene sent to it.
 * @author Karel
 *
 */
public class JavaRenderer extends JPanel implements RenderInterface, ShapeRenderInterface {
	
	// default serial number
	private static final long serialVersionUID = 1L;

	// the renderer
	SceneRenderer fRenderer;
	
	// the drawing context
	Graphics2D fSurface;
	
	// stack of transformations
	Stack<AffineTransform> fTransformStack = new Stack<AffineTransform>();
	
	// stack of composite operations, for restoration on restore()
	Stack<Composite> fCompositeStack = new Stack<Composite>();
	
	// current frame
	int fFrame = 0;
	
	// images
	Map<String, BufferedImage> fImages = new HashMap<String, BufferedImage>();
	
	// the scene
	Timeline fScene;
	
	// general path
	GeneralPath fPath;
	
	
	// color manipulation
	ColorManipulation fCurrentColorManipulation = null;
	
	
	// constructor
	public JavaRenderer(XFLDocument doc) {
		
		// get the scene
		fScene = doc.getScene();
		
		// set the renderer
		fRenderer = new SceneRenderer(fScene, this);
		
		// load all images
		Set<Bitmap> bitmaps = fScene.getUsedImages();
		for (Bitmap bitmap : bitmaps) {
			BufferedImage img;
			try {
				img = ImageIO.read(new File(bitmap.getAbsolutePath()));
				fImages.put(bitmap.getAbsolutePath(), img);
			} catch (IOException e) {
                e.printStackTrace();
			}
		}

		// get frame rate
		int frameRate = doc.getFrameRate();
		
		// launch a timer to draw the animation
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				repaint();
				++fFrame;
			}
			
		}, 0, 1000 / frameRate);
	}
	
	
	// draw
	@Override
	public void paintComponent(Graphics g) {
		clear(g);
		fSurface = (Graphics2D)g;
		fSurface.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		if (fScene.getMaxFrameIndex() == 0) fRenderer.render(0);
		else fRenderer.render(fFrame % fScene.getMaxFrameIndex());
	}
	
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}


	@Override
	public void save() {
		fTransformStack.push(fSurface.getTransform());
		fCompositeStack.push(fSurface.getComposite());
	}


	@Override
	public void restore() {
		fSurface.setTransform(fTransformStack.pop());
		fSurface.setComposite(fCompositeStack.pop());
	}


	@Override
	public void scale(double x, double y) {
		fSurface.scale(x,  y);
	}


	@Override
	public void translate(double x, double y) {
		fSurface.translate(x, y);
	}


	@Override
	public void rotate(double theta) {
		fSurface.rotate(theta);
	}


	@Override
	public void drawBitmapInstance(BitmapInstance instance) {
		Bitmap bitmap = instance.getBitmap();
		
		// this is a mask - just draw the outline
		if (instance.isMask()) {
			moveTo(0, 0);
			lineTo(bitmap.getWidth(), 0);
			lineTo(bitmap.getWidth(), bitmap.getHeight());
			lineTo(0, bitmap.getHeight());
			lineTo(0, 0);
			fillSolidColor(new Color(255, 0, 0));
			return;
		}
		
		// color manipulation
		if (fCurrentColorManipulation != null) {
			
			// get the original image
			BufferedImage img = fImages.get(bitmap.getAbsolutePath());
			
			// set up the rescale operation
			RescaleOp rescaleOp = new RescaleOp(new float[]{
					(float) fCurrentColorManipulation.getRedMultiplier(),
					(float) fCurrentColorManipulation.getGreenMultiplier(),
					(float) fCurrentColorManipulation.getBlueMultiplier(),
					(float) fCurrentColorManipulation.getAlphaMultiplier()},	new float[]{
					(float) fCurrentColorManipulation.getRedOffset(),
					(float) fCurrentColorManipulation.getGreenOffset(), 
					(float) fCurrentColorManipulation.getBlueOffset(), 
					(float) fCurrentColorManipulation.getAlphaOffset()}, null);
			
			// copy to a buffered image
			BufferedImage out = rescaleOp.createCompatibleDestImage(img, null);
			
			// apply filter
			rescaleOp.filter(img, out);
			
			// draw
			fSurface.drawImage(out, new AffineTransform(1f,0f,0f,1f,0,0), null);
		}
		
		// no color manipulation
		else {
			fSurface.drawImage(fImages.get(bitmap.getAbsolutePath()), new AffineTransform(1f,0f,0f,1f,0,0), null);
		}
		
		// only apply to one image
		fCurrentColorManipulation = null;
	}
	
	
	@Override
	public void drawShapeInstance(ShapeInstance shape) {
		shape.render(this);
	}
	
	
	@Override
	public void applyColorManipulation(ColorManipulation colorManipulation) {
		fCurrentColorManipulation = colorManipulation;
	}


	@Override
	public void fillSolidColor(Color color) {
		Paint paint = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * 255));
		fSurface.setPaint(paint);
		fPath.closePath();
		fSurface.fill(fPath);
		fPath = null;
	}
	
	
	@Override
	public void fillLinearGradient(double startX, double startY, double stopX, double stopY, Vector<ColorStop> colorStops) {
		float[] fractions = new float[colorStops.size()];
		java.awt.Color[] colors = new java.awt.Color[colorStops.size()];
		for (int i = 0; i < colorStops.size(); ++i) {
			fractions[i] = (float)colorStops.get(i).getRatio();
			Color color = colorStops.get(i).getColor();
			colors[i] = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * 255));
		}
		Paint paint = new LinearGradientPaint((float)startX, (float)startY, (float)stopX, (float)stopY, fractions, colors);
		fSurface.setPaint(paint);
		fPath.closePath();
		fSurface.fill(fPath);
		fPath = null;
	}

    @Override
    public void fillRadialGradient(double centerX, double centerY, double radius, Vector<ColorStop> colorStops) {
        float[] fractions = new float[colorStops.size()];
        java.awt.Color[] colors = new java.awt.Color[colorStops.size()];
        for (int i = 0; i < colorStops.size(); ++i) {
            fractions[i] = (float)colorStops.get(i).getRatio();
            Color color = colorStops.get(i).getColor();
            colors[i] = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * 255));
        }

        Paint paint = new RadialGradientPaint((float)centerX, (float)centerY, (float)radius, fractions, colors);
        fSurface.setPaint(paint);
        fPath.closePath();
        fSurface.fill(fPath);
        fPath = null;
    }


    @Override
	public void stroke(StrokeStyle strokeStyle) {
		Color color = strokeStyle.getColor();
		Paint paint = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * 255));
		fSurface.setStroke(new BasicStroke((float)strokeStyle.getWeight()));
		fSurface.setPaint(paint);
		fSurface.draw(fPath);
		fPath = null;
	}


	@Override
	public void moveTo(double x, double y) {
		if (fPath == null) fPath = new GeneralPath();
		fPath.moveTo(x,  y);
	}


	@Override
	public void lineTo(double x, double y) {
		fPath.lineTo(x,  y);
	}


	@Override
	public void quadraticCurveTo(double controlX, double controlY, double targetX, double targetY) {
		fPath.quadTo(controlX, controlY, targetX, targetY);
	}


	@Override
	public void clip() {
		fSurface.clip(fPath);
		fPath = null;
	}


	@Override
	public void resetMask() {
		fSurface.setClip(null);
	}
}
