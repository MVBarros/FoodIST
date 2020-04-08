package foodist.server;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class AddPhoto_ServerTest { 		
  
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
  	public void AddPhoto_PhotoPathCreated() {
		client.addMenu("Portuguesa", "Chourico", 6.50);
		client.addPhoto("Portuguesa", "Chourico", "photos/test/chourico.jpg");
		
		boolean path_exists = new File("photos/Portuguesa/Chourico").exists();
		assertTrue(path_exists);
  	}
	
	@Test
  	public void AddPhoto_SamePhoto() {
    	client.addMenu("Ristorante", "Pasta", 6.50);
    	client.addPhoto("Ristorante", "Pasta", "photos/test/pasta.jpg");           	    	
        
		String photoId = new File("photos/Ristorante/Pasta").list()[0];
		
		try {
			originalPhoto = ImageIO.read(new File("photos/test/pasta.jpg"));
	        originalPhotoDataBuffer = originalPhoto.getData().getDataBuffer();
	        originalPhotoSize = originalPhotoDataBuffer.getSize(); 
	        
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
	
}