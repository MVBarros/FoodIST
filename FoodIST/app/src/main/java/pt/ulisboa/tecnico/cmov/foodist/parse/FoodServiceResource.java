package pt.ulisboa.tecnico.cmov.foodist.parse;

import java.io.InputStream;

public class FoodServiceResource {
    private InputStream is;
    private String campus;

    public FoodServiceResource(InputStream is, String campus) {
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
