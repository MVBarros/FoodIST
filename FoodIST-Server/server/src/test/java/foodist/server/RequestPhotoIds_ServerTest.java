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

import foodist.server.grpc.contract.Contract.FoodType;
import foodist.server.service.ServiceImplementation;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class RequestPhotoIds_ServerTest {
	
	static final BindableService bindableService = new ServiceImplementation();
	static final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();    	
	static Client client;		
	static FoodType type;
	
	@BeforeClass
	public static void setUp() throws Exception {   				
		String serverName = InProcessServerBuilder.generateName();

		File priv = Security.getPrivateKey();
        File cert = Security.getPublicKey();       
		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().useTransportSecurity(cert, priv).addService(bindableService).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);
    	type = FoodType.Vegan;
	}
	
	@Test
  	public void RequestPhoto_Upload0Photo() throws IOException {
		client.addMenu("Mercado", "Alho", 0.69, type, "portuguese");											
		client.requestPhotoIds();		
		
		boolean exists = new File("photos/Mercado/Alho").exists();
		
		assertFalse(exists);		
  	}
	
	@Test
  	public void RequestPhoto_Upload1Photo() throws IOException {
		client.addMenu("Mercado", "Beterraba", 1.00, type, "portuguese");		
		client.addPhoto("Mercado", "Beterraba", "photos/test/beterraba.jpg");								
		for(String photoId : client.requestPhotoIds()) {
			client.downloadPhoto(photoId);
		}
		
		int total_iterations = 0;
		int wrong_iterations = 0;		
		
		String uploadedPhotoTitle = new File("photos/Mercado/Beterraba").list()[0];
		BufferedImage uploadedPhoto = ImageIO.read(new File("photos/Mercado/Beterraba/" + uploadedPhotoTitle));
		DataBuffer uploadedPhotoDataBuffer = uploadedPhoto.getData().getDataBuffer();
	    int uploadedPhotoSize = uploadedPhotoDataBuffer.getSize();
		for(String client : new File("photos/client/").list()) {
			BufferedImage downloadedPhoto = ImageIO.read(new File("photos/client/" + client));
			DataBuffer downloadedPhotoDataBuffer = downloadedPhoto.getData().getDataBuffer();
			int downloadedPhotoSize = downloadedPhotoDataBuffer.getSize();						
		    if(downloadedPhotoSize == uploadedPhotoSize) {
		    	for(int j=0; j<downloadedPhotoSize; j++) { 
		    		if(downloadedPhotoDataBuffer.getElem(j) != uploadedPhotoDataBuffer.getElem(j)) {	
		    			wrong_iterations--;
		                break;			                
		            }
		        }            
		    }
		    else {
		    	wrong_iterations++;
		    }
		    total_iterations++;
		}
					
		int right_photos = total_iterations - wrong_iterations;
		assertEquals(1, right_photos);
  	}
  	
	@Test
  	public void RequestPhoto_Upload2Photos() throws IOException {
		client.addMenu("Mercado", "Alface", 0.89, type, "portuguese");
				
		for(int i = 1; i<=2; i++) {
			client.addPhoto("Mercado", "Alface", "photos/test/alface_0" + i + ".jpg");			
		}			
		
		for(String photoId : client.requestPhotoIds()) {
			client.downloadPhoto(photoId);
		}	
		
		int total_iterations = 0;
		int wrong_iterations = 0;		
		for(String server : new File("photos/Mercado/Alface").list()) {
			BufferedImage uploadedPhoto = ImageIO.read(new File("photos/Mercado/Alface/" + server));
			DataBuffer uploadedPhotoDataBuffer = uploadedPhoto.getData().getDataBuffer();
		    int uploadedPhotoSize = uploadedPhotoDataBuffer.getSize();
			for(String client : new File("photos/client/").list()) {
				BufferedImage downloadedPhoto = ImageIO.read(new File("photos/client/" + client));
				DataBuffer downloadedPhotoDataBuffer = downloadedPhoto.getData().getDataBuffer();
				int downloadedPhotoSize = downloadedPhotoDataBuffer.getSize();						
			    if(downloadedPhotoSize == uploadedPhotoSize) {
			    	for(int j=0; j<downloadedPhotoSize; j++) { 
			    		if(downloadedPhotoDataBuffer.getElem(j) != uploadedPhotoDataBuffer.getElem(j)) {	
			    			wrong_iterations--;
			                break;			                
			            }
			        }            
			    }
			    else {
			    	wrong_iterations++;
			    }
			    total_iterations++;
			}
		}
					
		int right_photos = total_iterations - wrong_iterations;
		assertEquals(2, right_photos);
  	}

	@Test
  	public void RequestPhoto_Upload3Photos() throws IOException {
		client.addMenu("Mercado", "Cenoura", 0.79, type, "portuguese");
				
		for(int i = 1; i<=3; i++) {
			client.addPhoto("Mercado", "Cenoura", "photos/test/cenoura_0" + i + ".jpg");			
		}			
		
		for(String photoId : client.requestPhotoIds()) {
			client.downloadPhoto(photoId);
		}		
		
		int total_iterations = 0;
		int wrong_iterations = 0;		
		for(String server : new File("photos/Mercado/Cenoura").list()) {
			BufferedImage uploadedPhoto = ImageIO.read(new File("photos/Mercado/Cenoura/" + server));
			DataBuffer uploadedPhotoDataBuffer = uploadedPhoto.getData().getDataBuffer();
		    int uploadedPhotoSize = uploadedPhotoDataBuffer.getSize();
			for(String client : new File("photos/client/").list()) {
				BufferedImage downloadedPhoto = ImageIO.read(new File("photos/client/" + client));
				DataBuffer downloadedPhotoDataBuffer = downloadedPhoto.getData().getDataBuffer();
				int downloadedPhotoSize = downloadedPhotoDataBuffer.getSize();						
			    if(downloadedPhotoSize == uploadedPhotoSize) {
			    	for(int j=0; j<downloadedPhotoSize; j++) { 
			    		if(downloadedPhotoDataBuffer.getElem(j) != uploadedPhotoDataBuffer.getElem(j)) {	
			    			wrong_iterations--;
			                break;			                
			            }
			        }            
			    }
			    else {
			    	wrong_iterations++;
			    }
			    total_iterations++;
			}
		}
					
		int right_photos = total_iterations - wrong_iterations;
		assertEquals(3, right_photos);
  	}
	
	@Test
  	public void RequestPhoto_Upload4Photos() throws IOException {
		client.addMenu("Mercado", "Nabo", 0.99, type, "portuguese");
				
		for(int i = 1; i<=4; i++) {
			client.addPhoto("Mercado", "Nabo", "photos/test/nabo_0" + i + ".png");			
		}			
		
		for(String photoId : client.requestPhotoIds()) {
			client.downloadPhoto(photoId);
		}
		
		int total_iterations = 0;
		int wrong_iterations = 0;		
		for(String server : new File("photos/Mercado/Nabo").list()) {
			BufferedImage uploadedPhoto = ImageIO.read(new File("photos/Mercado/Nabo/" + server));
			DataBuffer uploadedPhotoDataBuffer = uploadedPhoto.getData().getDataBuffer();
		    int uploadedPhotoSize = uploadedPhotoDataBuffer.getSize();
			for(String client : new File("photos/client/").list()) {
				BufferedImage downloadedPhoto = ImageIO.read(new File("photos/client/" + client));
				DataBuffer downloadedPhotoDataBuffer = downloadedPhoto.getData().getDataBuffer();
				int downloadedPhotoSize = downloadedPhotoDataBuffer.getSize();						
			    if(downloadedPhotoSize == uploadedPhotoSize) {
			    	for(int j=0; j<downloadedPhotoSize; j++) { 
			    		if(downloadedPhotoDataBuffer.getElem(j) != uploadedPhotoDataBuffer.getElem(j)) {	
			    			wrong_iterations--;
			                break;			                
			            }
			        }            
			    }
			    else {
			    	wrong_iterations++;
			    }
			    total_iterations++;
			}
		}
					
		int right_photos = total_iterations - wrong_iterations;
		assertEquals(3, right_photos);
  	}
}
