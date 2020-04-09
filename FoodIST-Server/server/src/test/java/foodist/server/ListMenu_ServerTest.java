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
	  	
	  	for(Menu m : client.listMenu("Burger Shop")) {
	  		menus.add(m.getName());
			for(String photoId : m.getPhotoIdList()) {
				client.downloadPhoto(photoId);
			}
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
	  	
	  	for(Menu m : client.listMenu("Hamburger Town")) {
	  		menus.add(m.getName());
	  		for(String photoId : m.getPhotoIdList()) {
				client.downloadPhoto(photoId);
			}
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
	  	
	  	for(Menu m : client.listMenu("Pizza Parlor")) {
	  		menus.add(m.getName());
	  		for(String photoId : m.getPhotoIdList()) {
				client.downloadPhoto(photoId);
			}
	  	}
	  	
	  	String[] expected = {"Cheese Pizza", "Pepperoni Pizza"};	  	
	  	assertThat(menus, hasItems(expected));
  	}
	
	@Test
  	public void listMenu_AvoidDuplicateMenus_SameNameSameObject() {
		client.addMenu("Healthy Veggies", "Salad", 2.50);
		client.addMenu("Healthy Veggies", "Salad", 2.50);	
		
		List<String> menus = new ArrayList<String>();	  		

		for(Menu m : client.listMenu("Healthy Veggies")) {
			menus.add(m.getName());
			for(String photoId : m.getPhotoIdList()) {
				client.downloadPhoto(photoId);
			}
		}
		
		String[] expected = {"Salad"};
		assertThat(menus, hasItems(expected));
  	}
	
	@Test
  	public void listMenu_AvoidDuplicateMenus_SameNameDifferentObject() {
		client.addMenu("Deutsch Kuche", "Wurst", 6.50);
		client.addMenu("Deutsch Kuche", "Wurst", 5.50);	
		
		List<String> menus = new ArrayList<String>();

		for(Menu m : client.listMenu("Deutsch Kuche")) {
			menus.add(m.getName());
			for(String photoId : m.getPhotoIdList()) {
				client.downloadPhoto(photoId);
			}
		}
		
		String[] expected = {"Wurst"};
		assertThat(menus, hasItems(expected));
  	}
	
	@Test
  	public void listMenu_NoMenusInFoodService() {		
		assertEquals(0, client.listMenu("Invisible Restaurant").size());
  	}
	
}