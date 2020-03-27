package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.utils.CoordenateUtils;


public class GuessCampusTask extends BaseAsyncTask<String, Integer, String, MainActivity> {

    public GuessCampusTask(MainActivity activity) {
        super(activity);
    }

    private static final int NUMBER_CAMPUS = 2;
    private static final int ALAMEDA = 0;

    private static final String TAG = "LOCATION-TASK";

    @Override
    protected String doInBackground(String... strings) {
        if (strings.length != NUMBER_CAMPUS) {
            return null;
        }
        try {
            for (int i = 0; i < NUMBER_CAMPUS; ++i) {
                int distance = CoordenateUtils.getDistance(strings[i]);
                if (distance < 2000) {
                    return i == ALAMEDA ? "Alameda" : "TagusPark";
                }
            }
            return null;
        } catch (IOException | JSONException e) {
            Log.d(TAG, "Error getting location from google API", e);
            return null;
        }
    }

    @Override
    void safeRunOnUiThread(String result, MainActivity activity) {
        if (result != null) {
            activity.setCampus(result);
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
