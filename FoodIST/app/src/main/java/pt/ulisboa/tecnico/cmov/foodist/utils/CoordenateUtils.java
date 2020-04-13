package pt.ulisboa.tecnico.cmov.foodist.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class CoordenateUtils {

    public static int getDistanceBetween(double latitude, double longitude, double otherLatitude, double otherLongitude, String apiKey) throws IOException, JSONException {

        String addr = getUrlForDirections(latitude, longitude, otherLatitude, otherLongitude, apiKey);

        URL url = new URL(addr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String response;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
        } finally {
            urlConnection.disconnect();
        }
        JSONObject object = new JSONObject(response);
        //If response is invalid, an exception is thrown which we then catch
        return object.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance").getInt("value");
    }

    public static int getDistance(String addr) throws IOException, JSONException {

        URL url = new URL(addr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String response;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
        } finally {
            urlConnection.disconnect();
        }
        JSONObject object = new JSONObject(response);
        //If response is invalid, an exception is thrown which we then catch
        return object.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance").getInt("value");
    }


    public static String getWalkingTimeTo(double latitude, double longitude, double otherLatitude, double otherLongitude, String apiKey) throws IOException, JSONException {

        String addr = getUrlForDirections(latitude, longitude, otherLatitude, otherLongitude, apiKey);

        URL url = new URL(addr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String response;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
        } finally {
            urlConnection.disconnect();
        }
        JSONObject object = new JSONObject(response);
        //If response is invalid, an exception is thrown which we then catch
        return object.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
    }

    public static String getUrlForDistance(double latitude, double longitude, double destLatitude, double destLongitude, String apiKey) {
        String origin = String.format(Locale.ENGLISH, "%f,%f", latitude, longitude);
        String destination = String.format(Locale.ENGLISH, "%f,%f", destLatitude, destLongitude);
        return getUrlForDistance(origin, destination, apiKey);
    }

    public static String getUrlForDistance(String origin, String destination, String apiKey) {
        return "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin + "&destinations=" + destination + "&key=" + apiKey;
    }

    public static String getUrlForDistance(Location location, String destination, String apiKey) {
        return getUrlForDistance(String.format(Locale.ENGLISH, "%f,%f", location.getLatitude(), location.getLongitude()), destination, apiKey);
    }

    public static String getUrlForDirections(double latitude, double longitude, double destLatitude, double destLongitude, String apiKey) {
        String origin = String.format(Locale.ENGLISH, "%f,%f", latitude, longitude);
        String destination = String.format(Locale.ENGLISH, "%f,%f", destLatitude, destLongitude);
        return getUrlForDirections(origin, destination, apiKey);
    }

    public static String getUrlForDirections(LatLng org, LatLng dest, String apiKey) {
        return getUrlForDirections(org.latitude, org.longitude, dest.latitude, dest.longitude, apiKey);
    }

    public static String getUrlForDirections(String origin, String destination, String apiKey) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&key=" + apiKey + "&mode=walking&alternatives=false";
    }

    public static String getUrlForDirections(Location location, String destination, String apiKey) {
        return getUrlForDirections(String.format(Locale.ENGLISH, "%f,%f", location.getLatitude(), location.getLongitude()), destination, apiKey);
    }

    private static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

}
