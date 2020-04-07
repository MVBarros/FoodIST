package foodist.server.thread;

import foodist.server.data.Storage;

public class Cleanup implements Runnable {	
	
	public Cleanup() {
	}	
	
	@Override
	public void run() {		
		Storage.purge();				
	}		
	
}
