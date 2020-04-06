package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.ulisboa.tecnico.cmov.foodist.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;

public class ServiceNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "SERVICE-ACTIVITY-NETWORK-RECEIVER";

    @Override
    protected void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "On Network Up");
        FoodServiceActivity activity = (FoodServiceActivity) context;
        activity.updateMenus();
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "On Network Down");
    }

}
