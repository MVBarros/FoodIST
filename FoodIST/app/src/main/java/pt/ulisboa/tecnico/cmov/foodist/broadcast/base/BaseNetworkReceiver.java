package pt.ulisboa.tecnico.cmov.foodist.broadcast.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.HashMap;

public abstract class BaseNetworkReceiver extends BroadcastReceiver {
    private boolean wasNetworkAvailable = true;
    public boolean isFirst = true;

    HashMap<Button, OnClickListener> whenUp = new HashMap<>();
    HashMap<Button, OnClickListener> whenDown = new HashMap<>();


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
                    updateButtonsUp();
                } else {
                    onNetworkDown(context, intent);
                    updateButtonsDown();
                }
            }
            wasNetworkAvailable = isNetworkAvaliable;
        }
    }

    final void updateButtonsUp() {
        whenUp.keySet().forEach(button -> {
            button.setOnClickListener(whenUp.get(button));
        });
    }


    final void updateButtonsDown() {
        whenDown.keySet().forEach(button -> {
            button.setOnClickListener(whenDown.get(button));
        });
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
