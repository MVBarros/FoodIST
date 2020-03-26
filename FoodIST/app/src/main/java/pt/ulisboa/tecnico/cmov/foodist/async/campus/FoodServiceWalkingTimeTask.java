package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.data.WalkingTimeData;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;
import pt.ulisboa.tecnico.cmov.foodist.utils.CoordenateUtils;

public class FoodServiceWalkingTimeTask extends AsyncTask<WalkingTimeData, Integer, List<FoodService>> {
    private static final String TAG = "FOOD-SERVICE-WALKING-TIME-TASK";

    private WeakReference<MainActivity> mainActivity;
    public FoodServiceWalkingTimeTask(MainActivity activity) {
        this.mainActivity = new WeakReference<>(activity);
    }

    @Override
    protected List<FoodService> doInBackground(WalkingTimeData... walkingTimeResources) {
        if (walkingTimeResources == null || walkingTimeResources.length != 1) {
            return null;
        }
        WalkingTimeData resource = walkingTimeResources[0];
        Double latitude = resource.getLatitude();
        Double longitude = resource.getLongitude();
        if (latitude == null || longitude == null) {
            return null;
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
        return services;
    }

    @Override
    protected void onPostExecute(List<FoodService> result) {
        MainActivity activity = mainActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // activity is no longer valid, don't do anything!
            return;
        }
        if (result != null) {
            GlobalStatus status = activity.getGlobalStatus();
            status.updateServicesDistance(result);
            activity.drawServices();
        }
    }
}
