package foodist.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import foodist.server.grpc.contract.Contract.FoodType;
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
        
	final BindableService bindableService = new ServiceImplementation(true);
	
	Client client;	
	FoodType type;	
	
	@Before
	public void setUp() throws Exception {   
				
		String serverName = InProcessServerBuilder.generateName();        
		
		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(bindableService).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);    
    	type = FoodType.Meat;
	}
	
	@Test
  	public void UpdateMenu_EmptyPhotos() throws IOException {
		client.addMenu("Ronny\'s", "Invisible Pack", 69.99, type, "portuguese");
				
		Menu menu = client.updateMenu("Ronny\'s", "Invisible Pack");
		
		assertTrue(menu.getPhotoIdList().isEmpty());		
  	}
	
	@Test
  	public void UpdateMenu_UploadPhotos() throws IOException {
		client.addMenu("Ronny\'s", "Special", 5.99, type, "portuguese");
				
		String[] fastfood = {"burger", "fries", "sundae"};
		
		for(int i = 0; i<fastfood.length; i++) {
			client.addPhoto("Ronny\'s", "Special", "photos/test/" + fastfood[i] + ".jpg");			
		}					
		
		Menu menu = client.updateMenu("Ronny\'s", "Special");
		
		assertFalse(menu.getPhotoIdList().isEmpty());				
  	}
	
	@Test
  	public void UpdateMenu_DonwloadPhoto() throws IOException {
		client.addMenu("Pavano", "Pepperoni", 14.99, type, "portuguese");
		
		String[] format = {".jpg", ".png"};		
		for(int i = 0; i<format.length; i++) {
			client.addPhoto("Pavano", "Pepperoni", "photos/test/pepperoni_pizza" + format[i]);
		}
													
		Menu menu = client.updateMenu("Pavano", "Pepperoni");
		
		for(String photoId : menu.getPhotoIdList()) {
			client.downloadPhoto(photoId);
		}
		
		assertEquals(2, new File("photos/Pavano/Pepperoni/").list().length);
  	}
	
	@AfterClass
	public static void Clean() throws IOException {
		FileUtils.forceDelete(new File("photos/Ronny\'s"));
		FileUtils.forceDelete(new File("photos/Pavano"));
	}
}
