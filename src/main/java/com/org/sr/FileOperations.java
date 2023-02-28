package com.org.sr;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;

public class FileOperations {

	public static ArrayList<File> deleteFolderAndContents(String folderPath){
		ArrayList<File> unDeletedFiles = new ArrayList<File>();
		File folder = new File(folderPath);
		File[] listOfImages = folder.listFiles();
		for (File image : listOfImages) {
			image.delete();
			if(image.exists()) {
				unDeletedFiles.add(image);
			}
		}
		if(unDeletedFiles.isEmpty()) {
			folder.delete();
		}
		return unDeletedFiles;
	}
    
    public static Boolean checkFileLock(String fileName) throws IOException {
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        System.out.print(fileName + " : ");
        FileChannel channel = file.getChannel();
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                System.out.println("File is locked by another process.");
                return true;
            } else {
                lock.release();
                System.out.println("File is not locked.");
                return false;
            }
        } catch (OverlappingFileLockException e) {
            // If the lock is held by another process, get the FileLock object from the exception
            FileLock lock = channel.lock();
            System.out.println("File is locked by process: " + lock.acquiredBy());
            lock.release();
        } finally {
        	channel.close();
        	file.close();
        }
        return false;
    }
    
    
    public static void deleteFileRetry(File file) {
    	while(file.exists()) {
    		try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				if(file.exists()) {
					System.out.println(e.getMessage());
					FileOperations.forceUnlock(file);
					deleteFileRetry(file);
				}
			}
    	}
    }


	private static void forceUnlock(File file) {
        try (FileChannel channel = new RandomAccessFile(file, "rw").getChannel()) {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static void getFileOwner(String file) {
		Path filePath = Paths.get(file);
		FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(filePath, FileOwnerAttributeView.class);
		UserPrincipal owner = null;
		try {
		    owner = ownerAttributeView.getOwner();
		    Date lastModified = new Date(Files.getLastModifiedTime(filePath).toMillis());

			System.out.println("File Owner: " + owner.getName());
			System.out.println("Last Modified Time: " + lastModified);
		} catch (UserPrincipalNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
