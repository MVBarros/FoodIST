package pt.ulisboa.tecnico.cmov.foodist.async.main;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.async.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.parse.FoodServicesJsonParser;

public class ServiceParsingTask extends BaseAsyncTask<FoodServiceData, Integer, List<FoodService>, MainActivity> {

    private static final String TAG = "FOOD-SERVICE-PARSING";

    public ServiceParsingTask(MainActivity activity) {
        super(activity);
    }


    @Override
    public List<FoodService> doInBackground(FoodServiceData... foodServiceData) {
        if (foodServiceData.length != 1) {
            return null;
        }
        try {
            return FoodServicesJsonParser.parse(foodServiceData[0]);
        } catch (IOException e) {
            Log.e(TAG, "Exception ocurred when opening or reading resource file");
        } catch (JSONException e) {
            //Should never happen
            Log.e(TAG, "Json file is mal formed");
        }
        return null;
    }

    @Override
    public void onPostExecute(List<FoodService> services) {
        if (services != null) {
            getActivity().getGlobalStatus().setServices(services);
            getActivity().drawServices();
            //After it is done try to update walking distance
            getActivity().updateServicesWalkingDistance();
        }
    }
}

