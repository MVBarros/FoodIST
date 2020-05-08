package pt.ulisboa.tecnico.cmov.foodist.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import foodist.server.grpc.contract.Contract;

public class Menu {
    private String foodServiceName;
    private String menuName;
    private double price;
    private Contract.FoodType type;
    private String language;
    private String translatedName;
    private String menuId;
    private ArrayList<String> photoIds;
    private double averageRating;
    private ArrayList<Double> ratings;

    //To send menus to the server
    public Menu(String foodServiceName, String menuName, double price, Contract.FoodType type, String language, String translatedName) {
        this.foodServiceName = foodServiceName;
        this.menuName = menuName;
        this.price = price;
        this.type = type;
        this.language = language;
        this.translatedName = translatedName;
    }

    //To send menus to the server
    public Menu(String foodServiceName, String menuName, double price, Contract.FoodType type, String language) {
        this.foodServiceName = foodServiceName;
        this.menuName = menuName;
        this.price = price;
        this.type = type;
        this.language = language;
    }

    //To receive menus from the server
    public Menu(String originalName, double price, Contract.FoodType type, String language, String translatedName, String menuId, List<String> photoIds, double averageRating, List<Double> ratings) {
        this.price = price;
        this.type = type;
        this.language = language;
        this.translatedName = translatedName;
        this.menuName = originalName;
        this.menuId = menuId;
        this.photoIds = new ArrayList<>(photoIds);
        this.averageRating = averageRating;
        this.ratings = new ArrayList<>(ratings);
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

    public static Menu parseContractMenu(Contract.Menu menu) {
        return new Menu(menu.getOriginalName(), menu.getPrice(), menu.getType(), menu.getLanguage(), menu.getTranslatedName(), String.valueOf(menu.getMenuId()), menu.getPhotoIdList(), menu.getAverageRating(), menu.getRatingsList());
    }

    public Contract.FoodType getType() {
        return type;
    }

    public boolean isDesirable(Map<Contract.FoodType, Boolean> constraints) {
        return constraints.get(this.type);
    }

    public String getLanguageName() {
        return this.language;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getTranslatedName() {
        return this.translatedName;
    }

    public String getMenuId() {
        return menuId;
    }

    public ArrayList<String> getPhotoIds() {
        return photoIds;
    }
    public double getAverageRating() { return averageRating; }

    public ArrayList<Double> getRatings() { return ratings; }
}
