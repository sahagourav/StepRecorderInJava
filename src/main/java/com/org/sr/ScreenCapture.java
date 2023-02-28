package com.org.sr;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

public class ScreenCapture {

	public static synchronized void captureImage(String folderPath, String name, Long index) throws IOException, AWTException {
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		Robot rt = new Robot();
		BufferedImage image = rt
				.createScreenCapture(new Rectangle((int) screensize.getWidth(), (int) screensize.getHeight()));
		File folder = new File(folderPath);
		if (folder.exists() && folder.isDirectory()) {
			File file = new File(folderPath + name + "_" + index + ".jpeg");
			if(file.exists()) {
				ImageIO.write(image, "jpeg", file);
				System.out.println("Screen captured overridden " + index);
			}else {
				ImageIO.write(image, "jpeg", file);
				System.out.println("Screen captured " + index);
			}
		} else {
			folder.mkdir();
			ImageIO.write(image, "jpeg", new File(folderPath + name + "_" + index + ".jpeg"));
			System.out.println("Folder created and Screen captured " + index);
		}
		folder.setWritable(true);
	}
	
	public static synchronized void deleteImage(String folderPath, String name, Long index) throws IOException {
		File img = new File(folderPath + name + "_" + index + ".jpeg");
		if(img.exists()) {
			FileUtils.forceDelete(img);
			System.out.println("Deleted Screenshot " + index);
		}else {
			throw new IOException("File doesn't exists");
		}
	}
	
	public static HashMap<String, Integer> getTaskbarBounds(){
		Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
    	Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    	HashMap<String, Integer> taskBarCoordinates = new HashMap<String, Integer>();    	
    	if(winSize.getMinX()==0 && winSize.getMinY()==0
    			&& winSize.getMaxX()==scrnSize.width && winSize.getMaxY()<scrnSize.height) {
    		//BOTTOM
    		taskBarCoordinates.put("MinX", 0);
    		taskBarCoordinates.put("MinY", winSize.height);
    		taskBarCoordinates.put("MaxX", scrnSize.width);
    		taskBarCoordinates.put("MaxY", scrnSize.height);
    	}
    	else if(winSize.getMinX()==0 && winSize.getMinY()>0
    			&& winSize.getMaxX()==scrnSize.width && winSize.getMaxY()==scrnSize.height) {
    		//TOP
    		taskBarCoordinates.put("MinX", 0);
    		taskBarCoordinates.put("MinY", 0);
    		taskBarCoordinates.put("MaxX", scrnSize.width);
    		taskBarCoordinates.put("MaxY", (scrnSize.height - winSize.height));
    	}
    	else if(winSize.getMinX()==0 && winSize.getMinY()==0
    			&& winSize.getMaxX()<scrnSize.width && winSize.getMaxY()==scrnSize.height) {
    		//RIGHT
    		taskBarCoordinates.put("MinX", (scrnSize.width - winSize.width));
    		taskBarCoordinates.put("MinY", 0);
    		taskBarCoordinates.put("MaxX", scrnSize.width);
    		taskBarCoordinates.put("MaxY", scrnSize.height);
    	}
    	else if(winSize.getMinX()>0 && winSize.getMinY()==0
    			&& winSize.getMaxX()==scrnSize.width && winSize.getMaxY()==scrnSize.height) {
    		//LEFT
    		taskBarCoordinates.put("MinX", 0);
    		taskBarCoordinates.put("MinY", 0);
    		taskBarCoordinates.put("MaxX", (scrnSize.width - winSize.width));
    		taskBarCoordinates.put("MaxY", scrnSize.height);
    	}
		return taskBarCoordinates;
	}
}
