package pt.ulisboa.tecnico.cmov.foodist.parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.foodist.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;


public class FoodServicesJsonParser {

    private static JSONObject readFile(InputStream is) throws IOException, JSONException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);
        return new JSONObject(responseStrBuilder.toString());
    }

    public static List<FoodService> parse(FoodServiceData resource) throws IOException, JSONException {
            JSONObject object = readFile(resource.getIs());
            JSONArray arr = object.getJSONArray(resource.getCampus());
            List<FoodService>  services = new ArrayList<>();
            for(int i = 0; i < arr.length(); ++i) {
              FoodService service = parseObject(arr.getJSONObject(i));
              services.add(service);
            }
            return services;
    }

    private static FoodService parseObject(JSONObject object) throws JSONException {
        String name = object.getString("name");
        String distance = object.getString("distance");
        String time = object.getString("time");
        double latitude = object.getDouble("latitude");
        double longitude = object.getDouble("longitude");
        Map<String, String> hours = new HashMap<>();
        JSONObject hourObject = object.getJSONObject("hours");
        hours.put("monday", hourObject.getString("monday"));
        hours.put("tuesday", hourObject.getString("tuesday"));
        hours.put("wednesday", hourObject.getString("wednesday"));
        hours.put("thursday", hourObject.getString("thursday"));
        hours.put("sunday", hourObject.getString("sunday"));
        List<String> restrictions = new ArrayList<String>();
        JSONArray arr = object.getJSONArray("restrictions");
        for(int i = 0; i < arr.length(); ++i) {
            restrictions.add(arr.getString(i));
        }
        return new FoodService(name, distance, time, latitude, longitude, hours, restrictions);
    }
}
