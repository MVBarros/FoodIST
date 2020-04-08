package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import pt.ulisboa.tecnico.cmov.foodist.async.WifiPreloadingTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class CacheNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "CACHE-NETWORK-RECEIVER";

    private static boolean wasNetworkAvailable = true;
    public static boolean isFirst = true;

    private static WifiPreloadingTask cachePreloadingTask = null;
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
        FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub = ((GlobalStatus) context.getApplicationContext()).getStub();
        this.cachePreloadingTask = new WifiPreloadingTask();
        this.cachePreloadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, stub);
    }

    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "Wifi disconnected");

        if(this.cachePreloadingTask != null){
            this.cachePreloadingTask.cancel(true);
            this.cachePreloadingTask = null;
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
