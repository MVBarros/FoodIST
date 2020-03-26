package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.parse.FoodServicesJsonParser;

public class FoodServiceParsingTask extends AsyncTask<FoodServiceData, Integer, List<FoodService>> {

    private static final String TAG = "FOOD-SERVICE-PARSING";

    private WeakReference<MainActivity> mainActivity;

    public FoodServiceParsingTask(MainActivity mainActivity) {
        this.mainActivity = new WeakReference<>(mainActivity);
    }

    @Override
    protected List<FoodService> doInBackground(FoodServiceData... foodServiceData) {

        List<FoodService> services = null;
        try {
            if (foodServiceData.length != 1) {
                return null;
            }
            services = FoodServicesJsonParser.parse(foodServiceData[0]);
        } catch (IOException e) {
            Log.e(TAG, "Exception ocurred when opening or reading resource file");
        } catch (JSONException e) {
            Log.e(TAG, "Json file is mal formed");
        }

        return services;
    }

    @Override
    protected void onPostExecute(List<FoodService> services) {
        if (services == null) {
            return;
        }
        MainActivity activity = mainActivity.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // activity is no longer valid, don't do anything!
            return;
        }
        activity.getGlobalStatus().setServices(services);
        activity.drawServices();
        //After it is done try to update walking distance
        activity.updateServicesWalkingDistance();
    }
}

