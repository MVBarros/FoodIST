package foodist.server;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;

import foodist.server.service.ServiceImplementation;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class RequestPhotoIds_ServerTest {
	
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
  	public void RequestPhoto_Test() throws IOException {
		client.addMenu("Mercado", "Nabo", 1.00);
				
		for(int i = 1; i<=4; i++) {
			client.addPhoto("Mercado", "Nabo", "photos/test/nabo_0" + i + ".png");			
		}			
		
		client.requestPhotoIds();	
		
		//boolean[] expected = {true, true, true};		
		//ArrayList<Boolean> booleanList = new ArrayList<Boolean>();
		
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
