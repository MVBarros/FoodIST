package foodist.server;

import com.google.protobuf.Empty;
import foodist.server.grpc.contract.Contract.AddMenuRequest;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.AdditionalAnswers.delegatesTo;

@RunWith(JUnit4.class)
public class AddMenu_ClientTest { 
		
	private static final double TEST_PRICE = 1.50;	
	private static final String TEST_FOODSERVICE = "CLIENT_TEST";
	private static final String TEST_MENU = "CLIENT_TEST";
  
	@Rule
	public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();    
	
	private final FoodISTServerServiceGrpc.FoodISTServerServiceImplBase serviceImpl =
			mock(FoodISTServerServiceGrpc.FoodISTServerServiceImplBase.class, delegatesTo(
					new FoodISTServerServiceGrpc.FoodISTServerServiceImplBase() {												
					    @Override
					    public void addMenu(Contract.AddMenuRequest request, StreamObserver<Empty> responseObserver) {					    	
						    responseObserver.onNext(null);
						    responseObserver.onCompleted();      
					    } 
						
		}));
	Client client;	

	@Before
	public void setUp() throws Exception {   		
		String serverName = InProcessServerBuilder.generateName();

		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(serviceImpl).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);        	     	
	}

	@Test
  	public void AddMenu_FoodService() {
	    ArgumentCaptor<AddMenuRequest> requestCaptor = ArgumentCaptor.forClass(AddMenuRequest.class);
	    client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
	    verify(serviceImpl).addMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<Empty>>any());
	    assertEquals(TEST_FOODSERVICE, requestCaptor.getValue().getFoodService());       
  	}
	
	@Test
  	public void AddMenu_MenuName() {
	    ArgumentCaptor<AddMenuRequest> requestCaptor = ArgumentCaptor.forClass(AddMenuRequest.class);
	    client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
	    verify(serviceImpl).addMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<Empty>>any());
	    assertEquals(TEST_MENU, requestCaptor.getValue().getName());       
  	}
	
	@Test
  	public void AddMenu_Price() {
	    ArgumentCaptor<AddMenuRequest> requestCaptor = ArgumentCaptor.forClass(AddMenuRequest.class);
	    client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
	    verify(serviceImpl).addMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<Empty>>any());
	    assertEquals(TEST_PRICE, requestCaptor.getValue().getPrice(), 0.01);       
  	}
	
}