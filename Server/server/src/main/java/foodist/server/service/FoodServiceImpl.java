package foodist.server.service;

import foodist.grpc.contract.server.*;
import foodist.grpc.contract.server.Contract.HelloWorldReply;

public class FoodServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    @Override
    public void helloWorld(Contract.HelloWorldRequest request, io.grpc.stub.StreamObserver<HelloWorldReply> responseObserver) {
        HelloWorldReply reply = HelloWorldReply.newBuilder().setReply("Reply").build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
