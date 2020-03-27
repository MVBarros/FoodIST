package pt.ulisboa.tecnico.cmov.foodist.async.campus;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.parse.FoodServicesJsonParser;

public class FoodServiceParsingTask extends BaseAsyncTask<FoodServiceData, Integer, List<FoodService>, MainActivity> {

    private static final String TAG = "FOOD-SERVICE-PARSING";

    public FoodServiceParsingTask(MainActivity activity) {
        super(activity);
    }

    @Override
    protected List<FoodService> doInBackground(FoodServiceData... foodServiceData) {
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
    void safeRunOnUiThread(List<FoodService> services, MainActivity activity) {
        activity.getGlobalStatus().setServices(services);
        activity.drawServices();
        //After it is done try to update walking distance
        activity.updateServicesWalkingDistance();
    }
}

