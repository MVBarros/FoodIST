package pt.ulisboa.tecnico.cmov.foodist.data;

import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;

public class WalkingTimeData {
    private Double latitude;
    private Double longitude;
    private String apiKey;
    private String language;
    private List<FoodService> services;

    public WalkingTimeData(Double latitude, Double longitude, String apiKey, List<FoodService> services, String language) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.apiKey = apiKey;
        this.services = services;
        this.language = language;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getApiKey() {
        return apiKey;
    }

    public List<FoodService> getServices() {
        return services;
    }

    public String getLanguage() {
        return language;
    }

}
