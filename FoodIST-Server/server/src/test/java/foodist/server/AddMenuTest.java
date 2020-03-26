package foodist.server;

import com.google.protobuf.Empty;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
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
import static org.mockito.Mockito.verify;
import static org.mockito.AdditionalAnswers.delegatesTo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import foodist.server.common.Utils;
import foodist.server.grpc.contract.Contract.AddMenuRequest;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.service.ServiceImplementation;

import static org.mockito.Mockito.mock;


@RunWith(JUnit4.class)
public class AddMenuTest { 
		
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
						
						private HashMap<String, List<Menu>> menusHashMap = new HashMap<String, List<Menu>>();
						
					    @Override
					    public void addMenu(Contract.AddMenuRequest request, StreamObserver<Empty> responseObserver) {
					    	Menu.Builder menuBuilder = Menu.newBuilder();
					    	
						    String foodService = request.getFoodService();                     
						    menuBuilder.setName(request.getName());
						    menuBuilder.setPrice(request.getPrice());
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
	Client client;	

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

		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(serviceImpl).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);        	     	
	}

	@Test
  	public void AddMenu_FoodService() {
	    ArgumentCaptor<AddMenuRequest> requestCaptor = ArgumentCaptor.forClass(AddMenuRequest.class);
	    client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
	    verify(serviceImpl).addMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<Empty>>any());
	    assertEquals(TEST_FOODSERVICE, requestCaptor.getValue().getFoodService());       
  	}
	
	@Test
  	public void AddMenu_Name() {
	    ArgumentCaptor<AddMenuRequest> requestCaptor = ArgumentCaptor.forClass(AddMenuRequest.class);
	    client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
	    verify(serviceImpl).addMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<Empty>>any());
	    assertEquals(TEST_MENU, requestCaptor.getValue().getName());       
  	}
	
	@Test
  	public void AddMenu_Price() {
	    ArgumentCaptor<AddMenuRequest> requestCaptor = ArgumentCaptor.forClass(AddMenuRequest.class);
	    client.addMenu(TEST_FOODSERVICE, TEST_MENU, TEST_PRICE);
	    verify(serviceImpl).addMenu(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<Empty>>any());
	    assertEquals(TEST_PRICE, requestCaptor.getValue().getPrice(), 0.01);       
  	}
	
}