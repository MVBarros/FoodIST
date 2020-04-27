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
import java.util.Iterator;
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
        List<FoodService> services = new ArrayList<>();
        for (int i = 0; i < arr.length(); ++i) {
            FoodService service = parseObject(arr.getJSONObject(i));
            services.add(service);
        }
        return services;
    }

    private static FoodService parseObject(JSONObject object) throws JSONException {
        Map<FoodService.Language, String> names = new HashMap<>();
        for (FoodService.Language lang : FoodService.Language.values()) {
            names.put(lang, object.getString(lang.name()));
        }
        String distance = object.getString("distance");
        String time = object.getString("time");
        double latitude = object.getDouble("latitude");
        double longitude = object.getDouble("longitude");
        Map<String, Map<String, String>> hours = parseHours(object);

        return new FoodService(names, distance, time, latitude, longitude, hours);
    }

    private static Map<String, Map<String, String>> parseHours(JSONObject object) throws JSONException {
        Map<String, Map<String, String>> hours = new HashMap<>();
        JSONObject hourObject = object.getJSONObject("hours");
        for (Iterator<String> it = hourObject.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject keyHoursObject = hourObject.getJSONObject(key);
            Map<String, String> hourMap = new HashMap<>();
            hourMap.put("monday", keyHoursObject.getString("monday"));
            hourMap.put("tuesday", keyHoursObject.getString("tuesday"));
            hourMap.put("wednesday", keyHoursObject.getString("wednesday"));
            hourMap.put("thursday", keyHoursObject.getString("thursday"));
            hourMap.put("friday", keyHoursObject.getString("friday"));
            hourMap.put("saturday", keyHoursObject.getString("saturday"));
            hourMap.put("sunday", keyHoursObject.getString("sunday"));
            hours.put(key, hourMap);
        }
        return hours;
    }
}
