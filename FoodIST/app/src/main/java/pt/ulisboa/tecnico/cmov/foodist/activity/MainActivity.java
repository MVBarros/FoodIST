package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.GuessCampusTask;
import pt.ulisboa.tecnico.cmov.foodist.async.ServiceParsingTask;
import pt.ulisboa.tecnico.cmov.foodist.async.ServiceWalkingTimeTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.SafePostTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.MainNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.data.WalkingTimeData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;
import pt.ulisboa.tecnico.cmov.foodist.utils.CoordenateUtils;


public class MainActivity extends BaseActivity implements LocationListener {
    public enum LocationRequestContext {CAMPUS, DISTANCE}

    private LocationManager mLocationManager;

    private static final int PHONE_LOCATION_REQUEST_CODE = 1;

    private static final String SERVICE_NAME = "Service Name";
    private static final String DISTANCE = "Distance";
    private static final String QUEUE_TIME = "Queue time";
    private static final String SERVICE_HOURS = "Service Hours";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";

    private static final long MAX_TIME = 1000 * 60; // 1 Minute in milliseconds

    private boolean isOnCreate;

    private LocationRequestContext reqContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isOnCreate = true;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setButtons();
        setCurrentCampus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isOnCreate) {
            drawServices();
            updateServicesWalkingDistance();
        }
        isOnCreate = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PHONE_LOCATION_REQUEST_CODE) {
            loadCurrentCampus();
        }
    }

    public void addReceivers() {
        addReceiver(new MainNetworkReceiver(), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    private void updateFirstBoot() {
        boolean isFreshBoot;
        if (getGlobalStatus().isFreshBootFlag()) {
            getGlobalStatus().setFreshBootFlag(false);
            isFreshBoot = true;
        } else {
            isFreshBoot = false;
        }
    }

    private void setButtons() {
        Button userProfileButton = findViewById(R.id.userProfile);
        userProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        Button changeCampusButton = findViewById(R.id.changeCampus);
        changeCampusButton.setOnClickListener(v -> {
            askCampus();
        });
    }

    private void setCurrentCampus() {
        Intent intent = getIntent();
        String campus = intent.getStringExtra(ChooseCampusActivity.CAMPUS);

        if (campus != null) {
            //Know Current Location from previous user choice
            //(means we come from ChooseCampusActivity)
            setCampus(campus);
        } else {
            //No previous choice defined, try to guess and if not able to guess manually ask for it
            if (hasLocationPermission()) {
                //Have permission to access location, try and guess campus
                loadCurrentCampus();
            } else {
                askLocationPermission();
            }
        }
    }

    private void askLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PHONE_LOCATION_REQUEST_CODE);
    }

    private void updateServicesFromLocation(Location location) {
        GlobalStatus status = getGlobalStatus();
        List<FoodService> services = new ArrayList<>(status.getServices());
        String apiKey = status.getApiKey();
        WalkingTimeData data = new WalkingTimeData(location.getLatitude(), location.getLongitude(), apiKey, services);
        launchWalkingTimeTask(data);
    }

    @SuppressLint("MissingPermission")
    public void updateServicesWalkingDistance() {
        if (hasLocationPermission() && isNetworkAvailable()) {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null && location.getTime() >= Calendar.getInstance().getTimeInMillis() - MAX_TIME) {
                //We trust this location to be recent enough that the user's location has not changed that much
                distanceLocationCallback(location);
            } else {
                //Request new location since the last known location is too old
                reqContext = LocationRequestContext.DISTANCE;
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        } else {
            showToast("Could not get services walking distance as there is no location permission and/or noInternet");
        }
    }


    private void launchWalkingTimeTask(WalkingTimeData data) {
        new CancelableTask<>(new SafePostTask<>(new ServiceWalkingTimeTask(this))).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
    }

    private void launchGuessCampusTask(String... urls) {
        new CancelableTask<>(new SafePostTask<>(new GuessCampusTask(this))).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urls);
    }

    private void updateCampusFromLocation(Location location) {
        String apiKey = getGlobalStatus().getApiKey();
        String urlAlameda = CoordenateUtils.getUrlForDistance(location, getString(R.string.map_alameda), apiKey);
        String urlTagus = CoordenateUtils.getUrlForDistance(location, getString(R.string.map_taguspark), apiKey);
        launchGuessCampusTask(urlAlameda, urlTagus);
    }

    @SuppressLint("MissingPermission")
    private void guessCampusFromLocation() {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null && location.getTime() >= Calendar.getInstance().getTimeInMillis() - MAX_TIME) {
            //We trust this location to be recent enough that the user's location has not changed that much
            campusLocationCallback(location);
        } else {
            reqContext = LocationRequestContext.CAMPUS;
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    private void loadCurrentCampus() {
        if (hasLocationPermission() && isNetworkAvailable()) {
            guessCampusFromLocation();
        } else {
            Toast.makeText(getApplicationContext(), "Could not infer campus as no network and/or location is avaliable, please insert it manually", Toast.LENGTH_SHORT).show();
            askCampus();
        }
    }

    public void setCampus(String campus) {
        //Update Interface
        TextView textView = findViewById(R.id.currentCampus);
        textView.setText(campus);

        //Save preferences for later
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.global_preferences_file), 0).edit();
        editor.putString(getString(R.string.global_preferences_location), campus);
        editor.apply();
        loadServices(campus);
    }

    private void drawService(FoodService service) {
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.food_service, null);

        TextView name = v.findViewById(R.id.foodServiceName);
        name.setText(service.getName());

        TextView distance = v.findViewById(R.id.distance);
        distance.setText(String.format("Walking Time: %s", service.getDistance()));

        TextView queue = v.findViewById(R.id.queueTime);
        queue.setText(String.format("Queue Time: %s", service.getTime()));

        v.setOnClickListener(v1 -> {
            Intent intent = new Intent(MainActivity.this, FoodServiceActivity.class);
            TextView name1 = v1.findViewById(R.id.foodServiceName);
            intent.putExtra(SERVICE_NAME, name1.getText());
            intent.putExtra(DISTANCE, service.getDistance());
            intent.putExtra(QUEUE_TIME, service.getTime());
            intent.putExtra(SERVICE_HOURS, service.getHoursForToday());
            intent.putExtra(LATITUDE, service.getLatitude());
            intent.putExtra(LONGITUDE, service.getLongitude());
            startActivity(intent);
        });

        ViewGroup foodServiceList = findViewById(R.id.foodServices);
        foodServiceList.addView(v);

    }

    public void drawServices() {
        ViewGroup foodServiceList = findViewById(R.id.foodServices);
        foodServiceList.removeAllViews();
        getAvailableServices().forEach(this::drawService);
    }

    public void askCampus() {
        Intent intent = new Intent(MainActivity.this, ChooseCampusActivity.class);
        //Must Finish Activity so that user doesn't back out of choosing campus and leaves the application without campus
        finish();
        startActivity(intent);
    }

    private void loadServices(String campus) {
        InputStream is = getResources().openRawResource(R.raw.services);
        FoodServiceData resource = new FoodServiceData(is, campus);
        launchFoodServiceParseTask(resource);
    }

    private void launchFoodServiceParseTask(FoodServiceData resource) {
        new CancelableTask<>((new SafePostTask<>(new ServiceParsingTask(this)))).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, resource);
    }

    public List<FoodService> getAvailableServices() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        String position = pref.getString(getString(R.string.position_name), "");

        return getGlobalStatus().getServices().stream()
                .filter(service -> !service.getRestrictions().contains(position))
                .filter(FoodService::isFoodServiceAvailable)
                .collect(Collectors.toList());
    }

    private void distanceLocationCallback(Location location) {
        if (location != null) {
            Log.v("Location Result for Distance", location.getLatitude() + " and " + location.getLongitude());
            updateServicesFromLocation(location);
        } else {
            showToast("Could not get services walking distance");
        }
    }

    private void campusLocationCallback(Location location) {
        if (location != null) {
            Log.v("Location Result for Campus", location.getLatitude() + " and " + location.getLongitude());
            updateCampusFromLocation(location);
        } else {
            showToast("Could not infer campus from location");
            askCampus();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        switch (reqContext) {
            case DISTANCE:
                distanceLocationCallback(location);
                break;
            case CAMPUS:
                campusLocationCallback(location);
                break;
        }

        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //DO NOTHING
    }

    @Override
    public void onProviderEnabled(String provider) {
        //DO NOTHING
    }

    @Override
    public void onProviderDisabled(String provider) {
        //DO NOTHING
    }
}
