package foodist.server;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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

import com.google.protobuf.Empty;

import foodist.server.grpc.contract.Contract.AddMenuRequest;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.ListMenuRequest;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub;

@RunWith(JUnit4.class)
public class ServerTest { 
  /**
   * This rule manages automatic graceful shutdown for the registered servers and channels at the
   * end of test.
   */
  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

  private final FoodISTServerServiceGrpc.FoodISTServerServiceImplBase serviceImpl =
      mock(FoodISTServerServiceGrpc.FoodISTServerServiceImplBase.class, delegatesTo(
          new FoodISTServerServiceGrpc.FoodISTServerServiceImplBase() {
        	  private HashMap<String, List<Menu>> menusHashMap = new HashMap<String, List<Menu>>();
        	  
        	  @Override
        	  public void listMenu(Contract.ListMenuRequest request, StreamObserver<Contract.ListMenuReply> responseObserver) {
        	      String foodService = request.getFoodService();                
        	        
        	      List<Menu> menuList = menusHashMap.get(foodService);
        	        
        	      ListMenuReply.Builder listMenuReplyBuilder = ListMenuReply.newBuilder();
        	        
        	      for(Menu m : menuList) {
        	    	System.out.println("#%" + m.getName());
        	        listMenuReplyBuilder.addMenus(m);        	
        	      }
        	        
        	      ListMenuReply listMenuReply = listMenuReplyBuilder.build();        
        	      responseObserver.onNext(listMenuReply);
        	      responseObserver.onCompleted();   
        	  }
        	  
        	  @Override
        	  public void addMenu(Contract.AddMenuRequest request, StreamObserver<Empty> responseObserver) {
        		  Menu.Builder menuBuilder = Menu.newBuilder();
        	    	
        	      String foodService = request.getFoodService();                     
        	      menuBuilder.setName(request.getName());
        	      menuBuilder.setPrice(request.getPrice());
        	      //menuBuilder.setPhotoId(index, value);
        	      Menu menu = menuBuilder.build();
        	        
        	      System.out.println(request.getName() + ":" + request.getPrice());
        	      List<Menu> menuList = this.menusHashMap.get(foodService);
        	      
        	      if(menuList!=null) {
        	    	  menuList.add(menu);
        	    	  this.menusHashMap.put(foodService, menuList);         
        	      } 
        	      else {
        	    	  List<Menu> new_MenuList = new ArrayList<Menu>();
        	    	  new_MenuList.add(menu);
        	    	  this.menusHashMap.put(foodService, new_MenuList);         
        	      } 
        	      
        	      responseObserver.onNext(null);
        	      responseObserver.onCompleted();   
        	  }   
          }));

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
  }
  
  private Client client;

  @Before
  public void setUp() throws Exception {
    // Generate a unique in-process server name.
	String serverName = InProcessServerBuilder.generateName();
        
    // Create a server, add service, start, and register for automatic graceful shutdown.
    grpcCleanup.register(InProcessServerBuilder
        .forName(serverName).directExecutor().addService(serviceImpl).build().start());

    // Create a client channel and register for automatic graceful shutdown.
    ManagedChannel channel = grpcCleanup.register(
        InProcessChannelBuilder.forName(serverName).directExecutor().build());

    // Create a HelloWorldClient using the in-process channel;
    client = new Client(channel);
    
    client.addMenu("Redbar", "chourico", 4.50);    
  }

  /**
   * To test the client, call from the client against the fake server, and verify behaviors or state
   * changes from the server side.
   */
  @Test
  public void greet_messageDeliveredToServer() {
    //ArgumentCaptor<ListMenuRequest> requestCaptor = ArgumentCaptor.forClass(ListMenuRequest.class);

    client.listMenu("Redbar");    
    /*verify(serviceImpl).listMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<ListMenuRequest>>any());
    assertEquals("test name", requestCaptor.getValue().getName());*/
  }
}