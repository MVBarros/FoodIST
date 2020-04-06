package pt.ulisboa.tecnico.cmov.foodist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.foodist.async.GuessCampusTask;
import pt.ulisboa.tecnico.cmov.foodist.async.ServiceParsingTask;
import pt.ulisboa.tecnico.cmov.foodist.async.ServiceWalkingTimeTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.SafePostTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.MainActivityBroadcastReceiver;
import pt.ulisboa.tecnico.cmov.foodist.data.FoodServiceData;
import pt.ulisboa.tecnico.cmov.foodist.data.WalkingTimeData;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;
import pt.ulisboa.tecnico.cmov.foodist.utils.CoordenateUtils;


public class MainActivity extends BaseActivity {

    private FusedLocationProviderClient fusedLocationClient;

    private static final String TAG = "TAG_MainActivity";
    private static final int PHONE_LOCATION_REQUEST_CODE = 1;

    private static final String SERVICE_NAME = "Service Name";
    private static final String DISTANCE = "Distance";
    private static final String QUEUE_TIME = "Queue time";


    private boolean isFreshBoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        updateFirstBoot();
        setButtons();
        setCurrentCampus();
        setReceivers();
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!isFreshBoot) {
            drawServices();
            updateServicesWalkingDistance();
        } else {
            updateFirstBoot();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PHONE_LOCATION_REQUEST_CODE) {
            loadCurrentCampus();
        }
    }

    private void setReceivers() {
        addReceiver(new MainActivityBroadcastReceiver(), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    private void updateFirstBoot() {
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

    public void updateServicesWalkingDistance() {
        if (hasLocationPermission() && isNetworkAvailable()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this,
                    (Location location) -> {
                        //In some rare cases this can be null
                        if (location == null) {
                            return;
                        }
                        GlobalStatus status = getGlobalStatus();
                        List<FoodService> services = new ArrayList<>(status.getServices());
                        String apiKey = status.getApiKey();
                        WalkingTimeData data = new WalkingTimeData(location.getLatitude(), location.getLongitude(), apiKey, services);
                        launchWalkingTimeTask(data);
                    });
        }
    }

    private void launchWalkingTimeTask(WalkingTimeData data) {
        new CancelableTask<>(new SafePostTask<>(new ServiceWalkingTimeTask(this))).execute(data);
    }

    private void launchGuessCampusTask(String... urls) {
        new CancelableTask<>(new SafePostTask<>(new GuessCampusTask(this))).execute(urls);
    }

    private void guessCampusFromLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, (Location location) -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        String apiKey = getGlobalStatus().getApiKey();
                        String urlAlameda = CoordenateUtils.getUrlForDistance(location, getString(R.string.map_alameda), apiKey);
                        String urlTagus = CoordenateUtils.getUrlForDistance(location, getString(R.string.map_taguspark), apiKey);
                        launchGuessCampusTask(urlAlameda, urlTagus);
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not get location, please select campus", Toast.LENGTH_SHORT).show();
                        askCampus();
                    }
                });
    }

    private void loadCurrentCampus() {
        if (hasLocationPermission() && isNetworkAvailable()) {
            guessCampusFromLocation();
        } else {
            Toast.makeText(getApplicationContext(), "Could not infer campus, please insert it manually", Toast.LENGTH_SHORT).show();
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
        try {
            if (this.isFoodServiceAvailable(service)) {
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
                    startActivity(intent);
                });

                ViewGroup foodServiceList = findViewById(R.id.foodServices);
                foodServiceList.addView(v);
            }
        } catch(ParseException pe) {
            Log.e(TAG, "Unable to parse hours for Food Service: \"" + service.getHours() + "\".");
        }
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
        new CancelableTask<>((new SafePostTask<>(new ServiceParsingTask(this)))).execute(resource);
    }

    public List<FoodService> getAvailableServices() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.profile_file), 0);
        String position = pref.getString(getString(R.string.position_name), "");

        return getGlobalStatus().getServices().stream()
                .filter(service -> !service.getRestrictions().contains(position))
                .collect(Collectors.toList());
    }

    private boolean isFoodServiceAvailable(FoodService service) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        calendar.setTime(currentDate);

        String currentHours = dateFormat.format(currentDate);
        String currentWeekday = this.weekdayIntToString(calendar.get(Calendar.DAY_OF_WEEK));

        String functioningHours = service.getHours().get(currentWeekday);

        return this.isTimeInRange(currentHours, functioningHours.split("-"));
    }

    private String weekdayIntToString(int weekday) {
        switch(weekday) {
            case 1:
                return "sunday";
            case 2:
                return "monday";
            case 3:
                return "tuesday";
            case 4:
                return "wednesday";
            case 5:
                return "thursday";
            case 6:
                return "friday";
            case 7:
                return "saturday";
            default:
                throw new IndexOutOfBoundsException("A number in the \"1-7\" range must be inserted!");
        }
    }

    private boolean isTimeInRange(String currentTime, String[] timeRange) throws ParseException {
        String lowerLimit = timeRange[0];
        String upperLimit = timeRange[1];

        Date ctDate = new SimpleDateFormat("hh:mm").parse(currentTime);
        Date llDate = new SimpleDateFormat("hh:mm").parse(lowerLimit);
        Date upDate = new SimpleDateFormat("hh:mm").parse(upperLimit);

        return ctDate.after(llDate) && ctDate.before(upDate);
    }
}
