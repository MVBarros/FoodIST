package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.parse.FoodServiceResource;
import pt.ulisboa.tecnico.cmov.foodist.parse.FoodServicesJsonParser;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class FoodServiceParsingTask extends AsyncTask<FoodServiceResource, Integer, List<FoodService>> {

    private static final String TAG = "FOOD-SERVICE-PARSING";

    private WeakReference<MainActivity> mainActivity;

    public FoodServiceParsingTask(MainActivity mainActivity) {
        this.mainActivity = new WeakReference<>(mainActivity);
    }


    @Override
    protected List<FoodService> doInBackground(FoodServiceResource... foodServiceResources) {
        try {
            if (foodServiceResources.length != 1) {
                return null;
            }
            return FoodServicesJsonParser.parse(foodServiceResources[0]);
        } catch (IOException e) {
            Log.e(TAG, "Exception ocurred when opening or reading resource file");
        } catch (JSONException e) {
            Log.e(TAG, "Json file is mal formed");
        }
        return null;
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

        ((GlobalStatus)activity.getApplicationContext()).setServices(services);
        activity.writeServicesToUI(services);
        }
    }

