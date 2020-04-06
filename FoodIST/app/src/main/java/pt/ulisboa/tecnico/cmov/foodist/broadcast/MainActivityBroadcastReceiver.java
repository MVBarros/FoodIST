package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;

public class MainActivityBroadcastReceiver extends BaseNetworkReceiver {
    private final static String TAG = "MAIN-ACTIVITY-BROADCAST-RECEIVER";

    @Override
    void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "On Network Up");
        MainActivity activity = (MainActivity) context;
        activity.updateServicesWalkingDistance();
    }

    @Override
    void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "On Network Down");
    }

}
