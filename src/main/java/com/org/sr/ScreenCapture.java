package com.org.sr;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ScreenCapture {

	public static void captureImage(String folderPath, String name, Long index) throws IOException, AWTException {
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		Robot rt = new Robot();
		BufferedImage image = rt
				.createScreenCapture(new Rectangle((int) screensize.getWidth(), (int) screensize.getHeight()));
		File folder = new File(folderPath);
		if (folder.exists() && folder.isDirectory()) {
			ImageIO.write(image, "jpeg", new File(folderPath + name + "_" + index + ".jpeg"));
			System.out.println("Screen captured " + (index+1));
		} else {
			folder.mkdir();
			ImageIO.write(image, "jpeg", new File(folderPath + name + "_" + index + ".jpeg"));
			System.out.println("Folder created and Screen captured " + (index+1));
		}
		folder.setWritable(true);
	}
}
