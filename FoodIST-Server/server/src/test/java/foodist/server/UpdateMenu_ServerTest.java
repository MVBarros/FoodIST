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

import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.service.ServiceImplementation;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class UpdateMenu_ServerTest {
	

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
	}
	
	@Test
  	public void UpdateMenu_EmptyPhotos() throws IOException {
		client.addMenu("Mackies", "Invisible Pack", 69.99);
				
		Menu menu = client.updateMenu("Mackies", "Invisible Pack");
		
		assertTrue(menu.getPhotoIdList().isEmpty());		
  	}
	
	@Test
  	public void UpdateMenu_UploadPhotos() throws IOException {
		client.addMenu("Mackies", "Special", 5.99);
				
		String[] fastfood = {"burger", "fries", "sundae"};
		
		for(int i = 0; i<fastfood.length; i++) {
			client.addPhoto("Mackies", "Special", "photos/test/" + fastfood[i] + ".jpg");			
		}					
		
		Menu menu = client.updateMenu("Mackies", "Special");
		
		assertFalse(menu.getPhotoIdList().isEmpty());				
  	}
	
	@Test
  	public void UpdateMenu_DonwloadPhoto() throws IOException {
		client.addMenu("Romano", "Pepperoni", 14.99);				
		
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
