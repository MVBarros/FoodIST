package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;

public class MenuNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "MENU-ACTIVITY-NETWORK-RECEIVER";

    public FoodMenuActivity activity;

    public MenuNetworkReceiver(FoodMenuActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "On Network Up");
        activity.launchUpdateMenuTask();
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "On Network Down");
    }
}
