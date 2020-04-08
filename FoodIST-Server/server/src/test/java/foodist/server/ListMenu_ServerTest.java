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

import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.service.ServiceImplementation;

@RunWith(JUnit4.class)
public class ListMenu_ServerTest { 

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
  	public void listSingleMenu_SameFoodService() {
    	client.addMenu("Burger Shop", "Burger", 3.50);
    	client.addPhoto("Burger Shop", "Burger", "photos/test/burger.png");
    	
	  	String menus = "";	
	  	ListMenuReply listMenuReply = client.listMenu("Burger Shop");
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus += m.getName();
	  	}
	  	
		assertEquals("Burger", menus);
  	}
	
	@Test
  	public void listMultipleMenus_SameFoodService() {
		client.addMenu("Hamburger Town", "Burger", 2.50);
		client.addMenu("Hamburger Town", "Double Burger", 4.50);
		
		client.addPhoto("Hamburger Town", "Burger", "photos/test/burger.png");
		client.addPhoto("Hamburger Town", "Double Burger", "photos/test/double_burger.jpg");
		
	  	String menus = "";	
	  	ListMenuReply listMenuReply = client.listMenu("Hamburger Town");
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus += m.getName();
	  	}
	  	
	  	String expected = "Burger" + "Double Burger";
		assertEquals(expected, menus);
  	}
	
	@Test
  	public void listMultipleMenus_DifferentFoodService() {
		client.addMenu("Pizza Parlor", "Cheese Pizza", 9.50);
	  	client.addMenu("Pizza Parlor", "Pepperoni Pizza", 11.00);
	  	
	  	client.addPhoto("Pizza Parlor", "Pepperoni Pizza", "photos/test/cheese_pizza.jpg");
		client.addPhoto("Pizza Parlor", "Pepperoni Pizza", "photos/test/pepperoni_pizza.jpg");
			  	
		String menus = "";	
		ListMenuReply listMenuReply = client.listMenu("Pizza Parlor");
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus += m.getName();
	  	}
	  	
	  	String expected = "Pepperoni Pizza" +  "Cheese Pizza";	  	
	  	assertEquals(expected, menus);
	  	
  	}
	
	@Test
  	public void listMenu_AvoidDuplicateMenus_SameNameSameObject() {
		client.addMenu("Healthy Veggies", "Salad", 2.50);
		client.addMenu("Healthy Veggies", "Salad", 2.50);		
		ListMenuReply lmReply = client.listMenu("Healthy Veggies");
		String listedMenus = "";
		for(Menu m : lmReply.getMenusList()) {
			listedMenus += m.getName();
		}
		assertEquals("Salad", listedMenus);
  	}
	
	@Test
  	public void listMenu_AvoidDuplicateMenus_SameNameDifferentObject() {
		client.addMenu("Deutsch Kuche", "Wurst", 6.50);
		client.addMenu("Deutsch Kuche", "Wurst", 5.50);	
		ListMenuReply lmReply = client.listMenu("Deutsch Kuche");
		String listedMenus = "";
		for(Menu m : lmReply.getMenusList()) {
			listedMenus += m.getName();
		}
		assertEquals("Wurst", listedMenus);
  	}
	
}