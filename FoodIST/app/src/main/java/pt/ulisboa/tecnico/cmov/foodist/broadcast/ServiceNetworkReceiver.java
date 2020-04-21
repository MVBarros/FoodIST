package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.activity.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;

public class ServiceNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "SERVICE-ACTIVITY-NETWORK-RECEIVER";

    private FoodServiceActivity activity;

    public ServiceNetworkReceiver(Set<Button> buttons) {
        super(buttons);
    }

    public ServiceNetworkReceiver(FoodServiceActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    protected void onNetworkUp(Context context, Intent intent) {
        Log.d(TAG, "On Network Up");
        activity.updateMenus();
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "On Network Down");
    }

}
