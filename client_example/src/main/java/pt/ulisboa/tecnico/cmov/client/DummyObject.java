package pt.ulisboa.tecnico.cmov.client;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

public class DummyObject {
	
	private String dummyString = "dummy"; 	
	private ArrayList<String> dummyArrayList = new ArrayList<String>();
		
	public String getDummyString() {
		return this.dummyString;
	}
	
	public void setDummyString(String dummyString) {
		this.dummyString = dummyString;
	}
		
	public ArrayList<String> getDummyArrayList() {
		return this.dummyArrayList;
	}
	
	public void setDummyArrayList() {
		int min_int = 8;
		int max_int = 128;
		Random random = new Random();		
		
	    int random_num = random.nextInt((max_int - min_int) + 1) + min_int;

	    for(int i = 0; i<random_num; i++) {
	    	byte[] array = new byte[8];
		    new Random().nextBytes(array);
		    String generatedString = new String(array, Charset.forName("UTF-8"));
		    dummyArrayList.add(generatedString);		    
	    }
	}
}
