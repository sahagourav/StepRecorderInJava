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
}
