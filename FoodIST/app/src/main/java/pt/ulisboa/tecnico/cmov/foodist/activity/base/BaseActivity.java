package pt.ulisboa.tecnico.cmov.foodist.activity.base;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.broadcast.CacheNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public abstract class BaseActivity extends AppCompatActivity {
    private Set<AsyncTask> tasks = Collections.synchronizedSet(new HashSet<>());
    private Set<BroadcastReceiver> receivers = new HashSet<>();

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public GlobalStatus getGlobalStatus() {
        return (GlobalStatus) getApplicationContext();
    }

    private void cancelTasks() {
        tasks.forEach(task -> task.cancel(true));
    }

    private void cancelReceivers() {
        receivers.forEach(this::unregisterReceiver);
        receivers = new HashSet<>();
    }

    public void addTask(AsyncTask task) {
        tasks.add(task);
    }

    public void removeTask(AsyncTask task) {
        tasks.remove(task);
    }

    public void addReceiver(BroadcastReceiver receiver, String content, String... intents) {
        IntentFilter filter = new IntentFilter(content);
        Arrays.stream(intents).forEach(intent -> filter.addAction(getPackageName() + intent));

        registerReceiver(receiver, filter);
        receivers.add(receiver);
    }

    public void addReceivers() {
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addReceivers();
        addReceiver(new CacheNetworkReceiver(), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION );
    }

}
