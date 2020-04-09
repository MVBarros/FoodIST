package foodist.server;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.service.ServiceImplementation;

@RunWith(JUnit4.class)
public class DownloadPhoto_ServerTest { 		
  
	public BufferedImage originalPhoto;
	public DataBuffer originalPhotoDataBuffer;
	public int originalPhotoSize;
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
  	public void DownloadPhoto_ClientPath() {
		client.addMenu("Tugalandia", "Farinheira", 8.00);
		client.addPhoto("Tugalandia", "Farinheira", "photos/test/farinheira.png");
		
	  	String photoId = "";	
	  	ListMenuReply listMenuReply = client.listMenu("Tugalandia");
	  	
	  	photoId = listMenuReply.getMenusList().get(0).getPhotoId(0);
	  	
		boolean path_exists = new File("photos/client" + File.separator + photoId).exists();
		assertTrue(path_exists);
  	}
	
	@Test
  	public void DownloadPhoto_SamePhoto() {		
		client.addMenu("Romano", "Risotto", 7.00);
    	client.addPhoto("Romano", "Risotto", "photos/test/risotto.jpg");    		    	
        
	  	String photoId = "";	
	  	ListMenuReply listMenuReply = client.listMenu("Romano");
	  	
	  	photoId = listMenuReply.getMenusList().get(0).getPhotoId(0);
	  	
		client.downloadPhoto(photoId);

		try {
			originalPhoto = ImageIO.read(new File("photos/test/risotto.jpg"));
	        originalPhotoDataBuffer = originalPhoto.getData().getDataBuffer();
	        originalPhotoSize = originalPhotoDataBuffer.getSize();
	        
			BufferedImage uploadedPhoto = ImageIO.read(new File("photos/Romano/Risotto" + File.separator + photoId));
			
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
	public void DownloadPhoto_EmptyPhotoString() {
		client.downloadPhoto("");
	}
	
}