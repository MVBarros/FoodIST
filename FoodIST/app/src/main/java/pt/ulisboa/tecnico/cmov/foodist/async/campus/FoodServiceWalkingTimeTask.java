package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.data.WalkingTimeData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.utils.CoordenateUtils;

public class FoodServiceWalkingTimeTask extends AsyncTask<WalkingTimeData, Integer, Boolean> {
    private static final String TAG = "TAG_FoodServiceWalkingTimeTask";

    private WeakReference<MainActivity> mainActivity;

    public FoodServiceWalkingTimeTask(MainActivity activity) {
        this.mainActivity = new WeakReference<>(activity);
    }

    @Override
    protected Boolean doInBackground(WalkingTimeData... walkingTimeResources) {
        if (walkingTimeResources == null || walkingTimeResources.length != 1) {
            return null;
        }
        WalkingTimeData resource = walkingTimeResources[0];
        Double latitude = resource.getLatitude();
        Double longitude = resource.getLongitude();
        if (latitude == null || longitude == null) {
            return false;
        }
        String apiKey = resource.getApiKey();
        List<FoodService> services = resource.getServices();

        for (FoodService service : services) {
            try {
                double serviceLatitude = service.getLatitude();
                double serviceLongitude = service.getLongitude();
                String walkingTime = CoordenateUtils.getWalkingTimeTo(latitude, longitude, serviceLatitude, serviceLongitude, apiKey);
                service.setDistance(walkingTime);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Unable to get walking distance to service " + service.getName() + " due to cause: " + e.getCause());
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        MainActivity activity = mainActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // activity is no longer valid, don't do anything!
            return;
        }
        if (result) {
            //Services of global status are now updated, just need to draw them
            // (If they have been overridden nothing new will happen)
            activity.drawServices();
        }
    }
}
