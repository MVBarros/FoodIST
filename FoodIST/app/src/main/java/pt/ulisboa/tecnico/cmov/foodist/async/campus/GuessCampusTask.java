package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;


public class GuessCampusTask extends AsyncTask<String, Integer, int[]> {


    private WeakReference<MainActivity> mainActivity;

    public GuessCampusTask(MainActivity mainActivity) {
        this.mainActivity = new WeakReference<>(mainActivity);
    }

    private static final int NUMBER_CAMPUS = 2;
    private static final int ALAMEDA = 0;

    private static final String TAG = "LOCATION-TASK";

    @Override
    protected int[] doInBackground(String... strings) {

        if (strings.length != NUMBER_CAMPUS) {
            return null;
        }

        JSONObject[] object = new JSONObject[NUMBER_CAMPUS];
        try {
            String response;
            for (int i = 0; i < NUMBER_CAMPUS; ++i) {
                URL url = new URL(strings[i]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    response = readStream(in);
                } finally {
                    urlConnection.disconnect();
                }
                object[i] = new JSONObject(response);
            }
            int[] res = new int[NUMBER_CAMPUS];

            for (int i = 0; i < NUMBER_CAMPUS; ++i) {
                JSONArray array = object[i].getJSONArray("rows");
                JSONObject obj = array.getJSONObject(0);
                array = obj.getJSONArray("elements");
                obj = array.getJSONObject(0);
                obj = obj.getJSONObject("distance");

                int distance = obj.getInt("value");
                res[i] = distance;
            }
            return res;
        } catch (IOException | JSONException e) {
            Log.d(TAG, "Error getting location from google API", e);
            return null;
        }
    }


    @Override
    protected void onPostExecute(int[] res) {
        MainActivity activity = mainActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // activity is no longer valid, don't do anything!
            return;
        }
        if (res != null) {
            for (int i = 0; i < NUMBER_CAMPUS; ++i) {
                int distance = res[i];
                Log.d(TAG, "Distance to".concat(i == ALAMEDA ? "Alameda" : "Taguspark") + ": " + distance);
                if (distance < 2000) {
                    Log.d(TAG, "Location should be: ".concat(i == ALAMEDA ? "Alameda" : "TagusPark"));
                    activity.setCampus(i == ALAMEDA ? "Alameda" : "TagusPark");
                    return;
                }
            }
        } else {
            //Could not infer campus
            activity.askCampus();
        }

    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }
}
