package pt.ulisboa.tecnico.cmov.foodist.domain;

import foodist.server.grpc.contract.Contract;

public class Photo {
    private String foodServiceName;
    private String menuName;
    private String photoPath;

    //To send menus to the server
    public Photo(String foodServiceName, String menuName, String photoPath) {
        this.foodServiceName = foodServiceName;
        this.menuName = menuName;
        this.photoPath = photoPath;
    }

    public String getFoodServiceName() {
        return this.foodServiceName;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public String getPhotoPath() {
        return this.photoPath;
    }

    /*
    public static Photo parseContractMenu(String foodServiceName, Contract.Menu menu){
        return new Photo(foodServiceName, menu.getName(), menu.getPrice(), menu.getPhotoIdCount());
    }
     */
}
