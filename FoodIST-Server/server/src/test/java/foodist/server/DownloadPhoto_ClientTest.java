package foodist.server;

import foodist.server.common.Utils;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.grpc.contract.Contract.DownloadPhotoReply;
import foodist.server.grpc.contract.Contract.DownloadPhotoRequest;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.ListMenuRequest;
import foodist.server.grpc.contract.Contract.Menu;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class DownloadPhoto_ClientTest { 
		
	private static final double TEST_PRICE = 1.50;
	
	private static final String BASE_DIR = "photos";
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
        
	private final FoodISTServerServiceGrpc.FoodISTServerServiceImplBase serviceImpl =
			mock(FoodISTServerServiceGrpc.FoodISTServerServiceImplBase.class, delegatesTo(
					new FoodISTServerServiceGrpc.FoodISTServerServiceImplBase() {												
					    @Override
					    public void downloadPhoto(Contract.DownloadPhotoRequest request, StreamObserver<Contract.DownloadPhotoReply> responseObserver) {					    	
						    responseObserver.onNext(null);
						    responseObserver.onCompleted();      
					    } 
						
		}));
	Client client;		

	@Before
	public void setUp() throws Exception {   
		
		String serverName = InProcessServerBuilder.generateName();

		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(serviceImpl).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);            		
	}
	
	@Test
  	public void DownloadPhoto_FoodService() {
		ArgumentCaptor<DownloadPhotoRequest> requestCaptor = ArgumentCaptor.forClass(DownloadPhotoRequest.class);
	    client.downloadPhoto(TEST_PHOTO, TEST_FOODSERVICE, TEST_MENU);
	    verify(serviceImpl).downloadPhoto(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<DownloadPhotoReply>>any());
	    assertEquals(TEST_FOODSERVICE, requestCaptor.getValue().getFoodService());
  	}
	
	@Test
  	public void DownloadPhoto_MenuName() {
		ArgumentCaptor<DownloadPhotoRequest> requestCaptor = ArgumentCaptor.forClass(DownloadPhotoRequest.class);
	    client.downloadPhoto(TEST_PHOTO, TEST_FOODSERVICE, TEST_MENU);
	    verify(serviceImpl).downloadPhoto(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<DownloadPhotoReply>>any());
	    assertEquals(TEST_MENU, requestCaptor.getValue().getMenuName());
  	}
	
	@Test
  	public void DownloadPhoto_Photo() {
		ArgumentCaptor<DownloadPhotoRequest> requestCaptor = ArgumentCaptor.forClass(DownloadPhotoRequest.class);
	    client.downloadPhoto(TEST_PHOTO, TEST_FOODSERVICE, TEST_MENU);
	    verify(serviceImpl).downloadPhoto(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<DownloadPhotoReply>>any());
	    assertEquals(TEST_PHOTO, requestCaptor.getValue().getPhotoId());
  	}
	
}