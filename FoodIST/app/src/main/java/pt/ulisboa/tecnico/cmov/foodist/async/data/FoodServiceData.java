package pt.ulisboa.tecnico.cmov.foodist.async.data;

import java.io.InputStream;

public class FoodServiceData {
    private InputStream is;
    private String campus;

    public FoodServiceData(InputStream is, String campus) {
        this.is = is;
        this.campus = campus;
    }


    public InputStream getIs() {
        return is;
    }

    public String getCampus() {
        return campus;
    }

}
