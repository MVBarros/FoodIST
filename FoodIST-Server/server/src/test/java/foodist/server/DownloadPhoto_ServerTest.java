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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import foodist.server.common.Utils;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.service.ServiceImplementation;

@RunWith(JUnit4.class)
public class DownloadPhoto_ServerTest { 
		
	private static final double TEST_PRICE = 1.50;
	
	private static final String BASE_DIR = "photos";
	private static final String TEST_ALTERNATIVE_FOODSERVICE = "Lakeviewrestaurant";
	private static final String TEST_ALTERNATIVE_MENU = "Farinheira";
	private static final String TEST_ALTERNATIVE_PHOTO = "photos/test/farinheira.png";
	private static final String TEST_FOODSERVICE = "Testbar";
	private static final String TEST_MENU = "Chourico";
	private static final String TEST_PHOTO = "photos/test/chourico.jpg";
  
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
    	client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
    	client.addPhoto(TEST_FOODSERVICE, TEST_MENU, TEST_PHOTO);    
    	
		client.addMenu(TEST_FOODSERVICE, TEST_ALTERNATIVE_MENU, TEST_PRICE);
		client.addPhoto(TEST_FOODSERVICE, TEST_ALTERNATIVE_MENU, TEST_ALTERNATIVE_PHOTO);
		
    	originalPhoto = ImageIO.read(new File(TEST_PHOTO));
        originalPhotoDataBuffer = originalPhoto.getData().getDataBuffer();
        originalPhotoSize = originalPhotoDataBuffer.getSize();    
        
        client.addMenu(TEST_ALTERNATIVE_FOODSERVICE, TEST_MENU, TEST_PRICE);
        client.addPhoto(TEST_ALTERNATIVE_FOODSERVICE, TEST_MENU, TEST_PHOTO);
	}
	
	@Test
  	public void DownloadPhoto_ClientPath() {
	  	String photoId = "";	
	  	ListMenuReply listMenuReply = client.listMenu(TEST_ALTERNATIVE_FOODSERVICE);
	  	
	  	photoId = listMenuReply.getMenusList().get(0).getPhotoId(0);
	  	
		boolean path_exists = new File(BASE_DIR + File.separator + "client" + File.separator 
				+ TEST_ALTERNATIVE_FOODSERVICE + File.separator + TEST_MENU 
				+ File.separator + photoId).exists();
		assertTrue(path_exists);
  	}
	
	@Test
  	public void DownloadPhoto_SamePhoto() {		
	  	String photoId = "";	
	  	ListMenuReply listMenuReply = client.listMenu(TEST_ALTERNATIVE_FOODSERVICE);
	  	
	  	photoId = listMenuReply.getMenusList().get(0).getPhotoId(0);
	  	
		client.downloadPhoto(photoId, TEST_ALTERNATIVE_FOODSERVICE, TEST_MENU);

		try {
			BufferedImage uploadedPhoto = ImageIO.read(new File(
					BASE_DIR + File.separator + "client" + File.separator 
					+ TEST_ALTERNATIVE_FOODSERVICE + File.separator + TEST_MENU 
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