package pt.ulisboa.tecnico.cmov.foodist;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.async.CancelableAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public abstract class BaseActivity extends AppCompatActivity {
    private Set<CancelableAsyncTask> tasks = Collections.synchronizedSet(new HashSet<>());

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

    public void add(CancelableAsyncTask task) {
        tasks.add(task);
    }

    public void remove(CancelableAsyncTask task) {
        tasks.remove(task);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTasks();
    }
}
