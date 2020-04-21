package foodist.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import foodist.server.grpc.contract.Contract.FoodType;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.service.ServiceImplementation;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class UpdateMenu_ServerTest {
	

	@Rule
	public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
        
	final BindableService bindableService = new ServiceImplementation();
	
	Client client;	
	FoodType type;

	@Before
	public void setUp() throws Exception {   
		
		String serverName = InProcessServerBuilder.generateName();

		File priv = Security.getPrivateKey();
        File cert = Security.getPublicKey();    
        
		grpcCleanup.register(ServerBuilder.forPort(8080).useTransportSecurity(cert, priv).addService(bindableService).build());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);    
    	type = FoodType.Meat;
	}
	
	@Test
  	public void UpdateMenu_EmptyPhotos() throws IOException {
		client.addMenu("Mackies", "Invisible Pack", 69.99, type, "portuguese");
				
		Menu menu = client.updateMenu("Mackies", "Invisible Pack");
		
		assertTrue(menu.getPhotoIdList().isEmpty());		
  	}
	
	@Test
  	public void UpdateMenu_UploadPhotos() throws IOException {
		client.addMenu("Mackies", "Special", 5.99, type, "portuguese");
				
		String[] fastfood = {"burger", "fries", "sundae"};
		
		for(int i = 0; i<fastfood.length; i++) {
			client.addPhoto("Mackies", "Special", "photos/test/" + fastfood[i] + ".jpg");			
		}					
		
		Menu menu = client.updateMenu("Mackies", "Special");
		
		assertFalse(menu.getPhotoIdList().isEmpty());				
  	}
	
	@Test
  	public void UpdateMenu_DonwloadPhoto() throws IOException {
		client.addMenu("Romano", "Pepperoni", 14.99, type, "portuguese");
		
		String[] format = {".jpg", ".png"};		
		for(int i = 0; i<format.length; i++) {
			client.addPhoto("Romano", "Pepperoni", "photos/test/pepperoni_pizza" + format[i]);
		}
													
		Menu menu = client.updateMenu("Romano", "Pepperoni");
		
		for(String photoId : menu.getPhotoIdList()) {
			client.downloadPhoto(photoId);
		}
		
		assertEquals(2, new File("photos/Romano/Pepperoni/").list().length);
  	}
}
