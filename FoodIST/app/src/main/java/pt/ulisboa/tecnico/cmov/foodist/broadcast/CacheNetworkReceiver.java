package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CacheNetworkReceiver extends BroadcastReceiver {
    private final static String TAG = "CACHE-NETWORK-RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Wifi changed");
    }
}
