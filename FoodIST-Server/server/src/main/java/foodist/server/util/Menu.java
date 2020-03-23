package foodist.server.util;

public class Menu {
	private String name;
	private double price;
	private String photoDirectory;
	
	public Menu(String name, double price) {
		this.name = name;
		this.price = price;
	}
	
	public String getName() {
		return this.name;
	}
	
	public double getPrice() {
		return this.price;
	}
	
	public String getPhotoDirectory() {
		return this.photoDirectory;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setPhotoDirectory(String photoDirectory) {
		this.photoDirectory = photoDirectory;
	}
}
