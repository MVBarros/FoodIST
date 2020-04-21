package foodist.server;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import foodist.server.grpc.contract.Contract.FoodType;
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
	final BindableService bindableService = new ServiceImplementation(true);

	@Before
	public void setUp() throws Exception {   
		
		String serverName = InProcessServerBuilder.generateName();
      
		grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(bindableService).build().start());
		ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

    	client = new Client(channel);    
	}
	
	@Test
  	public void listMenu_SingleMenu() {
		FoodType type = FoodType.Meat;
    	client.addMenu("Burger Shop", "Burger", 3.50, type, "portuguese");
    	client.addPhoto("Burger Shop", "Burger", "photos/test/burger.png");
    	
    	List<String> menus = new ArrayList<String>();	  	
	  	
	  	for(Menu m : client.listMenu("Burger Shop", "portuguese")) {
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
		FoodType type = FoodType.Meat;
		client.addMenu("Hamburger Town", "Burger", 2.50, type, "portuguese");
		client.addMenu("Hamburger Town", "Double Burger", 4.50, type, "portuguese");
		
		client.addPhoto("Hamburger Town", "Burger", "photos/test/burger.png");
		client.addPhoto("Hamburger Town", "Double Burger", "photos/test/double_burger.jpg");
			  	
	  	List<String> menus = new ArrayList<String>();	  	
	  	
	  	for(Menu m : client.listMenu("Hamburger Town", "portuguese")) {
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
		FoodType vegetarian = FoodType.Vegetarian;
		FoodType meat = FoodType.Meat;
		
		client.addMenu("Pizza Parlor", "Cheese Pizza", 9.50, vegetarian, "portuguese");
	  	client.addMenu("Pizza Parlor", "Pepperoni Pizza", 11.00, meat, "portuguese");
	  	
	  	client.addPhoto("Pizza Parlor", "Pepperoni Pizza", "photos/test/cheese_pizza.jpg");
		client.addPhoto("Pizza Parlor", "Pepperoni Pizza", "photos/test/pepperoni_pizza.jpg");
			  	
		List<String> menus = new ArrayList<String>();	  		
	  	
	  	for(Menu m : client.listMenu("Pizza Parlor", "portuguese")) {
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
		FoodType type = FoodType.Vegan;
		
		client.addMenu("Healthy Veggies", "Salad", 2.50, type, "portuguese");
		client.addMenu("Healthy Veggies", "Salad", 2.50, type, "portuguese");	
		
		List<String> menus = new ArrayList<String>();	  		

		for(Menu m : client.listMenu("Healthy Veggies", "portuguese")) {
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
		FoodType type = FoodType.Meat;
		
		client.addMenu("Deutsch Kuche", "Wurst", 6.50, type, "portuguese");
		client.addMenu("Deutsch Kuche", "Wurst", 5.50, type, "portuguese");	
		
		List<String> menus = new ArrayList<String>();

		for(Menu m : client.listMenu("Deutsch Kuche", "portuguese")) {
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
		assertEquals(0, client.listMenu("Invisible Restaurant", "portuguese").size());
  	}
	
	@Test
  	public void UpdateMenu_DonwloadPhoto() throws IOException {
		FoodType meat = FoodType.Meat;
		FoodType vegetarian = FoodType.Vegetarian;
		
		client.addMenu("Graveli", "Pepperoni", 14.99, meat, "portuguese");
		client.addMenu("Graveli", "Cheese", 12.99, vegetarian, "portuguese");
		
		String[] format = {"pepperoni_pizza.jpg", "pepperoni_pizza.png"};
		client.addPhoto("Graveli", "Cheese", "photos/test/cheese_pizza.jpg");
		for(int i = 0; i<format.length; i++) {
			client.addPhoto("Graveli", "Pepperoni", "photos/test/" + format[i]);
		}		
		
		for(Menu menu : client.listMenu("Graveli", "portuguese")) {
			for(String photoId : menu.getPhotoIdList()) {
				client.downloadPhoto(photoId);
			}
		}
		
		int photos = 
				new File("photos/Graveli/Pepperoni/").list().length + 
				new File("photos/Graveli/Cheese/").list().length;
		assertEquals(3, photos);
  	}
	
	@AfterClass
	public static void Clean() throws IOException {
		FileUtils.forceDelete(new File("photos/Burger Shop"));
		FileUtils.forceDelete(new File("photos/Hamburger Town"));
		FileUtils.forceDelete(new File("photos/Pizza Parlor"));		
		FileUtils.forceDelete(new File("photos/Graveli"));
	}
	
}