package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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


public class GuessCampusTask extends AsyncTask<String, Integer, JSONObject[]> {


    private WeakReference<MainActivity> mainActivity;

    public GuessCampusTask(MainActivity mainActivity) {
        this.mainActivity = new WeakReference<>(mainActivity);
    }

    private static final int NUMBER_CAMPUS = 2;
    private static final int ALAMEDA = 0;
    private static final int TAGUSPARK = 1;
    @Override
    protected JSONObject[] doInBackground(String... strings) {

        if (strings.length != NUMBER_CAMPUS) {
            return null;
        }

        JSONObject[] res = new JSONObject[NUMBER_CAMPUS];
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
                JSONObject object = new JSONObject(response);
                res[i] = object;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
        return res;
    }


    @Override
    protected void onPostExecute(JSONObject[] object) {
        if (object == null) {
            return;
        }
        try {
            for(int i = 0; i < NUMBER_CAMPUS; ++i) {
                JSONArray array = object[i].getJSONArray("rows");
                JSONObject obj = array.getJSONObject(0);
                array = obj.getJSONArray("elements");
                obj = array.getJSONObject(0);
                obj = obj.getJSONObject("distance");
                int distance = obj.getInt("value");
                Log.d("LOCATION", "Distance to alameda: " + distance);
                if (distance < 2000) {
                    Log.d("LOCATION", "Location should be: ".concat(i == ALAMEDA ? "Alameda" : "TagusPark"));
                    if (mainActivity != null) {
                        mainActivity.get().setCampus(i == ALAMEDA ? "Alameda" : "TagusPark");
                        return;
                    }
                    else {
                        return;
                    }
                }
            }
            //Could not infer campus
            if (mainActivity != null) {
                mainActivity.get().askCampus();
            }

        } catch (JSONException e) {
            e.printStackTrace();
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
