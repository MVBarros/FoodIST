package foodist.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Test;

import foodist.server.service.ServiceImplementation;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class UpdateMenu_ServerTest {
	
	static final BindableService bindableService = new ServiceImplementation();
	static final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();    	
	static Client client;		
	
	@BeforeClass
	public static void setUp() throws Exception {   				
		String serverName = InProcessServerBuilder.generateName();

		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(bindableService).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);
	}
	
	@Test
  	public void updateMenu_Test() throws IOException {
		this.client.addMenu("Mercado", "Alho", 0.49);
		this.client.addPhoto("Mercado", "Alho", "photos/test/alho.jpg");
		this.client.updateMenu("Mercado", "Alho");
  	}  	
	
}
