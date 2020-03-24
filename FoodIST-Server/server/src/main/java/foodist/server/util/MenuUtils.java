package foodist.server.util;

import com.google.protobuf.ByteString;
import foodist.server.grpc.contract.Contract.Menu;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MenuUtils {
	
	private static final String BASE_DIR = "photos";
	
	public static void addPhotoToMenu(String foodServiceName, String menuName, ByteString photoByteString) {	    	    		
			
		String foodServicePath = getFoodServiceDir(foodServiceName, menuName);
	    createPhotoDir(foodServicePath);
	    
	    String photoPath = foodServicePath + UUID.randomUUID().toString();
	    try{
	        FileOutputStream out=new FileOutputStream(photoPath);	        
	        out.write(photoByteString.toByteArray());
	        out.close(); 
	    }
	    catch (IOException ioe){
	        System.out.println("Error! Could not write file: \"" + photoPath + "\".");
	    }
	}
	
	public static Menu fetchMenuPhotos(String foodServiceName, String menuName, double menu_price) {
		String foodServicePath = getFoodServiceDir(foodServiceName, menuName);
		
		System.out.println("fecth menu " + foodServicePath);
		File directory = new File(foodServicePath);
		
		Menu.Builder menuBuilder = Menu.newBuilder();
		menuBuilder.setName(menuName);
		menuBuilder.setPrice(menu_price);
				
	    if (directory.exists()){
	    	    
	        for(String filename : directory.list()) {
	        	System.out.println(filename);
	        	menuBuilder.addPhotoId(filename);	        	
	        }	        
	    }	
	    
	    return menuBuilder.build();
	}

	private static void createPhotoDir(String photoPath) {		
		File directory = new File(photoPath);
	    if (!directory.exists()){
	        directory.mkdir();	        
	    }	
	}
	
	private static String getFoodServiceDir(String foodServiceName, String menuName) {
		StringBuilder buildPath = new StringBuilder();		
		buildPath.append(BASE_DIR).append("/").append(foodServiceName).append("/").append(menuName).append("/");
		return buildPath.toString();
	}
}
