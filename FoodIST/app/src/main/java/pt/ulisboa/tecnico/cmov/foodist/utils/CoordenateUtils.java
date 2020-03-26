package pt.ulisboa.tecnico.cmov.foodist.utils;

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

    public static int getDistanceBetween(double latitude, double longitude, double otherLatitude, double otherLongitude, String apiKey) {
        return 0;
    }

    public static String getWalkingTimeTo(double latitude, double longitude, double otherLatitude, double otherLongitude, String apiKey) throws IOException, JSONException {

        String common = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        String originCoords = String.format(Locale.ENGLISH, "%f,%f", latitude, longitude);
        String destinationCoords = String.format(Locale.ENGLISH, "%f,%f", otherLatitude, otherLongitude);

        String addr = common + originCoords + "&destination=" + destinationCoords + "&key=" + apiKey + "&mode=walking&alternatives=false";

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
        String time = object.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");
        return time;

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
