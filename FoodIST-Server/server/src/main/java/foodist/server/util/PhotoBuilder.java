package foodist.server.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import com.google.protobuf.ByteString;

public class PhotoBuilder {
	
	public static void store(String foodService, String name) {	    	    
		String path;
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("/").append(foodService).append("/");
		
		path = stringBuilder.toString();
	    File directory = new File(path);
	    if (!directory.exists()){
	        directory.mkdir();	        
	    }

	    File file = new File(path + "/" + name);
	    try{
	        FileWriter fw = new FileWriter("photos/test/chourico.jpg");
	        BufferedWriter bw = new BufferedWriter(fw);
	        bw.write("asdasd");
	        bw.close();
	    }
	    catch (IOException e){
	        e.printStackTrace();
	        System.exit(-1);
	    }
	}
	
}
