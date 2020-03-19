package pt.ulisboa.tecnico.cmov.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.Scanner;

import pt.ulisboa.tecnico.cmov.protocol.DummyArray;
import pt.ulisboa.tecnico.cmov.protocol.DummySend;
import pt.ulisboa.tecnico.cmov.protocol.DummySummary;
import pt.ulisboa.tecnico.cmov.protocol.FoodISTServiceGrpc;
import pt.ulisboa.tecnico.cmov.protocol.PingRequest;
import pt.ulisboa.tecnico.cmov.protocol.PingResponse;

public class CLI {
	
	private static final String DUMMY = "dummy";
	private static final String PING = "ping";
	private static final String QUIT = "quit";
	
	public void execute() {
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Dummy cmov Client Command Line Interface");			   
				
		while(true) {		
			System.out.print("Enter your command to be run on the server: ");
			String command = scanner.nextLine();	   	    
			
			FoodISTServiceGrpc.FoodISTServiceBlockingStub stub = FoodISTServiceGrpc.newBlockingStub(channel);
	    	switch(command) {	    			    	
		    	case DUMMY:		    		
		    		DummyObject dummy = new DummyObject();
		    		dummy.setDummyString("test");
		    		dummy.setDummyArrayList();		    			    				    		      		    			    		
		    				    				       
		    		DummySend.Builder builder = DummySend.newBuilder();
			        builder.setDummyString(dummy.getDummyString());			        
			        ArrayList<String> dummyList = dummy.getDummyArrayList();
			        			        
			        for(int i = 0; i<dummyList.size(); i++) {
			        	String dummyStringItem = dummyList.get(i);
			        	DummyArray dummyArrayItem = DummyArray.newBuilder().setDummyContent(dummyStringItem).build();
			        	builder.addArrayContent(dummyArrayItem);
			        }			        
			        System.out.println("Sending dummy object to server!");
			        DummySummary dummySummary = stub.dummy(builder.build());        
			        System.out.println("Response received from server: " + dummySummary.getSummary());		        
			        break;			        
				case PING:
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
