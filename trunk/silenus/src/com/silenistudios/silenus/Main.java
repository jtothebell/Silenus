package com.silenistudios.silenus;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


import javax.swing.JFrame;

import com.silenistudios.silenus.raw.AnimationData;

/**
 * This demo will take any XFL directory from the command line, and render it to screen.
 * @author Karel
 *
 */
public class Main {

	public static void main(String[] args) throws IOException {
		
		// if no argument is provided, we show the default example
		String directoryName = "";

		if (args.length > 0){
            directoryName = args[0];
        }
		
		// parse an XFL document and render it to screen
		XFLDocument xfl = new XFLDocument();
		try {
			
			// parse document
			System.out.println("Parsing document in directory '" + directoryName + "'");
			xfl.parseXFL(directoryName);
			
			System.out.println("Used images...");
			xfl.getScene().getUsedImages();
			
			// draw document
			System.out.println("Drawing document...");
			
			/**
			 * RawJavaRenderer will compute all the locations for the different bitmaps
			 * once at the start and then just draws the different bitmaps at the computed locations
			 * at real-time. This results in higher memory consumption, faster drawing.
			 * 
			 * Use the JavaRenderer for live animation and low memory footprint.
			 */
			openInJFrame(new JavaRenderer(xfl), xfl.getWidth(), xfl.getHeight(), "Silenus demo");
		}
		catch (ParseException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
		// perform a raw data renderer and save to file
		RawDataRenderer raw = new RawDataRenderer(xfl.getScene(), xfl.getWidth(), xfl.getHeight(), xfl.getFrameRate());
		AnimationData data = raw.getAnimationData();
		String json = data.getJSON();
		try {
            String path = "D:/www/silenus-renderer/upload/data.json";
            if (args.length > 1) {
                path = args[1];
            }
			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			writer.write(json);
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// done
		System.out.println("Done!");
	}
	
	// open a simple JFrame to display the animation
	public static JFrame openInJFrame(Container content,
			int width,
			int height,
			String title) {
		JFrame frame = new JFrame(title);
		frame.setBackground(Color.white);
		content.setBackground(Color.white);
		frame.setSize(width, height + 50); // 50 for the header, which is included here
		frame.setContentPane(content);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
		return(frame);
	}
}
