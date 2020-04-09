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

import java.util.ArrayList;
import java.util.List;

import foodist.server.grpc.contract.Contract.ListMenuReply;
import foodist.server.grpc.contract.Contract.Menu;
import foodist.server.service.ServiceImplementation;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
  	public void listMenu_SingleMenu() {
    	client.addMenu("Burger Shop", "Burger", 3.50);
    	client.addPhoto("Burger Shop", "Burger", "photos/test/burger.png");
    	
    	List<String> menus = new ArrayList<String>();	  	
	  	ListMenuReply listMenuReply = client.listMenu("Burger Shop");
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus.add(m.getName());
	  	}
	  	
	  	String[] expected = {"Burger"};	  	
	    assertThat(menus, hasItems(expected));		
  	}	
	
	@Test
  	public void listMenu_MultipleMenu_OnePhotoForBoth() {
		client.addMenu("Hamburger Town", "Burger", 2.50);
		client.addMenu("Hamburger Town", "Double Burger", 4.50);
		
		client.addPhoto("Hamburger Town", "Burger", "photos/test/burger.png");
		client.addPhoto("Hamburger Town", "Double Burger", "photos/test/double_burger.jpg");
			  	
	  	List<String> menus = new ArrayList<String>();	  	
	  	ListMenuReply listMenuReply = client.listMenu("Hamburger Town");
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus.add(m.getName());
	  	}
	  	
	  	String[] expected = {"Burger", "Double Burger"};	    	  	
	    assertThat(menus, hasItems(expected));
  	}
	
	@Test
  	public void listMenu_MultipleMenu_TwoPhotosForJustOne() {
		client.addMenu("Pizza Parlor", "Cheese Pizza", 9.50);
	  	client.addMenu("Pizza Parlor", "Pepperoni Pizza", 11.00);
	  	
	  	client.addPhoto("Pizza Parlor", "Pepperoni Pizza", "photos/test/cheese_pizza.jpg");
		client.addPhoto("Pizza Parlor", "Pepperoni Pizza", "photos/test/pepperoni_pizza.jpg");
			  	
		List<String> menus = new ArrayList<String>();	  		
		ListMenuReply listMenuReply = client.listMenu("Pizza Parlor");
	  	
	  	for(Menu m : listMenuReply.getMenusList()) {
	  		menus.add(m.getName());
	  	}
	  	
	  	String[] expected = {"Cheese Pizza", "Pepperoni Pizza"};	  	
	  	assertThat(menus, hasItems(expected));
  	}
	
	@Test
  	public void listMenu_AvoidDuplicateMenus_SameNameSameObject() {
		client.addMenu("Healthy Veggies", "Salad", 2.50);
		client.addMenu("Healthy Veggies", "Salad", 2.50);	
		
		List<String> menus = new ArrayList<String>();	  		
		ListMenuReply lmReply = client.listMenu("Healthy Veggies");

		for(Menu m : lmReply.getMenusList()) {
			menus.add(m.getName());
		}
		
		String[] expected = {"Salad"};
		assertThat(menus, hasItems(expected));
  	}
	
	@Test
  	public void listMenu_AvoidDuplicateMenus_SameNameDifferentObject() {
		client.addMenu("Deutsch Kuche", "Wurst", 6.50);
		client.addMenu("Deutsch Kuche", "Wurst", 5.50);	
		
		List<String> menus = new ArrayList<String>();
		ListMenuReply lmReply = client.listMenu("Deutsch Kuche");

		for(Menu m : lmReply.getMenusList()) {
			menus.add(m.getName());
		}
		
		String[] expected = {"Wurst"};
		assertThat(menus, hasItems(expected));
  	}
	
	@Test
  	public void listMenu_NoMenusInFoodService() {
		ListMenuReply lmReply = client.listMenu("Invisible Restaurant");
		
		assertEquals(0, lmReply.getMenusList().size());
  	}
	
}