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

    @Override
    public void onReceive(Context context, Intent intent) {
        SimWifiP2pDeviceList deviceInfo = (SimWifiP2pDeviceList) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);

        if (deviceInfo == null) {
            //Just in case
            Log.d(TAG, "Device info was null");
            return;
        }
        List<String> foodServiceBeacons = deviceInfo.getDeviceList()
                .stream()
                .filter(Objects::nonNull)
                .map(device -> device.deviceName.replaceAll("_", " "))
                .filter(deviceName -> foodServiceNames.contains(deviceName))
                .collect(Collectors.toList());

        if (foodServiceBeacons.size() > 0) {
            if (!isInQueue) {
                Log.d(TAG, "Entered line of food service " + foodServiceBeacons.get(0));
                if (isNetworkAvailable(context)) {
                    currentFoodService = foodServiceBeacons.get(0);
                    Log.d(TAG, "Internet! Telling server to add me to queue " + currentFoodService);

                    isInQueue = true;
                    new JoinQueueTask(uuid, stub).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentFoodService);
                } else {
                    Log.d(TAG, "No internet, could not tell server to add me to queue " + foodServiceBeacons.get(0));
                }

            }
        } else {
            if (isInQueue) {
                Log.d(TAG, "Left line of food service");
                isInQueue = false;
                if (isNetworkAvailable(context)) {
                    Log.d(TAG, "Internet! Telling server to remove from the queue for service " + currentFoodService);
                    new LeaveQueueTask(uuid, stub).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currentFoodService);
                } else {
                    Log.d(TAG, "No internet, could not tell server to remove me from queue to service " + currentFoodService);
                }
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
