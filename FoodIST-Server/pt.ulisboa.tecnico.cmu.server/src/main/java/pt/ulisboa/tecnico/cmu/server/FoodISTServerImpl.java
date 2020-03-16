package pt.ulisboa.tecnico.cmu.server;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.cmu.protocol.FoodISTServiceLibrary;
import pt.ulisboa.tecnico.cmu.protocol.PingRequest;
import pt.ulisboa.tecnico.cmu.protocol.PingResponse;

public class FoodISTServerImpl extends FoodISTServiceLibrary {
	
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {    	
        System.out.println("Request received from client: " + request.getPing());

        PingResponse response = PingResponse.newBuilder().setPong("pong").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
}