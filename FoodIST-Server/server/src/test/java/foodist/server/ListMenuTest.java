package foodist.server;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import foodist.server.common.Utils;
import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.ListMenuRequest;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.service.ServiceImplementation;

@RunWith(JUnit4.class)
public class ListMenuTest { 
		
	private static final double TEST_PRICE = 1.50;
	
	private static final String BASE_DIR = "photos";
	private static final String TEST_FOODSERVICE = "Testbar";
	private static final String TEST_MENU = "Chourico";
	private static final String TEST_ALTERNATIVE_MENU = "Farinheira";
	private static final String TEST_PHOTO = "photos/test/chourico.jpg";
	private static final String TEST_ALTERNATIVE_PHOTO = "photos/test/farinheira.png";
  
	@Rule
	public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
        
	Client client;	
	final BindableService bindableService = new ServiceImplementation();

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
    	client.addPhoto(TEST_FOODSERVICE, TEST_MENU, TEST_PHOTO);
	}

	@Test
  	public void listSingleMenu_SameFoodService() {
	  	String menus = "";	
	  	ListMenuReply listMenuReply = client.listMenu(TEST_FOODSERVICE);
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus = m.getName();
	  	}
	  	
		assertEquals(TEST_MENU, menus);
  	}
	
	@Test
  	public void listMultipleMenus_SameFoodService() {
		client.addMenu(TEST_FOODSERVICE, TEST_ALTERNATIVE_MENU, TEST_PRICE);
		client.addPhoto(TEST_FOODSERVICE, TEST_ALTERNATIVE_MENU, TEST_ALTERNATIVE_PHOTO);
		
	  	String menus = "";	
	  	ListMenuReply listMenuReply = client.listMenu(TEST_FOODSERVICE);
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus += m.getName();
	  	}
	  	
		assertEquals(TEST_MENU+TEST_ALTERNATIVE_MENU, menus);
  	}
	
	@Test
  	public void listMultipleMenus_DifferentFoodService() {
		client.addMenu(TEST_FOODSERVICE, TEST_ALTERNATIVE_MENU, TEST_PRICE);
		client.addPhoto(TEST_FOODSERVICE, TEST_ALTERNATIVE_MENU, TEST_ALTERNATIVE_PHOTO);
						
	  	String menus = "";	
	  	
	  	ListMenuReply listMenuReply = client.listMenu(TEST_FOODSERVICE);
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus += m.getName();
	  	}
	  	
	  	client.addMenu("Sewerbar", TEST_MENU, TEST_PRICE);
		client.addPhoto("Sewerbar", TEST_MENU, TEST_ALTERNATIVE_PHOTO);
	  	
	  	listMenuReply = client.listMenu("Sewerbar");
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus += m.getName();
	  	}
	  	
	  	assertEquals(TEST_MENU+TEST_ALTERNATIVE_MENU+TEST_MENU, menus);
	  	
  	}
	
}