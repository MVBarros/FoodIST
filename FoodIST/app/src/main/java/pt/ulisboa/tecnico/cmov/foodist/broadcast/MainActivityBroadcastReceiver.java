package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import pt.ulisboa.tecnico.cmov.foodist.MainActivity;

public class MainActivityBroadcastReceiver extends BroadcastReceiver {

    private boolean wasNetworkAvailable = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkAvailable(context)) {
            if (!wasNetworkAvailable()) {
                MainActivity activity = (MainActivity) context;
                activity.updateServicesWalkingDistance();
            }
            wasNetworkAvailable = true;
        } else {
            wasNetworkAvailable = false;
        }

    }

    public boolean wasNetworkAvailable() {
        return wasNetworkAvailable;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
