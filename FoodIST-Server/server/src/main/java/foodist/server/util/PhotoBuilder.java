package foodist.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.protobuf.ByteString;

public class PhotoBuilder {
	
	private static final String BASE_DIR = "photos";
	
	public static void store(String foodService, String name, ByteString photo) {	    	    
		String foodServicePath, photoPath;
		StringBuilder stringBuilder = new StringBuilder();
		
		System.out.println("entrou");
		stringBuilder.append(BASE_DIR).append("/").append(foodService).append("/");
				
		foodServicePath = stringBuilder.toString();
		
		System.out.println("entrou2");
		System.out.println(foodServicePath);
		System.out.println("entrou3");
	    File directory = new File(foodServicePath);
	    if (!directory.exists()){
	        directory.mkdir();	        
	    }
	    
	    photoPath = stringBuilder.append(name).toString();
	    try{
	        FileOutputStream out=new FileOutputStream(photoPath);
	        out.write(photo.toByteArray());
	        out.close();
	    }
	    catch (IOException e){
	        e.printStackTrace();
	        System.exit(-1);
	    }
	}
	
}
