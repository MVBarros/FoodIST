package pt.ulisboa.tecnico.cmov.foodist.domain;

import java.util.List;

import foodist.server.grpc.contract.Contract;

public class Menu {
    private String foodServiceName;
    private String menuName;
    private double price;
    private int photoIdCount = 0;
    private String[] photoID;

    //To send menus to the server
    public Menu(String foodServiceName, String menuName, double price) {
        this.foodServiceName = foodServiceName;
        this.menuName = menuName;
        this.price = price;
    }

    //When parsing the menus from the server
    public Menu(String foodServiceName, String menuName, double price, int photoIdCount, List<String> photoIDs) {
        this(foodServiceName, menuName, price);
        this.photoIdCount = photoIdCount;
        this.photoID = photoIDs.toArray(new String[0]);
    }

    public String getFoodServiceName() {
        return this.foodServiceName;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public double getPrice() {
        return this.price;
    }

    public int getPhotoIdCount() {
        return this.photoIdCount;
    }

    public String[] getPhotoID() {
        return this.photoID;
    }

    public static Menu parseContractMenu(String foodServiceName, Contract.Menu menu) {
        return new Menu(foodServiceName, menu.getName(), menu.getPrice(), menu.getPhotoIdCount(), menu.getPhotoIdList());
    }
}
