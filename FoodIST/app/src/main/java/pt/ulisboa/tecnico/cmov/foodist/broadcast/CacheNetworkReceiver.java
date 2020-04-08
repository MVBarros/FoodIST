package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Button;

import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;

public class CacheNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "CACHE-NETWORK-RECEIVER";

    private boolean wasNetworkAvailable = true;
    public boolean isFirst = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkAvailable = isNetworkAvailable(context);
        if (isFirst) {
            if (isNetworkAvailable) {
                onNetworkUp(context, intent);
            } else {
                onNetworkDown(context, intent);
            }
            isFirst = false;
        } else {
            if (isNetworkAvailable != wasNetworkAvailable) {
                if (isNetworkAvailable) {
                    onNetworkUp(context, intent);
                } else {
                    onNetworkDown(context, intent);
                }
            }
        }
        wasNetworkAvailable = isNetworkAvailable;
    }
    @Override
    protected void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "Wifi connected");
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "Wifi disconnected");

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
