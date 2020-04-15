package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.activity.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.ActivityWithMap;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;

public class MapUpdateTask extends BaseNetworkReceiver  {
    private final static String TAG = "SERVICE-ACTIVITY-NETWORK-RECEIVER";


    public MapUpdateTask() {
        super();
    }

    public MapUpdateTask(Set<Button> buttons) {
        super(buttons);
    }

    @Override
    protected void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "On Network Up");
        ActivityWithMap activity = (ActivityWithMap) context;
        activity.startLocationUpdates();
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "On Network Down");
        ActivityWithMap activity = (ActivityWithMap) context;
        activity.stopLocationUpdates();
    }
}
