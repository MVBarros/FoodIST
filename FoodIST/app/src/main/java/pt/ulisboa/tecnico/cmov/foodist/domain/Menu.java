package pt.ulisboa.tecnico.cmov.foodist.domain;

import java.util.List;
import java.util.Map;

import foodist.server.grpc.contract.Contract;

public class Menu {
    private String foodServiceName;
    private String menuName;
    private double price;
    private Contract.FoodType type;
    private Contract.Language language;
    private String translatedName;

    //To send menus to the server
    public Menu(String foodServiceName, String menuName, double price, Contract.FoodType type, Contract.Language language, String translatedName) {
        this.foodServiceName = foodServiceName;
        this.menuName = menuName;
        this.price = price;
        this.type = type;
        this.language = language;
        this.translatedName = translatedName;
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

    public static Menu parseContractMenu(String foodServiceName, Contract.Menu menu) {
        return new Menu(foodServiceName, menu.getName(), menu.getPrice(), menu.getType(), menu.getLanguage(), menu.getTranslatedName());
    }

    public Contract.FoodType getType() {
        return type;
    }

    public boolean isDesirable(Map<Contract.FoodType, Boolean> constraints) {
        return constraints.get(this.type);
    }

    public String getLanguageName() {
        return this.language.name();
    }

    public Contract.Language getLanguage() {
        return this.language;
    }

    public String getTranslatedName() {
        return this.translatedName;
    }
}
