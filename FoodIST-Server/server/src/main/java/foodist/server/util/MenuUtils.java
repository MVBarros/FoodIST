package foodist.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.protobuf.ByteString;

import foodist.server.grpc.contract.Contract.Menu;

public class MenuUtils {
	
	private static final String BASE_DIR = "photos";
	
	public static void addPhotoToMenu(String foodServiceName, String menuName, ByteString photoByteString, HashMap<String, List<Menu>> menusHashMap) {	    	    		
		String photoName, foodServicePath, photoPath;
		
		StringBuilder buildPath = new StringBuilder();		
		buildPath.append(BASE_DIR).append("/").append(foodServiceName).append("/").append(menuName).append("/");				
		foodServicePath = buildPath.toString();
		
	    createPhotoDir(foodServicePath);
	    photoName = UUID.randomUUID().toString();
	    photoPath = buildPath.append(photoName).toString();
	    try{
	        FileOutputStream out=new FileOutputStream(photoPath);	        
	        out.write(photoByteString.toByteArray());
	        out.close(); 
	    }
	    catch (IOException ioe){
	        System.out.println("Error! Could not write file: \"" + photoPath + "\".");
	    }
	}

	private static void createPhotoDir(String photoPath) {		
		File directory = new File(photoPath);
	    if (!directory.exists()){
	        directory.mkdir();	        
	    }	
	}
	
}
