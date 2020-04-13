package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.activity.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;

public class ServiceNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "SERVICE-ACTIVITY-NETWORK-RECEIVER";


    public ServiceNetworkReceiver() {
        super();
    }

    public ServiceNetworkReceiver(Set<Button> buttons) {
        super(buttons);
    }

    @Override
    protected void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "On Network Up");
        FoodServiceActivity activity = (FoodServiceActivity) context;
        activity.updateMenus();
        activity.startLocationUpdates();
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "On Network Down");
        FoodServiceActivity activity = (FoodServiceActivity) context;
        activity.stopLocationUpdates();
    }

}
