package pt.ulisboa.tecnico.cmov.foodist.domain;

import java.util.List;
import java.util.Map;

public class Menu {
    private String foodServiceName;
    private String menuName;
    private double price;


    public Menu(String foodServiceName, String menuName, double price) {
        this.foodServiceName = foodServiceName;
        this.menuName = menuName;
        this.price = price;
    }

    public String getFoodServiceName() {
        return this.foodServiceName;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public double getPrice() { return this.price; }

}
