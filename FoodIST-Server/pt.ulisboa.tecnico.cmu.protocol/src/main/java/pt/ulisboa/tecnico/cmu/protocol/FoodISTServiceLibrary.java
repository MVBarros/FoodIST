package pt.ulisboa.tecnico.cmu.protocol;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.cmu.protocol.FoodISTServiceGrpc.FoodISTServiceImplBase;

public abstract class FoodISTServiceLibrary extends FoodISTServiceImplBase {
	
    @Override
    public abstract void ping(PingRequest request, StreamObserver<PingResponse> responseObserver);
    
    @Override
    public abstract void dummy(DummySend dummy, StreamObserver<DummySummary> responseObserver);
}