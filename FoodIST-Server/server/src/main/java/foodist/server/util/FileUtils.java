package foodist.server.util;

import java.io.File;

public class FileUtils {

	public static String getFileFromPath(String path) {
		String[] split_path = path.split("/");
		int position = split_path.length - 1;
		return split_path[position];
	}
	
	public static void createPhotoDir(String photoPath) {			
		File directory = new File(photoPath);
	    if (!directory.exists()){
	        directory.mkdirs();	        
	    }	
	}
}
