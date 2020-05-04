package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.ulisboa.tecnico.cmov.foodist.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;

public class MainNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "MAIN-ACTIVITY-NETWORK-RECEIVER";
    private MainActivity activity;


    public MainNetworkReceiver(MainActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "On Network Up");
        activity.updateServicesWalkingDistance();
        activity.updateServicesQueueTime();
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "On Network Down");
    }
}
