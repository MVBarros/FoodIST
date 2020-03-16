package pt.ulisboa.tecnico.cmu.server;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.ulisboa.tecnico.cmu.protocol.*;

public class GrpcImpl {
	
	public void execute() {
		Server server = ServerBuilder.forPort(8080).addService(new PingServiceImpl()).build();
		
        System.out.println("Starting server...");
        
        try {
        	
			server.start();
			System.out.println("Server started!");
		    server.awaitTermination();
		    
		} catch (IOException ioe) {
			System.out.println("\"GrpcImpl.execute\" - Found an exception while executing the "
					+ "\"server.start\" method: " + ioe.getMessage());
		} catch (InterruptedException ie) {
			System.out.println("\"GrpcImpl.execute\" - Found an exception while executing the "
					+ "\"server.awaitTermination\" method: " + ie.getMessage());
		}
        
	}
}
