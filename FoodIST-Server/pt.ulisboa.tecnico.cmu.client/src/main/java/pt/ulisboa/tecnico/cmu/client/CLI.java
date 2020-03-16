package pt.ulisboa.tecnico.cmu.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Scanner;
import pt.ulisboa.tecnico.cmu.protocol.PingRequest;
import pt.ulisboa.tecnico.cmu.protocol.PingResponse;
import pt.ulisboa.tecnico.cmu.protocol.PingServiceGrpc;

public class CLI {
	
	private static final String PING = "ping";
	private static final String QUIT = "quit";
	
	public void execute() {
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Dummy CMU Client Command Line Interface");			   
				
		while(true) {		
			System.out.print("Enter your command to be run on the server: ");
			String command = scanner.nextLine();	   	    
	    
	    	switch(command) {
			case PING:
		        PingServiceGrpc.PingServiceBlockingStub stub = PingServiceGrpc.newBlockingStub(channel);        
		        System.out.println("Ping message sent to server!");                
		        PingResponse response = stub.ping(PingRequest.newBuilder().setPing("ping").build());        
		        System.out.println("Response received from server: " + response.getPong());		        
				break;
			case QUIT:
				channel.shutdown();
				return;
			default:
				System.out.println("Error! The input command \"" + command + "does not exist!");
	    	}
	    }					
	}
}
