package pt.ulisboa.tecnico.cmov.foodist.domain;

public class Photo {
    private String menuId;
    private String photoPath;
    //For downloads
    private String photoID;

    //To download photos from the server
    public Photo(String menuId,  String photoPath, String photoID) {
        this(menuId, photoPath);
        this.photoID = photoID;
    }

    //To send photos to the server
    public Photo(String menuId,  String photoPath) {
        this.menuId = menuId;
        this.photoPath = photoPath;
    }


    public String getMenuId() {
        return this.menuId;
    }

    public String getPhotoPath() {
        return this.photoPath;
    }

    public String getPhotoID() {
        return this.photoID;
    }
}
