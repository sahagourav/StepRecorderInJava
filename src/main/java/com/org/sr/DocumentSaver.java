package com.org.sr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class DocumentSaver {
	public static void saveToDocumentFile(String folderPath, String name) throws IOException, InvalidFormatException {
		File folder = new File(folderPath);
		File[] listOfImages = DocumentSaver.sortFiles(folder.listFiles());
		XWPFDocument document = new XWPFDocument();
		FileOutputStream output = new FileOutputStream(new File(folderPath + name + ".docx"));
		Integer count = 1;
		for(int i = 0; i < listOfImages.length; i++) {
			InputStream imageData = new FileInputStream(listOfImages[i]);
			int imageType = XWPFDocument.PICTURE_TYPE_JPEG;
	        String imageFileName = listOfImages[i].getName();
	        BufferedImage imageMetadata = ImageIO.read(listOfImages[i]);
	        
	        int width = imageMetadata.getWidth();//700
	        int height = imageMetadata.getHeight();//396
	        
	        double scaling = 1.0;
	        if (width > 72*7.8) scaling = (72*7.8)/width;
	        
			XWPFParagraph paragraph = document.createParagraph();
			/*
			 * Borders border = Borders.NONE; paragraph.setBorderLeft(border);
			 * paragraph.setBorderRight(border);
			 */
			paragraph.setIndentationLeft(-1100);
			paragraph.setIndentationRight(-1100);
			XWPFRun run = paragraph.createRun();
			run.setText("Step " + count++ + ": \n");
			run.addPicture(imageData, imageType, imageFileName, Units.toEMU(width*scaling), Units.toEMU(height*scaling));
		}
	    document.write(output);
	    document.close();
	    output.close();
	}

	private static File[] sortFiles(File[] listOfImages) {
		File temp = listOfImages[0];
		for(int i=0; i < listOfImages.length; i++){
			for(int j=1; j < (listOfImages.length-i); j++){
				Integer position1 = Integer.parseInt(listOfImages[j-1].getName().substring(listOfImages[j-1].getName().indexOf("_")+1, listOfImages[j-1].getName().indexOf(".jpeg")));
				Integer position2 = Integer.parseInt(listOfImages[j].getName().substring(listOfImages[j].getName().indexOf("_")+1, listOfImages[j].getName().indexOf(".jpeg")));
				if(position1 > position2) {
					temp = listOfImages[j-1];
					listOfImages[j-1] = listOfImages[j];
					listOfImages[j] = temp;
				}

			}
		}
		return listOfImages;
	}
}
