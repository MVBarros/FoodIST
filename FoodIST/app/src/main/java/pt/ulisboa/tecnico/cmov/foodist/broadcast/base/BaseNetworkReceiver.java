package pt.ulisboa.tecnico.cmov.foodist.broadcast.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class BaseNetworkReceiver extends BroadcastReceiver {
    private boolean wasNetworkAvailable = true;
    public boolean isFirst = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isFirst) {
            isFirst = false;
            wasNetworkAvailable = isNetworkAvailable(context);
        } else {
            boolean isNetworkAvaliable = isNetworkAvailable(context);
            if (isNetworkAvaliable != wasNetworkAvailable) {
                if (isNetworkAvaliable) {
                    onNetworkUp(context, intent);
                } else {
                    onNetworkDown(context, intent);
                }
            }
            wasNetworkAvailable = isNetworkAvaliable;
        }
    }

    protected abstract void onNetworkUp(Context context, Intent intent);

    protected abstract void onNetworkDown(Context context, Intent intent);

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
