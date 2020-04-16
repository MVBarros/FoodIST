package pt.ulisboa.tecnico.cmov.foodist.domain;

import java.util.List;

import foodist.server.grpc.contract.Contract;

public class Menu {
    private String foodServiceName;
    private String menuName;
    private double price;
    private int photoIdCount = 0;
    private String[] photoID;
    private Contract.FoodType type;

    //To send menus to the server
    public Menu(String foodServiceName, String menuName, double price, Contract.FoodType type) {
        this.foodServiceName = foodServiceName;
        this.menuName = menuName;
        this.price = price;
        this.type = type;
    }

    //When parsing the menus from the server
    public Menu(String foodServiceName, String menuName, double price, int photoIdCount, List<String> photoIDs, Contract.FoodType type) {
        this(foodServiceName, menuName, price, type);
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
        return new Menu(foodServiceName, menu.getName(), menu.getPrice(), menu.getPhotoIdCount(), menu.getPhotoIdList(), menu.getType());
    }

    public Contract.FoodType getType() {
        return type;
    }
}
