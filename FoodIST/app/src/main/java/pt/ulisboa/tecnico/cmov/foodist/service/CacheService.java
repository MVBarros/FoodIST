package pt.ulisboa.tecnico.cmov.foodist.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import pt.ulisboa.tecnico.cmov.foodist.broadcast.CacheNetworkReceiver;

public class CacheService extends Service {

    private CacheNetworkReceiver receiver = new CacheNetworkReceiver();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION );
        filter.addAction(getPackageName() + WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(receiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }
}
