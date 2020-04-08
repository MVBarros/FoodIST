package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.protobuf.Empty;

import java.util.List;

import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.base.BaseNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;
import pt.ulisboa.tecnico.cmov.foodist.threads.WifiPreloadingThread;

public class CacheNetworkReceiver extends BaseNetworkReceiver {
    private final static String TAG = "CACHE-NETWORK-RECEIVER";

    private boolean wasNetworkAvailable = true;
    public boolean isFirst = true;

    private Thread cachePreloadingThread = null;
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
        try{
            List<String> photoIDs = stub.requestPhotoIDs(Empty.newBuilder().build()).getPhotoIDList();

            this.cachePreloadingThread = new Thread(new WifiPreloadingThread(stub, photoIDs));
            this.cachePreloadingThread.start();

        } catch (StatusRuntimeException e){
            Log.d(TAG, "Couldnt obtain photoIDs");
        }
    }


    @Override
    protected void onNetworkDown(Context context, Intent intent) {
        Log.d(TAG, "Wifi disconnected");

        if(this.cachePreloadingThread != null){
            this.cachePreloadingThread.interrupt();
            this.cachePreloadingThread = null;
        }

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
