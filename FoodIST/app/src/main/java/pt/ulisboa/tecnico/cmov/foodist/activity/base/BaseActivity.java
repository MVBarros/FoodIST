package pt.ulisboa.tecnico.cmov.foodist.activity.base;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Messenger;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.PreLoadingNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public abstract class BaseActivity extends AppCompatActivity {


    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
        }
    };

    private Set<AsyncTask> tasks = Collections.synchronizedSet(new HashSet<>());
    private Set<BroadcastReceiver> receivers = new HashSet<>();
    private SimWifiP2pBroadcastReceiver receiver;

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLoggedIn() {
        return getGlobalStatus().isLoggedIn();
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

    public static Context setLocale(Context context) {
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.profile_file), 0);

        Locale locale = new Locale(pref.getString(context.getString(R.string.shared_prefs_profile_language), "en"));
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(setLocale(base));
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
        unregisterReceiver(receiver);
        unbindService(mConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addReceivers();
        setWifiDirectListener();
        addReceiver(new PreLoadingNetworkReceiver(), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    public String timeString(Long time) {
        String res = "";
        if (time > (3600 * 24)) {
            long days = time / (3600 * 24);
            res += Long.toString(days);
            String suffix = days == 1 ? getString(R.string.day) : getString(R.string.days);
            res += " " + suffix + " ";
            time = time % (3600 * 24);
        }
        if (time > 3600) {
            long hours = time / 3600;
            res += Long.toString(hours);
            String suffix = hours == 1 ? getString(R.string.hour) : getString(R.string.hours);
            res += " " + suffix + " ";
            time = time % 3600;
        }
        if (time > 60) {
            long minutes = time / 60;
            res += Long.toString(minutes);
            String suffix = minutes == 1 ? getString(R.string.minute) : getString(R.string.minutes);
            res += " " + suffix + " ";
            time = time % 60;
        }
        res += Long.toString(time);
        String suffix = time == 1 ? getString(R.string.second) : getString(R.string.seconds);
        res += " " + suffix;
        return res;
    }


    public void setWifiDirectListener(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        receiver = SimWifiP2pBroadcastReceiver.getInstance(this);
        registerReceiver(receiver, filter);
    }
}
