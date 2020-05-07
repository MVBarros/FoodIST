package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.boot.ChooseLanguageActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.SafePostTask;
import pt.ulisboa.tecnico.cmov.foodist.async.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.async.data.WalkingTimeData;
import pt.ulisboa.tecnico.cmov.foodist.async.main.GuessCampusTask;
import pt.ulisboa.tecnico.cmov.foodist.async.main.ServiceParsingTask;
import pt.ulisboa.tecnico.cmov.foodist.async.main.ServiceQueueTimeTask;
import pt.ulisboa.tecnico.cmov.foodist.async.main.ServiceWalkingTimeTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.MainNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.SimWifiP2pBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.CAMPUS;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.DISTANCE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.LATITUDE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.LONGITUDE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.QUEUE_TIME;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.SERVICE_DISPLAY_NAME;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.SERVICE_HOURS;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.SERVICE_NAME;


public class MainActivity extends BaseActivity implements LocationListener {
    public enum LocationRequestContext {CAMPUS, DISTANCE}

    private LocationManager mLocationManager;

    private static final int PHONE_LOCATION_REQUEST_CODE = 1;
    private static final long MAX_TIME = 1000 * 60; // 1 Minute in milliseconds
    private static final LatLng LOCATION_TAGUS = new LatLng(38.737050, -9.302734);
    private static final LatLng LOCATION_ALAMEDA = new LatLng(38.736819, -9.138769);
    private static final LatLng LOCATION_CTN = new LatLng(38.811936, -9.094336);

    private boolean isOnCreate;
    private boolean useConstraints = true;

    private LocationRequestContext reqContext;

    private String campus;

