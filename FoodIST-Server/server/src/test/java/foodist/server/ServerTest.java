package foodist.server;

import io.grpc.BindableService;
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
import static org.mockito.Mockito.verify;

import foodist.server.grpc.contract.Contract.ListMenuRequest;
import foodist.server.service.ServiceImplementation;

@RunWith(JUnit4.class)
public class ServerTest { 

	private static final double TEST_PRICE = 1.50;
	
	private static final String TEST_FOODSERVICE = "Testbar";
	private static final String TEST_MENU = "Chourico";
	private static final String TEST_PHOTO = "photos/test/chourico.jpg";
  
	@Rule
	public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
        
	Client client;	
	final BindableService bindableService = new ServiceImplementation();

	@Before
	public void setUp() throws Exception {   
		String serverName = InProcessServerBuilder.generateName();

		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(bindableService).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);    
    	client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
    	client.addPhoto(TEST_MENU, TEST_FOODSERVICE, TEST_PHOTO);
	}

	@Test
  	public void listMenu_deliveredToServer() {
	  	ArgumentCaptor<ListMenuRequest> requestCaptor = ArgumentCaptor.forClass(ListMenuRequest.class);	
	  	client.listMenu(TEST_FOODSERVICE);   
	  	//Mockito.ver.listMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<ListMenuRequest>>any());
		//assertEquals("test name", requestCaptor.getValue().getName());
  	}
  
}