package foodist.server.service;

import foodist.server.grpc.contract.Contract.HelloWorldReply;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceImplBase;
import foodist.server.grpc.contract.Contract.HelloWorldRequest;
import io.grpc.stub.StreamObserver;

public class ServiceImplementation extends FoodISTServerServiceImplBase {
    
    @Override
    public void helloWorld(HelloWorldRequest request, StreamObserver<HelloWorldReply> responseObserver) {
        System.out.println("Received Hello World Request");
        String message = request.getRequest();
        message.concat(" received!");
        responseObserver.onNext(HelloWorldReply.newBuilder().setReply(message).build());
        responseObserver.onCompleted();
    }
}
