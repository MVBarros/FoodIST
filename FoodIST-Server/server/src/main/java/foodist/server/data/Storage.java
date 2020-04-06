package foodist.server.data;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;

import foodist.server.grpc.contract.Contract.Menu;

public class Storage {
	
	private static final String BASE_DIR = "photos";
	
	private static ConcurrentHashMap<String, HashSet<Menu>> menusHashMap = new ConcurrentHashMap<String, HashSet<Menu>>();
	
	public synchronized static void addMenu(String foodService, Menu menu) {
	    HashSet<Menu> menuSet = menusHashMap.get(foodService);
	      
	    if(menuSet!=null) {
	    	menuSet.add(menu);
	    	menusHashMap.put(foodService, menuSet);         
	    } 
	    else {
	    	HashSet<Menu> new_MenuSet = new HashSet<Menu>();
	    	new_MenuSet.add(menu);
	    	menusHashMap.put(foodService, new_MenuSet);         
	    } 
	}
	
	public synchronized static HashSet<Menu> getMenuSet(String foodService) {
		return menusHashMap.get(foodService);
	}
	
	public synchronized static void purge() {	
		System.out.print("Cleaning up server persistent memory... ");
		Iterator<Entry<String, HashSet<Menu>>> iterator = menusHashMap.entrySet().iterator();
		while (iterator.hasNext()) {
	        Map.Entry<String, HashSet<Menu>> pair = (Map.Entry<String, HashSet<Menu>>) iterator.next();
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
	
}
