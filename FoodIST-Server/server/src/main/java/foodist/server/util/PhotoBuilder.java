package foodist.server.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import com.google.protobuf.ByteString;

public class PhotoBuilder {
	
	public static void store(String foodService, String name, ByteString photo) {	    	    
		String path;
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("/").append(foodService).append("/");
		
		
		path = stringBuilder.toString();
	    File directory = new File(path);
	    if (!directory.exists()){
	        directory.mkdir();	        
	    }

	    try{
	        FileOutputStream out=new FileOutputStream("photos/test/chourico1.jpg");
	        System.out.println(photo.toByteArray().toString());
	        out.write(photo.toByteArray());
	        out.close();
	    }
	    catch (IOException e){
	        e.printStackTrace();
	        System.exit(-1);
	    }
	}
	
}
