package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
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
import pt.ulisboa.tecnico.cmov.foodist.adapters.MenuAdapterNoTranslation;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.SafePostTask;
import pt.ulisboa.tecnico.cmov.foodist.async.service.GetMenusTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.ServiceNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.DISTANCE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.LATITUDE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.LONGITUDE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.QUEUE_TIME;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.SERVICE_DISPLAY_NAME;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.SERVICE_HOURS;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.SERVICE_NAME;


public class FoodServiceActivity extends BaseActivity implements OnMapReadyCallback {

    private static final int ZOOM = 18;

    private String foodServiceName;

    private boolean translation = true;

    private String foodServiceId;

    private AtomicBoolean filter = new AtomicBoolean(true);

    private static String distance; //So we can set it in other activities
    private String hours;

    private double latitude;
    private double longitude;

    private ArrayList<Menu> menus = new ArrayList<>();

    private static final String TAG = "ACTIVITY_FOOD_SERVICE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);
        FoodServiceActivity.setDistance(getIntent().getStringExtra(DISTANCE) == null ? "" : getIntent().getStringExtra(DISTANCE));
        setFoodService();
        setQueueTime();
        setTranslateBox();
        doShowAllButton();
        setButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        initMap();
        setDistance();
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

    private void setTranslateBox() {
        CheckBox box = findViewById(R.id.switch_translation);
        box.setOnClickListener(v -> {
            translation = !translation;
            drawServices();
        });
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

    private void setDistance() {
        TextView foodServiceDistance = findViewById(R.id.walkingDistance);
        foodServiceDistance.setText(String.format("%s %s", getString(R.string.food_service_walking_distance), FoodServiceActivity.getDistance()));
    }

    private void setFoodService() {
        this.foodServiceId = getIntent().getStringExtra(SERVICE_NAME) == null ? "" : getIntent().getStringExtra(SERVICE_NAME);
        this.foodServiceName = getIntent().getStringExtra(SERVICE_DISPLAY_NAME) == null ? "" : getIntent().getStringExtra(SERVICE_DISPLAY_NAME);
        this.latitude = getIntent().getDoubleExtra(LATITUDE, 0);
        this.longitude = getIntent().getDoubleExtra(LONGITUDE, 0);
        this.hours = getIntent().getStringExtra(SERVICE_HOURS) == null ? "" : getIntent().getStringExtra(SERVICE_HOURS);

        TextView foodServiceName = findViewById(R.id.foodServiceName);
        TextView foodServiceHours = findViewById(R.id.openingTimes);

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

    public void drawServices() {
        if (translation) {
            drawFoodServices();
        } else {
            drawFoodServicesNotTranslated();
        }
    }

    public void drawFoodServicesNotTranslated() {
        ArrayList<Menu> drawableMenus = new ArrayList<>(menus);

        filterMenus(drawableMenus, getGlobalStatus().getUserConstraints());

        final MenuAdapterNoTranslation menuAdapter = new MenuAdapterNoTranslation(this, drawableMenus);

        ListView foodServiceList = findViewById(R.id.menus);
        foodServiceList.setAdapter(menuAdapter);
    }


    public void filterMenus(ArrayList<Menu> menus, Map<Contract.FoodType, Boolean> constraints) {
        if (getFilter()) {
            menus.removeIf(menu -> !menu.isDesirable(constraints));
        }
    }

    public void updateMenus() {
        if (!isNetworkAvailable()) {
            showToast(getString(R.string.food_service_menu_update_failure_toast));
            return;
        }
        new CancelableTask<>(new SafePostTask<>(new GetMenusTask(this))).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.foodServiceId);
    }

    public void doShowAllButton() {
        final CheckBox checkBox = findViewById(R.id.show_all_menus_button);
        checkBox.setOnClickListener((l) -> {
            filter.set(checkBox.isChecked());
            drawServices();
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


    public static String getDistance() {
        return distance;
    }

    public static void setDistance(String distance) {
        FoodServiceActivity.distance = distance;
    }
}

