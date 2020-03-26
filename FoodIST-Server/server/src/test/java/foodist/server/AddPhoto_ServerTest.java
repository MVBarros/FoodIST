package foodist.server;

import foodist.server.common.Utils;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.grpc.contract.Contract.AddPhotoRequest;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.ListMenuRequest;
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
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import com.google.protobuf.Empty;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class AddPhoto_ServerTest { 	
	
	private static final double TEST_PRICE = 1.50;
	
	private static final String BASE_DIR = "photos";
	private static final String TEST_FOODSERVICE = "Testbar";
	private static final String TEST_MENU = "Chourico";
	private static final String TEST_CLIENT_PHOTO = "photos/test/chourico.jpg";
	private static final String TEST_SERVER_PHOTO = "photos/Testbar/Chourico/";
  
	public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();    
	
	Client client;	
	final BindableService bindableService = new ServiceImplementation();	
	
	public BufferedImage originalPhoto;
	public DataBuffer originalPhotoDataBuffer;
	public int originalPhotoSize;
	
	@Before
	public void setUp() throws Exception {   
		File directory = new File(BASE_DIR);
		
		for(String filename : directory.list()) {
			if(filename.equals("test")) {
				continue;
			}
			else {
				Utils.deleteMenuDirectories(new File(BASE_DIR + "/" + filename), 0);
			}
		}	
		
		String serverName = InProcessServerBuilder.generateName();

		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(bindableService).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);
    	client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
    	client.addPhoto(TEST_FOODSERVICE, TEST_MENU, TEST_CLIENT_PHOTO);    
    	
    	originalPhoto = ImageIO.read(new File(TEST_CLIENT_PHOTO));
        originalPhotoDataBuffer = originalPhoto.getData().getDataBuffer();
        originalPhotoSize = originalPhotoDataBuffer.getSize();   
	}

	@Test
  	public void AddPhoto_FoodService() {
		boolean path_exists = new File(TEST_SERVER_PHOTO).exists();
		assertTrue(path_exists);
  	}
	
	@Test
  	public void AddPhoto_SamePhoto() {
		String photoId = new File(TEST_SERVER_PHOTO).list()[0];
		
		try {
			BufferedImage uploadedPhoto = ImageIO.read(new File(
					BASE_DIR + File.separator + "client" + File.separator 
					+ TEST_FOODSERVICE + File.separator + TEST_MENU 
					+ File.separator + photoId));
			
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