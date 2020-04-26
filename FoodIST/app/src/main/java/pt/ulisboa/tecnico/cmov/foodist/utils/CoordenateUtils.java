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

    private final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

    public static int getDistanceBetween(double latitude, double longitude, double otherLatitude, double otherLongitude, String apiKey, String language) throws IOException, JSONException {

        String addr = getUrlForDirections(latitude, longitude, otherLatitude, otherLongitude, apiKey, language);

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


    public static String getWalkingTimeTo(double latitude, double longitude, double otherLatitude, double otherLongitude, String apiKey, String language) throws IOException, JSONException {

        String addr = getUrlForDirections(latitude, longitude, otherLatitude, otherLongitude, apiKey, language);

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

    public static String getUrlForDistance(double latitude, double longitude, double destLatitude, double destLongitude, String apiKey, String language) {
        String origin = String.format(Locale.ENGLISH, "%f,%f", latitude, longitude);
        String destination = String.format(Locale.ENGLISH, "%f,%f", destLatitude, destLongitude);
        return getUrlForDistance(origin, destination, apiKey, language);
    }

    public static String getUrlForDistance(String origin, String destination, String apiKey, String language) {
        return "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin + "&destinations=" + destination + "&key=" + apiKey + "&language=" + language;
    }

    public static String getUrlForDistance(Location location, String destination, String apiKey, String language) {
        return getUrlForDistance(String.format(Locale.ENGLISH, "%f,%f", location.getLatitude(), location.getLongitude()), destination, apiKey, language);
    }

    public static String getUrlForDirections(double latitude, double longitude, double destLatitude, double destLongitude, String apiKey, String language) {
        String origin = String.format(Locale.ENGLISH, "%f,%f", latitude, longitude);
        String destination = String.format(Locale.ENGLISH, "%f,%f", destLatitude, destLongitude);
        return getUrlForDirections(origin, destination, apiKey, language);
    }

    public static String getUrlForDirections(LatLng org, LatLng dest, String apiKey, String language) {
        return getUrlForDirections(org.latitude, org.longitude, dest.latitude, dest.longitude, apiKey, language);
    }

    public static String getUrlForDirections(String origin, String destination, String apiKey, String language) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&key=" + apiKey + "&mode=walking&alternatives=false" + "&language=" + language;
    }

    public static String getUrlForDirections(Location location, String destination, String apiKey, String language) {
        return getUrlForDirections(String.format(Locale.ENGLISH, "%f,%f", location.getLatitude(), location.getLongitude()), destination, apiKey, language);
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

    public static int calculateDistance(LatLng origin, LatLng dest) {

        double latDistance = Math.toRadians(origin.latitude - dest.latitude);
        double lngDistance = Math.toRadians(origin.longitude - dest.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(origin.latitude)) * Math.cos(Math.toRadians(dest.latitude))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c) * 1000);
    }

}
