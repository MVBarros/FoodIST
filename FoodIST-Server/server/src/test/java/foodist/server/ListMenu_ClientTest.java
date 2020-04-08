package foodist.server;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.ListMenuRequest;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ListMenu_ClientTest { 
	
	private static final String TEST_FOODSERVICE = "CLIENT_TEST";
  
	public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();    
	
	private final FoodISTServerServiceGrpc.FoodISTServerServiceImplBase serviceImpl =
			mock(FoodISTServerServiceGrpc.FoodISTServerServiceImplBase.class, delegatesTo(
					new FoodISTServerServiceGrpc.FoodISTServerServiceImplBase() {												
					    @Override
					    public void listMenu(Contract.ListMenuRequest request, StreamObserver<Contract.ListMenuReply> responseObserver) {
					    	ListMenuReply listMenuReply = ListMenuReply.newBuilder().build();        
					    	responseObserver.onNext(listMenuReply);
							responseObserver.onCompleted();  
					    } 
						
		}));
	Client client;	

	
	@Before
	public void setUp() throws Exception {   
		
		String serverName = InProcessServerBuilder.generateName();

		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(serviceImpl).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);    ;
	}

	@Test
  	public void ListMenu_FoodService() {
	    ArgumentCaptor<ListMenuRequest> requestCaptor = ArgumentCaptor.forClass(ListMenuRequest.class);
	    client.listMenu(TEST_FOODSERVICE);
	    verify(serviceImpl).listMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<ListMenuReply>>any());
	    assertEquals(TEST_FOODSERVICE, requestCaptor.getValue().getFoodService());       
  	}
	
}