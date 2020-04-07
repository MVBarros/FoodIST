package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.ulisboa.tecnico.cmov.foodist.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;

public class MainNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "MAIN-ACTIVITY-NETWORK-RECEIVER";

    public MainNetworkReceiver() {
        super();
    }

    @Override
    protected void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "On Network Up");
        MainActivity activity = (MainActivity) context;
        activity.updateServicesWalkingDistance();
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "On Network Down");
    }
}
