package foodist.server.thread;

import foodist.server.service.ServiceImplementation;

public class Cleanup implements Runnable {

	private final ServiceImplementation impl;

	public Cleanup(ServiceImplementation impl) {
		this.impl = impl;
	}	
	
	@Override
	public void run() {
		impl.cleanup();
	}		
	
}
