package pt.ulisboa.tecnico.cmov.foodist.async;

import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.utils.CoordenateUtils;


public class GuessCampusTask extends CancelableAsyncTask<String, Integer, String, MainActivity> {

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
    protected void safeRunOnUiThread(String result, MainActivity activity) {
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
