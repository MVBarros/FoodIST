package pt.ulisboa.tecnico.cmov.foodist.domain;

import java.util.List;
import java.util.Map;

public class FoodService {
    private String name;
    private String distance;
    private String time;
    private double latitude;
    private double longitude;
    private Map<String, String> hours;
    private List<String> restrictions;

    public FoodService(String name, String distance, String time, double latitude,
                        double longitude, Map<String, String> hours, List<String> restictions) {
        this.name = name;
        this.distance = distance;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hours = hours;
        this.restrictions = restictions;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public String getTime() {
        return time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Map<String, String> getHours() {
        return hours;
    }

    public List<String> getRestrictions() {
        return restrictions;
    }

}
