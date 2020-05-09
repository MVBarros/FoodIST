package pt.ulisboa.tecnico.cmov.foodist.async.main;

import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.foodist.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.async.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.parse.FoodServicesJsonParser;

public class ServiceParsingTask extends BaseAsyncTask<FoodServiceData, Integer, Map<String, List<FoodService>>, MainActivity> {

    private static final String TAG = "FOOD-SERVICE-PARSING";

    private String campus;
    public ServiceParsingTask(MainActivity activity) {
        super(activity);
    }


    @Override
    public Map<String, List<FoodService>> doInBackground(FoodServiceData... foodServiceData) {
        if (foodServiceData.length != 1) {
            return null;
        }
        try {
            campus = foodServiceData[0].getCampus();
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
    public void onPostExecute(Map<String, List<FoodService>> services) {
        if (services != null) {
            MainActivity.setHaveServicesBeenParsed();
            getActivity().getGlobalStatus().setServicesPerCampus(services);
            getActivity().getGlobalStatus().setServices(services.get(campus));
            getActivity().updateServices();

            List<FoodService> allServices = services.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            getActivity().setWifiDirectListener(allServices);
        }
    }
}

