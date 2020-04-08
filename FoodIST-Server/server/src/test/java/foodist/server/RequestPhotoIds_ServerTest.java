package foodist.server;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import org.junit.Before;
import org.junit.Test;

import foodist.server.service.ServiceImplementation;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class RequestPhotoIds_ServerTest {
	
	public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();    
	
	Client client;	
	final BindableService bindableService = new ServiceImplementation();	
	
	public BufferedImage originalPhoto;
	public DataBuffer originalPhotoDataBuffer;
	public int originalPhotoSize;
	
	@Before
	public void setUp() throws Exception {   				
		String serverName = InProcessServerBuilder.generateName();

		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(bindableService).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);
	}

	@Test
  	public void RequestPhoto_Test() {
		// TODO 
		client.addMenu("Mercado", "Nabo", 1.00);
				
		for(int i = 1; i<=4; i++) {
			client.addPhoto("Mercado", "Nabo", "photos/test/nabo_0" + i + ".png");
		}			
		
		client.requestPhotoIds();
		
		assertTrue(true);
  	}
	
}
