package foodist.server.common;

import com.google.protobuf.ByteString;
import foodist.server.grpc.contract.Contract.Menu;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Utils {
	
	private static final String BASE_DIR = "photos";
	
	public static void addPhotoToMenu(String photoName, String foodServiceName, String menuName, ByteString photoByteString) {	    	    		
			
		String foodServicePath = getFoodServiceDir(foodServiceName, menuName);
		createPhotoDir(foodServicePath);
	    String photoPath = foodServicePath + UUID.randomUUID().toString() + "." + photoName.split("\\.")[1];
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
		
		File directory = new File(foodServicePath);
		
		Menu.Builder menuBuilder = Menu.newBuilder();
		menuBuilder.setName(menuName);
		menuBuilder.setPrice(menu_price);
				
	    if (directory.exists()){
	    	    
	        for(String filename : directory.list()) {
	        	menuBuilder.addPhotoId(filename);	        	
	        }	        
	    }	
	    
	    return menuBuilder.build();
	}
	
	public static byte[] fetchPhotoBytes(String photoId, String foodServiceName, String menuName) {
		String foodServicePath = getFoodServiceDir(foodServiceName, menuName);
		
		File file = new File(foodServicePath + photoId);		
		
		if (file.exists()){
			try {
				InputStream inputStream = FileUtils.openInputStream(file);
		    	byte[] bytes = IOUtils.toByteArray(inputStream);
		    	return bytes;
			} catch(IOException ioe) {
				System.out.println("Error! Could not open file: \"" + file + "\".");
				//TODO alterar e lancar excepcao personalizada em vez de retornar nulo
			}						
	    }			
		return null;
	}	
	
	public static void createPhotoDir(String photoPath) {			
		File directory = new File(photoPath);
	    if (!directory.exists()){
	        directory.mkdirs();	        
	    }	
	}
	
	public static String getFileFromPath(String path) {
		String[] split_path = path.split("/");
		int position = split_path.length - 1;
		return split_path[position];
	}
	
	private static String getFoodServiceDir(String foodServiceName, String menuName) {
		StringBuilder buildPath = new StringBuilder();		
		buildPath.append(BASE_DIR).append("/").append(foodServiceName).append("/").append(menuName).append("/");
		return buildPath.toString();
	}			
	
	public static void deleteMenuDirectories(File directory, int iteration) {
		try {			
			for(File file : directory.listFiles()) {
				if(file.isFile()) {
					FileUtils.forceDelete(new File(file.getParent() + File.separator + file.getName()));		
				}
				if(file.isDirectory()) {
					iteration++;
					deleteMenuDirectories(file, iteration);
				}
			}				
			if(iteration==1) {
				directory.delete();
			}
		} catch(IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
	public static void deleteMenuDirectory(File directory) {
	    File[] files = directory.listFiles();
	    
	    if(files!=null) {	       
	    	for(File file: files) {	        	
	            if(file.isDirectory()) {
	            	deleteMenuDirectory(file);
	            } else {
	            	file.delete();
	            }
	        }
	    }
	    
	    directory.delete();
	}
}
