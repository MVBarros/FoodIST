package pt.ulisboa.tecnico.cmu.server;

import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.cmu.protocol.DummyArray;
import pt.ulisboa.tecnico.cmu.protocol.DummySend;
import pt.ulisboa.tecnico.cmu.protocol.DummySummary;
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
    
    @Override
    public void dummy(DummySend dummy, StreamObserver<DummySummary> responseObserver) {
    	
    	System.out.println("Received dummy string from client:  received from client: " + dummy.getDummyString());    	
        
    	List<DummyArray> dummyArrayList = dummy.getArrayContentList();
    	
    	System.out.println("This is the information received in the DummyArrayList:");
    	for(int i = 0; i<dummyArrayList.size(); i++) {
    		DummyArray dummyArrayItem = dummyArrayList.get(i);
    		String dummyItemContent = dummyArrayItem.getDummyContent();    		
    		System.out.println("- " + dummyItemContent);
    	}
        
    	DummySummary dummySummary = DummySummary.newBuilder().setSummary("OK").build(); 
        responseObserver.onNext(dummySummary);
        responseObserver.onCompleted();
    }
    
}