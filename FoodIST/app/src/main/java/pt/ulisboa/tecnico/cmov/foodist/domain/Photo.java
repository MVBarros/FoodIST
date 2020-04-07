package pt.ulisboa.tecnico.cmov.foodist.domain;

public class Photo {
    private String foodServiceName;
    private String menuName;
    private String photoPath;
    //For downloads
    private String photoID;

    //To download photos from the server
    public Photo(String foodServiceName, String menuName, String photoPath, String photoID) {
        this(foodServiceName, menuName, photoPath);
        this.photoID = photoID;
    }

    //To send photos to the server
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

    public String getPhotoID() {
        return this.photoID;
    }
    /*
    public static Photo parseContractMenu(String foodServiceName, Contract.Menu menu){
        return new Photo(foodServiceName, menu.getName(), menu.getPrice(), menu.getPhotoIdCount());
    }
     */
}
