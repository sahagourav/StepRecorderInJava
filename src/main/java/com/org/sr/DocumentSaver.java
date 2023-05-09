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
	
	private static final String JPEG = ".jpeg";
	private static final String PNG = ".png";
	private static final String BMP = ".bmp";
	
	public static void saveToDocumentFile(String folderPath, String name, File[] sortedFileList) throws IOException, InvalidFormatException {
		File[] listOfImages = sortedFileList;
		XWPFDocument document = new XWPFDocument();
		FileOutputStream output = new FileOutputStream(new File(folderPath + name + ".docx"));
		Integer count = 1;
		for (int i = 0; i < listOfImages.length; i++) {
			InputStream imageData = new FileInputStream(listOfImages[i]);
			int imageType = XWPFDocument.PICTURE_TYPE_JPEG;
			String imageExtension = DocumentSaver.JPEG;
			if(listOfImages[i].getName().endsWith(DocumentSaver.PNG)) {
				imageType = XWPFDocument.PICTURE_TYPE_PNG;
				imageExtension = DocumentSaver.PNG;
			}else if(listOfImages[i].getName().endsWith(DocumentSaver.BMP)) {
				imageType = XWPFDocument.PICTURE_TYPE_BMP;
				imageExtension = DocumentSaver.BMP;
			}
			String imageFileName = listOfImages[i].getName();
			BufferedImage imageMetadata = ImageIO.read(listOfImages[i]);

			int width = imageMetadata.getWidth();// 700
			int height = imageMetadata.getHeight();// 396

			double scaling = 1.0;
			if (width > 72 * 7.8)
				scaling = (72 * 7.8) / width;

			XWPFParagraph paragraph = document.createParagraph();
			/*
			 * Borders border = Borders.NONE; paragraph.setBorderLeft(border);
			 * paragraph.setBorderRight(border);
			 */
			paragraph.setIndentationLeft(-1100);
			paragraph.setIndentationRight(-1100);
			XWPFRun run = paragraph.createRun();
			run.setText("Step " + count++ + ":");
			
			run.addPicture(imageData, imageType, imageFileName, Units.toEMU(width * scaling),
					Units.toEMU(height * scaling));
			imageData.close();
		}
		document.write(output);
		document.close();
		output.close();
	}

	public static File[] sortFiles(File folder, String name) {
		File[] listOfImages = folder
				.listFiles((dir, fileName) -> (fileName.startsWith(name) && (fileName.toLowerCase().endsWith(DocumentSaver.JPEG)
						||fileName.toLowerCase().endsWith(DocumentSaver.BMP) || fileName.toLowerCase().endsWith(DocumentSaver.PNG))));
		if(listOfImages == null || (listOfImages != null && listOfImages.length == 0)) {
			return null;
		}
		File temp = listOfImages[0];
		Integer position1, position2;
		for (int i = 0; i < listOfImages.length; i++) {
			for (int j = 1; j < (listOfImages.length - i); j++) {
				String type = DocumentSaver.JPEG;
				if(listOfImages[j - 1].getName().toLowerCase().endsWith(DocumentSaver.BMP)) {
					type = DocumentSaver.BMP;
				}else if(listOfImages[j - 1].getName().toLowerCase().endsWith(DocumentSaver.PNG)) {
					type = DocumentSaver.PNG;
				}
				position1 = Integer.parseInt(
						listOfImages[j - 1].getName().substring(listOfImages[j - 1].getName().lastIndexOf("_") + 1,
								listOfImages[j - 1].getName().indexOf(type)));
				type = DocumentSaver.JPEG;
				if(listOfImages[j].getName().toLowerCase().endsWith(DocumentSaver.BMP)) {
					type = DocumentSaver.BMP;
				}else if(listOfImages[j].getName().toLowerCase().endsWith(DocumentSaver.PNG)) {
					type = DocumentSaver.PNG;
				}
				position2 = Integer.parseInt(listOfImages[j].getName().substring(
						listOfImages[j].getName().lastIndexOf("_") + 1, listOfImages[j].getName().indexOf(type)));
				if (position1 > position2) {
					temp = listOfImages[j - 1];
					listOfImages[j - 1] = listOfImages[j];
					listOfImages[j] = temp;
				}

			}
		}
		return listOfImages;
	}
}
