package foodist.server.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.protobuf.ByteString;

import foodist.server.grpc.contract.Contract.Menu;

public class Storage {
	
	private static final String BASE_DIR = "photos";
	
	private static ConcurrentHashMap<String, HashSet<Menu>> menusHashMap = new ConcurrentHashMap<>();
	
	public synchronized static void addMenu(String foodService, Menu menu) {
	    HashSet<Menu> menuSet = menusHashMap.get(foodService);
	      
	    if(menuSet!=null) {
	    	menuSet.add(menu);
	    	menusHashMap.put(foodService, menuSet);         
	    } 
	    else {
	    	HashSet<Menu> new_MenuSet = new HashSet<>();
	    	new_MenuSet.add(menu);
	    	menusHashMap.put(foodService, new_MenuSet);         
	    } 
	}
	
	public synchronized static HashSet<Menu> getMenuSet(String foodService) {
		return menusHashMap.putIfAbsent(foodService, new HashSet<>());
	}
	
	public synchronized static void purge() {	
		System.out.print("Cleaning up server persistent memory... ");
		Iterator<Entry<String, HashSet<Menu>>> iterator = menusHashMap.entrySet().iterator();
		while (iterator.hasNext()) {
	        Map.Entry<String, HashSet<Menu>> pair = iterator.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	        menusHashMap.put(pair.getKey(), null);
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	        iterator.remove(); 
	    }
		File directory = new File(BASE_DIR);
		
		for(String filename : directory.list()) {
			if(filename.equals("test")) {
				continue;
			}
			else {
				try {
					FileUtils.forceDelete(new File(BASE_DIR + "/" + filename));
				} catch (IOException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		System.out.println("Sucess!");
	}
	
	public synchronized static void addPhotoToMenu(String photoName, String foodServiceName, String menuName, ByteString photoByteString) {	    	    		
		
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
	
	public synchronized static Menu fetchMenuPhotos(String foodServiceName, String menuName, double menu_price) {
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
	
	public synchronized static byte[] fetchPhotoBytes(String photoId, String foodServiceName, String menuName) {
		String foodServicePath = getFoodServiceDir(foodServiceName, menuName);
		
		File file = new File(foodServicePath + photoId);		
		
		if (file.exists()){
			try {
				InputStream inputStream = FileUtils.openInputStream(file);
		    	byte[] bytes = IOUtils.toByteArray(inputStream);
		    	return bytes;
			} catch(IOException ioe) {
				System.out.println(ioe.getMessage());
			}						
	    }			
		return null;
	}	
	
	public synchronized static void createPhotoDir(String photoPath) {			
		File directory = new File(photoPath);
	    if (!directory.exists()){
	        directory.mkdirs();	        
	    }	
	}
	
	public synchronized static String getFileFromPath(String path) {
		String[] split_path = path.split("/");
		int position = split_path.length - 1;
		return split_path[position];
	}
	
	private synchronized static String getFoodServiceDir(String foodServiceName, String menuName) {
		StringBuilder buildPath = new StringBuilder();		
		buildPath.append(BASE_DIR).append("/").append(foodServiceName).append("/").append(menuName).append("/");
		return buildPath.toString();
	}			
}
