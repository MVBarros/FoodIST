package foodist.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;

import foodist.server.grpc.contract.Contract.AddMenuRequest;
import foodist.server.grpc.contract.Contract.AddPhotoRequest;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.ListMenuRequest;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub;
import foodist.server.service.ServiceImplementation;
import foodist.server.util.PhotoBuilder;

@RunWith(JUnit4.class)
public class ServerTest { 
  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
	private static final int TEST_PORT = 8080;
	private static final double TEST_PRICE = 1.50;
	
  private static final String TEST_FOODSERVICE = "Testbar";
  private static final String TEST_MENU = "Chourico.jpg";
  private static final String TEST_PHOTO = "photos/test/chourico.jpg";
  
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
  
  private final BindableService bindableService = new ServiceImplementation();
  
  class Client {
	  FoodISTServerServiceBlockingStub stub;
	  ManagedChannel channel;
	  Client(ManagedChannel channel) {
		  this.channel = channel;
		  this.stub = FoodISTServerServiceGrpc.newBlockingStub(this.channel);
	  }  
	  
	  void addMenu(String foodService, String name, double price) {		 
		  System.out.println(foodService);
		  AddMenuRequest.Builder addMenuBuilder = AddMenuRequest.newBuilder();					
		  
		  addMenuBuilder.setFoodService(foodService);
		  addMenuBuilder.setName(name);
		  addMenuBuilder.setPrice(price);					
		  
		  AddMenuRequest addMenuRequestExample = addMenuBuilder.build();
			
		  this.stub.addMenu(addMenuRequestExample);     
	  }
	  
	  void listMenu(String foodService) {		  		  
		  ListMenuRequest listMenuRequest = ListMenuRequest.newBuilder().setFoodService(foodService).build();
		  
		  System.out.println(foodService);
		  ListMenuReply listMenuReply = this.stub.listMenu(listMenuRequest);    
		  List<Menu> list = listMenuReply.getMenusList();
		  
		  for(Menu m : list) {
			  System.out.println(m.getName());
		  }
	  }
	  
	  void addPhoto(String photoName, String photoFoodService, String photoPath) {
			AddPhotoRequest.Builder addPhotoRequest = AddPhotoRequest.newBuilder();
			
			final CountDownLatch finishLatch = new CountDownLatch(1);
	        int sequence = 0;
	        
	        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
	            @Override
	            public void onNext(Empty empty) {
	            }

	            @Override
	            public void onError(Throwable throwable) {
	                System.out.println("Error uploading file, does that file already exist?" + throwable.getMessage());
	                finishLatch.countDown();
	            }

	            @Override
	            public void onCompleted() {
	                System.out.println("File uploaded successfully");
	                finishLatch.countDown();
	            }
	        };
	        
	        FoodISTServerServiceGrpc.FoodISTServerServiceStub foodISTServerServiceStub = FoodISTServerServiceGrpc.newStub(channel);			        
	        StreamObserver<Contract.AddPhotoRequest> requestObserver = foodISTServerServiceStub.addPhoto(responseObserver);
	        byte[] data = new byte[1024 * 1024];

	        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(photoPath))) {
	            int numRead;
	            //Send file chunks to server
	            while ((numRead = in.read(data)) >= 0) {
	            	Contract.AddPhotoRequest.Builder addPhotoRequestBuilder = Contract.AddPhotoRequest.newBuilder();
	                
	            	addPhotoRequestBuilder.setContent(ByteString.copyFrom(Arrays.copyOfRange(data, 0, numRead)));
	            	addPhotoRequestBuilder.setName(photoName);
	            	addPhotoRequestBuilder.setSequenceNumber(sequence);
	            	addPhotoRequestBuilder.setFoodService(photoFoodService);
	                requestObserver.onNext(addPhotoRequestBuilder.build());
	                sequence++;
	            }

	            requestObserver.onCompleted();

	            //Wait for server to finish saving file to Database
	            finishLatch.await();

	        } catch (FileNotFoundException e) {
	            System.out.println(String.format("File with filename: %s not found.", photoPath));
	        } catch (IOException | InterruptedException e) {
	            //Should Never Happen
	            System.exit(1);
	        }
	  }
  }
  
  private Client client;

  @Before
  public void setUp() throws Exception {
    // Generate a unique in-process server name.
	String serverName = InProcessServerBuilder.generateName();
	
	final BindableService bindableService = new ServiceImplementation();
    // Create a server, add service, start, and register for automatic graceful shutdown.
	
	grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(bindableService).build().start());

    // Create a client channel and register for automatic graceful shutdown.
    ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    // Create a HelloWorldClient using the in-process channel;
    client = new Client(channel);
    
    client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);    
  }

  /**
   * To test the client, call from the client against the fake server, and verify behaviors or state
   * changes from the server side.
   */
  @Test
  public void greet_messageDeliveredToServer() {
    //ArgumentCaptor<ListMenuRequest> requestCaptor = ArgumentCaptor.forClass(ListMenuRequest.class);
	
    client.listMenu(TEST_FOODSERVICE);   
    client.addPhoto(TEST_MENU, TEST_FOODSERVICE, TEST_PHOTO);
    /*verify(serviceImpl).listMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<ListMenuRequest>>any());
    assertEquals("test name", requestCaptor.getValue().getName());*/
  }
}