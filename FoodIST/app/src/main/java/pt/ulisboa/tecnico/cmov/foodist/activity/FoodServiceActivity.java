package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import foodist.server.grpc.contract.Contract;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen.FullscreenMapActivity;
import pt.ulisboa.tecnico.cmov.foodist.adapters.MenuAdapter;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.SafePostTask;
import pt.ulisboa.tecnico.cmov.foodist.async.service.GetMenusTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.ServiceNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;

public class FoodServiceActivity extends BaseActivity implements OnMapReadyCallback {

    private static final String SERVICE_NAME = "Service Name";
    private static final String SERVICE_DISPLAY_NAME = "Service Display Name";
    private static final String SERVICE_HOURS = "Service Hours";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String QUEUE_TIME = "Queue time";
    private static final String DISTANCE = "Distance";

    private static final int ZOOM = 18;

    private String foodServiceName;

    private String foodServiceId;

    private AtomicBoolean filter = new AtomicBoolean(true);

    private String distance;
    private String hours;

    private double latitude;
    private double longitude;

    private ArrayList<Menu> menus = new ArrayList<>();

    private static final String TAG = "ACTIVITY_FOOD_SERVICE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);
        setFoodService();
        setQueueTime();
        setButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        initMap();
        updateMenus();
    }

    @Override
    public void addReceivers() {
        super.addReceivers();
        addReceiver(new ServiceNetworkReceiver(this), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public boolean getFilter() {
        return filter.get();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    public String getMarkerName() {
        return foodServiceName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
    }

    public void mapClick(LatLng latLng) {
        Intent intent = new Intent(FoodServiceActivity.this, FullscreenMapActivity.class);
        intent.putExtra(SERVICE_NAME, foodServiceName);
        intent.putExtra(LATITUDE, latitude);
        intent.putExtra(LONGITUDE, longitude);
        startActivity(intent);
    }


    private void setQueueTime() {
        TextView queueTime = findViewById(R.id.queueTime);

        Intent intent = getIntent();
        String queueValue = intent.getStringExtra(QUEUE_TIME) == null ? "" : intent.getStringExtra(QUEUE_TIME);
        queueTime.setText(String.format("%s: %s", getString(R.string.food_service_queue_time), queueValue));
    }

    private void setButtons() {
        Button addMenu = findViewById(R.id.add_menu_button);

        addMenu.setOnClickListener(v -> {
            Intent intent = new Intent(FoodServiceActivity.this, AddMenuActivity.class);
            intent.putExtra(SERVICE_NAME, foodServiceId);
            startActivity(intent);
        });
    }

    private void setFoodService() {

        this.foodServiceId = getIntent().getStringExtra(SERVICE_NAME) == null ? "" : getIntent().getStringExtra(SERVICE_NAME);
        this.foodServiceName = getIntent().getStringExtra(SERVICE_DISPLAY_NAME) == null ? "" : getIntent().getStringExtra(SERVICE_DISPLAY_NAME);
        this.distance = getIntent().getStringExtra(DISTANCE) == null ? "" : getIntent().getStringExtra(DISTANCE);
        this.latitude = getIntent().getDoubleExtra(LATITUDE, 0);
        this.longitude = getIntent().getDoubleExtra(LONGITUDE, 0);
        this.hours = getIntent().getStringExtra(SERVICE_HOURS) == null ? "" : getIntent().getStringExtra(SERVICE_HOURS);

        TextView foodServiceName = findViewById(R.id.foodServiceName);
        TextView foodServiceHours = findViewById(R.id.openingTimes);
        TextView foodServiceDistance = findViewById(R.id.walkingDistance);

        foodServiceDistance.setText(String.format("%s %s", getString(R.string.food_service_walking_distance), this.distance));
        foodServiceName.setText(this.foodServiceName);
        foodServiceHours.setText(String.format("%s %s", getString(R.string.food_service_working_hours), this.hours));
    }

    public void drawFoodServices() {
        ArrayList<Menu> drawableMenus = new ArrayList<>(menus);

        filterMenus(drawableMenus, getGlobalStatus().getUserConstraints());

        final MenuAdapter menuAdapter = new MenuAdapter(this, drawableMenus);

        ListView foodServiceList = findViewById(R.id.menus);
        foodServiceList.setAdapter(menuAdapter);
    }

    public void filterMenus(ArrayList<Menu> menus, Map<Contract.FoodType, Boolean> constraints) {
        if (getFilter()) {
            menus.removeIf(menu -> !menu.isDesirable(constraints));
        }
    }

    public void updateMenus() {
        if (isNetworkAvailable()) {
            new CancelableTask<>(new SafePostTask<>(new GetMenusTask(this))).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.foodServiceId);
        } else {
            showToast(getString(R.string.food_service_menu_update_failure_toast));
        }
    }

    public void doShowAllButton() {
        final Button button = findViewById(R.id.show_all_menus_button);
        button.setOnClickListener((l) -> {
            filter.set(!filter.get());
            button.setText(getString(getFilter() ? R.string.food_service_show_all_menus : R.string.food_service_filter_menus));
            drawFoodServices();
        });
    }

    private void initMap() {
        getMapFragment().getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setIndoorEnabled(true);

        LatLng destination = new LatLng(getLatitude(), getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, ZOOM));
        googleMap.addMarker(new MarkerOptions().position(destination).title(getMarkerName()));
        googleMap.setOnMapClickListener(this::mapClick);
    }

    public void setMenus(ArrayList<Menu> menus) {
        this.menus = menus;
    }

}