    private SimWifiP2pBroadcastReceiver mReceiver;
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    //Dont think we need Bound but oh well

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isOnCreate = true;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setButtons();
        setLanguage();
        setCurrentCampus();

    }

    public void setWifiDirectListener(List<FoodService> services) {
        List<String> foodServiceName = services.stream()
                .map(FoodService::getName)
                .collect(Collectors.toList());

        Log.d("TAG", "NumberFoodService: " + foodServiceName.size());
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this, foodServiceName);
        registerReceiver(mReceiver, filter);

        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isOnCreate) {
            drawServices();
            updateServicesWalkingDistance();
            updateServicesQueueTime();
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
        addReceiver(new MainNetworkReceiver(this), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    private void setButtons() {
        Button userProfileButton = findViewById(R.id.userProfile);
        userProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra(CAMPUS, campus);
            startActivity(intent);
        });

        Button changeCampusButton = findViewById(R.id.changeCampus);
        changeCampusButton.setOnClickListener(v -> {
            toCampus();
        });
    }

    private void setLanguage() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);

        if (pref.getBoolean(getString(R.string.language_first_boot), true)) {
            Intent intent = new Intent(MainActivity.this, ChooseLanguageActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setCurrentCampus() {
        Intent intent = getIntent();
        String campus = intent.getStringExtra(CAMPUS);

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
        WalkingTimeData data = new WalkingTimeData(location.getLatitude(), location.getLongitude(), apiKey, services, getGlobalStatus().getLanguage());
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
            showToast(getString(R.string.main_update_walking_distance_failure_toast));
        }
    }


    public void updateServicesQueueTime() {
        if (isNetworkAvailable()) {
            String[] foodServiceNames = getGlobalStatus().getServices().stream()
                    .map(FoodService::getName)
                    .toArray(String[]::new);
            new CancelableTask<>(new SafePostTask<>(new ServiceQueueTimeTask(this))).executeOnExecutor(getGlobalStatus().getExecutor(), foodServiceNames);
        } else {
            showToast(getString(R.string.no_network_avaliable_queue_time_message));
        }
    }


    public void setFoodServiceQueueTimes(Map<String, String> times) {
        times = times.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), timeString(Long.parseLong(entry.getValue()))))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
        getGlobalStatus().setQueueTimes(times);
    }


    private void launchWalkingTimeTask(WalkingTimeData data) {
        new CancelableTask<>(new SafePostTask<>(new ServiceWalkingTimeTask(this))).executeOnExecutor(getGlobalStatus().getExecutor(), data);
    }

    private void launchGuessCampusTask(LatLng curr) {
        new CancelableTask<>(new SafePostTask<>(new GuessCampusTask(this, curr))).executeOnExecutor(getGlobalStatus().getExecutor(), MainActivity.LOCATION_ALAMEDA, MainActivity.LOCATION_TAGUS, MainActivity.LOCATION_CTN);
    }

    private void updateCampusFromLocation(Location location) {
        LatLng curr = new LatLng(location.getLatitude(), location.getLongitude());
        launchGuessCampusTask(curr);
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
        if (hasLocationPermission()) {
            guessCampusFromLocation();
        } else {
            showToast(getString(R.string.main_infering_location_failure_toast));
            askCampus();
        }
    }

    public void setCampus(String campus) {
        //Update Interface
        this.campus = campus;
        TextView textView = findViewById(R.id.currentCampus);
        textView.setText(campus);

        //Save preferences for later
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.global_preferences_file), 0).edit();
        editor.putString(getString(R.string.global_preferences_location), campus);
        editor.apply();
        getGlobalStatus().setCampus(campus);
        loadServices(campus);
    }

    private void drawService(FoodService service) {
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.food_service, null);

        TextView name = v.findViewById(R.id.foodServiceName);
        name.setText(service.getName(getGlobalStatus().getLanguage()));

        TextView distance = v.findViewById(R.id.distance);
        distance.setText(String.format("%s: %s", getString(R.string.main_walking_time), service.getDistance()));

        TextView queue = v.findViewById(R.id.queueTime);
        queue.setText(String.format("%s: %s", getString(R.string.main_queue_time), service.getTime()));

        v.setOnClickListener(v1 -> {
            Intent intent = new Intent(MainActivity.this, FoodServiceActivity.class);
            intent.putExtra(SERVICE_NAME, service.getName());
            intent.putExtra(SERVICE_DISPLAY_NAME, service.getName(getGlobalStatus().getLanguage()));
            intent.putExtra(DISTANCE, service.getDistance());
            intent.putExtra(QUEUE_TIME, service.getTime());
            intent.putExtra(SERVICE_HOURS, service.getHoursForToday(getGlobalStatus().getUserRole()));
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
        setFilterBox();
    }

    public void askCampus() {
        Intent intent = new Intent(MainActivity.this, ChooseCampusActivity.class);
        //Must Finish Activity so that user doesn't back out of choosing campus and leaves the application without campus
        finish();
        startActivity(intent);
    }


    public void toCampus() {
        Intent intent = new Intent(MainActivity.this, ChooseCampusActivity.class);
        startActivity(intent);
    }

    private void loadServices(String campus) {
        InputStream is = getResources().openRawResource(R.raw.services);
        FoodServiceData resource = new FoodServiceData(is, campus);
        launchFoodServiceParseTask(resource);
    }

    private void launchFoodServiceParseTask(FoodServiceData resource) {
        new CancelableTask<>((new SafePostTask<>(new ServiceParsingTask(this)))).executeOnExecutor(getGlobalStatus().getExecutor(), resource);
    }

    public List<FoodService> getAvailableServices() {
        return useConstraints ? getAvailableServicesWithConstraints() : getAvailableServicesNoConstraints();
    }

    public void switchConstraints() {
        useConstraints = !useConstraints;
        drawServices();
    }

    public void setFilterBox() {
        CheckBox box = findViewById(R.id.show_all_services_button);
        box.setEnabled(true);
        box.setOnClickListener(v -> this.switchConstraints());
    }

    public List<FoodService> getAvailableServicesWithConstraints() {

        return getGlobalStatus().getServices().stream()
                .filter(service -> service.isFoodServiceAvailable(getGlobalStatus().getUserRole()))
                .filter(service -> service.isFoodServiceConstrained(getGlobalStatus().getUserConstraints()))
                .collect(Collectors.toList());
    }

    public List<FoodService> getAvailableServicesNoConstraints() {

        return getGlobalStatus().getServices().stream()
                .filter(service -> service.isFoodServiceAvailable(getGlobalStatus().getUserRole()))
                .collect(Collectors.toList());
    }


    private void distanceLocationCallback(Location location) {
        if (location != null) {
            updateServicesFromLocation(location);
        } else {
            showToast(getString(R.string.could_not_get_walking_distance_message));
        }
    }

    private void campusLocationCallback(Location location) {
        if (location != null) {
            updateCampusFromLocation(location);
        } else {
            showToast(getString(R.string.could_not_inter_campus_message));
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
