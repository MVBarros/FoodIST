package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.GetMenusTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.SafePostTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.ServiceNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class FoodServiceActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String SERVICE_NAME = "Service Name";
    private static final String SERVICE_HOURS = "Service Hours";
    private static final String DISTANCE = "Distance";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String QUEUE_TIME = "Queue time";

    private String foodServiceName;
    private double latitude;
    private double longitude;
    private String walkingTime;
    private GoogleMap map;

    private static final String TAG = "ACTIVITY_FOOD_SERVICE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);
        setFoodService();
        initMap();
        setQueueTime();
        setButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMenus();
    }

    public void addReceivers() {
        addReceiver(new ServiceNetworkReceiver(), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setIndoorEnabled(true);
        LatLng latlngsrc = new LatLng(38.7373428,-9.3027452);
        LatLng latlngdst = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlngsrc, 18));
        map.addMarker(new MarkerOptions().position(latlngdst).title(foodServiceName));
        GoogleDirection.withServerKey(((GlobalStatus) getApplicationContext()).getApiKey())
                .from(latlngsrc)
                .to(latlngdst)
                .transportMode(TransportMode.WALKING)
                .execute((new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction) {
                        if(direction.isOK()) {
                            Log.v(TAG, "Direction is Ok");
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
                            ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(getApplicationContext(), stepList, 5, Color.RED, 3, Color.BLUE);
                            for (PolylineOptions polylineOption : polylineOptionList) {
                                map.addPolyline(polylineOption);
                            }
                        } else {
                            Log.v(TAG, "Direction is not Ok");
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.v(TAG, "Could not get Direction");
                        showToast("Could not get directions to food service from current location");
                    }
                }));
    }

    private void setQueueTime() {
        TextView queueTime = findViewById(R.id.queueTime);

        Intent intent = getIntent();
        String queueValue = intent.getStringExtra(QUEUE_TIME) == null ? "" : intent.getStringExtra(QUEUE_TIME);
        queueTime.setText(queueValue);
    }

    private void setButtons() {
        Button addMenu = findViewById(R.id.add_menu_button);

        addMenu.setOnClickListener(v -> {
            String serviceName = getIntent().getStringExtra("Service Name");

            Intent intent = new Intent(FoodServiceActivity.this, AddMenuActivity.class);
            intent.putExtra(SERVICE_NAME, serviceName);

            startActivity(intent);
        });
    }

    private void setFoodService() {
        TextView foodServiceName = findViewById(R.id.foodServiceName);
        TextView foodServiceHours = findViewById(R.id.openingTimes);
        Intent intent = getIntent();
        String foodService = intent.getStringExtra(SERVICE_NAME) == null ? "" : intent.getStringExtra(SERVICE_NAME);
        String hours = intent.getStringExtra(SERVICE_HOURS) == null ? "" : intent.getStringExtra(SERVICE_HOURS);
        this.foodServiceName = foodService;
        this.latitude = intent.getDoubleExtra(LATITUDE, 0);
        this.longitude = intent.getDoubleExtra(LONGITUDE, 0);
        this.walkingTime = intent.getStringExtra(DISTANCE) == null ? "" : intent.getStringExtra(DISTANCE);
        foodServiceName.setText(foodService);
        foodServiceHours.setText(String.format("%s %s", getString(R.string.working_hours), hours));
    }

    public void updateMenus() {
        if (isNetworkAvailable()) {
            new CancelableTask<>(new SafePostTask<>(new GetMenusTask(this))).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.foodServiceName);
        } else {
            showToast("No internet connection: Cannot get menus");
        }
    }
}

