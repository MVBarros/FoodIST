package pt.ulisboa.tecnico.cmu.protocol;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.cmu.protocol.PingServiceGrpc.PingServiceImplBase;

public class PingServiceImpl extends PingServiceImplBase {
	
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {    	
        System.out.println("Request received from client: " + request.getPing());

        PingResponse response = PingResponse.newBuilder().setPong("pong").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}