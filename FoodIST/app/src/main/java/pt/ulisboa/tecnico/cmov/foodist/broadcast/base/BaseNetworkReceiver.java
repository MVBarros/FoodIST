package pt.ulisboa.tecnico.cmov.foodist.broadcast.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Button;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseNetworkReceiver extends BroadcastReceiver {
    private boolean wasNetworkAvailable = true;
    public boolean isFirst = true;

    Set<Button> buttons;


    public BaseNetworkReceiver() {
        this.buttons = new HashSet<>();
    }

    public BaseNetworkReceiver(Set<Button> buttons) {
        this.buttons = buttons;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkAvailable = isNetworkAvailable(context);
        if (isFirst) {
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

        if (isNetworkAvailable) {
            updateButtonsUp();
        } else {
            updateButtonsDown();
        }
        wasNetworkAvailable = isNetworkAvailable;
    }

    final void updateButtonsUp() {
        buttons.forEach(button -> button.setClickable(true));
        buttons.forEach(button -> button.setAlpha(1f));
    }


    final void updateButtonsDown() {
        buttons.forEach(button -> button.setClickable(false));
        buttons.forEach(button -> button.setAlpha(.5f));
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
