package pt.ulisboa.tecnico.cmov.foodist.parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.async.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;


public class FoodServicesJsonParser {

    private static final String[] WEEK_DAYS = new String[]{"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

    private static JSONObject readFile(InputStream is) throws IOException, JSONException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);
        return new JSONObject(responseStrBuilder.toString());
    }

    public static Map<String, List<FoodService>> parse(FoodServiceData resource) throws IOException, JSONException {
        JSONObject object = readFile(resource.getIs());
        Map<String, List<FoodService>> serviceMap = new HashMap<>();
        for (Iterator<String> it = object.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONArray arr = object.getJSONArray(key);
            List<FoodService> services = new ArrayList<>();
            for (int i = 0; i < arr.length(); ++i) {
                FoodService service = parseObject(arr.getJSONObject(i));
                services.add(service);
            }
            serviceMap.put(key, services);
        }
        return serviceMap;
    }

    private static FoodService parseObject(JSONObject object) throws JSONException {
        Map<String, String> names = new HashMap<>();

        names.put("en", object.getString("en"));
        names.put("pt", object.getString("pt"));
        String distance = object.getString("distance");
        String time = object.getString("time");
        String beacon = object.getString("beacon");
        double latitude = object.getDouble("latitude");
        double longitude = object.getDouble("longitude");
        Map<String, Map<String, List<String>>> hours = parseHours(object);
        Set<String> constrains = parseConstrains(object);
        return new FoodService(names, distance, time, latitude, longitude, hours, constrains, beacon);
    }

    private static Map<String, Map<String, List<String>>> parseHours(JSONObject object) throws JSONException {
        Map<String, Map<String, List<String>>> hours = new HashMap<>();
        JSONObject hourObject = object.getJSONObject("hours");
        for (Iterator<String> it = hourObject.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject keyHoursObject = hourObject.getJSONObject(key);
            Map<String, List<String>> hourMap = new HashMap<>();
            for (String day : WEEK_DAYS) {
                JSONArray array = keyHoursObject.getJSONArray(day);
                List<String> hourList = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    hourList.add(array.getString(i));
                }
                hourMap.put(day, hourList);
            }
            hours.put(key, hourMap);
        }
        return hours;
    }

    private static  Set<String> parseConstrains(JSONObject object) throws JSONException {
        Set<String> constrains = new HashSet<>();
        JSONArray array = object.getJSONArray("constraints");
        for (int i = 0; i < array.length(); i++) {
            constrains.add(array.getString(i));
        }
        return constrains;
    }
}
