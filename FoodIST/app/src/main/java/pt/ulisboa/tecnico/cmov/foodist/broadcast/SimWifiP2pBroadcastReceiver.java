package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.queue.JoinQueueTask;
import pt.ulisboa.tecnico.cmov.foodist.async.queue.LeaveQueueTask;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private final String uuid;
    private final FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private final static String TAG = "WIFI-DIRECT-RECEIVER";
    private boolean isInQueue = false;
    private List<String> foodServiceNames;
    private String currentFoodService;

    public SimWifiP2pBroadcastReceiver(BaseActivity activity, List<String> foodServiceNames) {
        super();
        this.uuid = activity.getGlobalStatus().getUUID();
        this.stub = activity.getGlobalStatus().getStub();
        this.foodServiceNames = foodServiceNames;
    }

    //Network is always available for this broadcast receiver
    @Override
    public void onReceive(Context context, Intent intent) {
        SimWifiP2pDeviceList deviceInfo = (SimWifiP2pDeviceList) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);

        if (deviceInfo == null) {
            //Just in case
            Log.d(TAG, "Device info was null");
            return;
        }
        Set<String> foodServiceBeacons = deviceInfo.getDeviceList()
                .stream()
                .filter(Objects::nonNull)
                .map(device -> device.deviceName.replaceAll("_", " "))
                .filter(deviceName -> foodServiceNames.contains(deviceName))
                .collect(Collectors.toSet());

        if (foodServiceBeacons.isEmpty()) {
            if (isInQueue) {
                Log.d(TAG, "Left line of food service " + currentFoodService);
                isInQueue = false;
                new LeaveQueueTask(uuid, stub).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentFoodService);
            }
        } else {
            String foodService = foodServiceBeacons.iterator().next();
            if (!foodService.equals(currentFoodService)) {
                //Somehow moved to different foodService (probably disconnected my wifi)
                new LeaveQueueTask(uuid, stub).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentFoodService);
                isInQueue = false;
            }
            if (!isInQueue) {
                Log.d(TAG, "Entered line of food service " + foodService);
                currentFoodService = foodService;
                isInQueue = true;
                new JoinQueueTask(uuid, stub).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentFoodService);
            }
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
