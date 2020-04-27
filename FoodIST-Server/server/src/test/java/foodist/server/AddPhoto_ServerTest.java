package foodist.server;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.Contract.FoodType;
import foodist.server.service.ServiceImplementation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class AddPhoto_ServerTest { 		
  
	static final BindableService bindableService = new ServiceImplementation(true);
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
  	public void AddPhoto_PhotoPathCreated() {
		FoodType type = FoodType.Meat;
		client.addMenu("Portuguesa", "Chourico", 6.50, type, Contract.Language.pt);
		client.addPhoto("Portuguesa", "Chourico", "photos/test/chourico.jpg");
		
		boolean path_exists = new File("photos/Portuguesa/Chourico").exists();
		assertTrue(path_exists);
  	}
	
	@Test
  	public void AddPhoto_SamePhoto() {
		FoodType type = FoodType.Vegetarian;
    	client.addMenu("Ristorante", "Pasta", 6.50, type, Contract.Language.pt);
    	client.addPhoto("Ristorante", "Pasta", "photos/test/pasta.jpg");           	    	
        
		String photoId = new File("photos/Ristorante/Pasta").list()[0];
		
		try {
			
			BufferedImage originalPhoto = ImageIO.read(new File("photos/test/pasta.jpg"));
			DataBuffer originalPhotoDataBuffer = originalPhoto.getData().getDataBuffer();
			int originalPhotoSize = originalPhotoDataBuffer.getSize();
  
			BufferedImage uploadedPhoto = ImageIO.read(new File(
					"photos/Ristorante/Pasta" 
					+ File.separator + photoId 
					));
			
			DataBuffer uploadedPhotoDataBuffer = uploadedPhoto.getData().getDataBuffer();
		    int uploadedPhotoSize = uploadedPhotoDataBuffer.getSize();                      
		                
		    boolean different_photo = false;
		        
		    if(originalPhotoSize == uploadedPhotoSize) {
		    	for(int i=0; i<originalPhotoSize; i++) { 
		    		if(originalPhotoDataBuffer.getElem(i) != uploadedPhotoDataBuffer.getElem(i)) {
		    			different_photo = true;
		                break;
		            }
		        }            
		    }
			assertFalse(different_photo);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}	
  	}
	
	@Test
  	public void AddPhoto_DuplicatePhoto() {
		FoodType type = FoodType.Vegan;
    	client.addMenu("Mackies", "Fries", 6.50, type, Contract.Language.pt);
    	
    	for(int i=0; i<1024; i++) {
    		client.addPhoto("Mackies", "Fries", "photos/test/fries.jpg");
    	}
        
		assertEquals(1024, new File("photos/Mackies/Fries/").list().length);
  	}
	
}