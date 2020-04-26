package pt.ulisboa.tecnico.cmov.foodist.async;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.utils.CoordenateUtils;


public class GuessCampusTask extends BaseAsyncTask<LatLng, Integer, String, MainActivity> {

    private LatLng curr;

    public GuessCampusTask(MainActivity activity, LatLng curr) {
        super(activity);
        this.curr = curr;
    }

    private static final int NUMBER_CAMPUS = 2;
    private static final int ALAMEDA = 0;

    private static final String TAG = "LOCATION-TASK";

    private static final int MAX_DISTANCE = 2000; //2 kilometers

    @Override
    protected String doInBackground(LatLng... lngs) {
        if (lngs.length != NUMBER_CAMPUS) {
            return null;
        }
        for (int i = 0; i < NUMBER_CAMPUS; ++i) {
            int distance = CoordenateUtils.calculateDistance(curr, lngs[i]);
            if (distance < MAX_DISTANCE) {
                return i == ALAMEDA ? "Alameda" : "TagusPark";
            }
        }
        return null;

    }

    @Override
    public void onPostExecute(String result) {
        if (result != null) {
            getActivity().setCampus(result);
        } else {
            //Could not infer campus
            getActivity().showToast(getActivity().getString(R.string.guess_campus_task_impossible_message));
            getActivity().askCampus();
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }
}
