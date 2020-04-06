package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class BaseNetworkReceiver extends BroadcastReceiver {
    private boolean wasNetworkAvailable = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkAvaliable = isNetworkAvailable(context);
        if (isNetworkAvaliable != wasNetworkAvailable) {
            if (isNetworkAvaliable) {
                onNetworkUp(context, intent);
            }
            else {
                onNetworkDown(context, intent);
            }
        }
        wasNetworkAvailable = isNetworkAvaliable;
    }

    abstract void onNetworkUp(Context context, Intent intent);
    abstract void onNetworkDown(Context context, Intent intent);

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
