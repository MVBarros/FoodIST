package pt.ulisboa.tecnico.cmov.foodist.activity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.ActivityWithMap;
import pt.ulisboa.tecnico.cmov.foodist.adapters.MenuAdapter;
import pt.ulisboa.tecnico.cmov.foodist.async.GetMenusTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.CancelableTask;
import pt.ulisboa.tecnico.cmov.foodist.async.base.SafePostTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.ServiceNetworkReceiver;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;

public class FoodServiceActivity extends ActivityWithMap {


    private static final String SERVICE_NAME = "Service Name";
    private static final String SERVICE_HOURS = "Service Hours";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String QUEUE_TIME = "Queue time";

    private String foodServiceName;
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
        updateMenus();
    }

    @Override
    public void addReceivers() {
        addReceiver(new ServiceNetworkReceiver(), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public String getMarkerName() {
        return foodServiceName;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
    }

    @Override
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
        foodServiceName.setText(foodService);
        foodServiceHours.setText(String.format("%s %s", getString(R.string.food_service_working_hours), hours));
    }

    public void updateMenus() {
        if (isNetworkAvailable()) {
            new CancelableTask<>(new SafePostTask<>(new GetMenusTask(this))).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.foodServiceName);
        } else {
            showToast(getString(R.string.food_service_menu_update_failure_toast));
        }
    }

    public ArrayList<Menu> getMenus() {
        return menus;
    }

    public void setMenus(ArrayList<Menu> menus) {
        this.menus = menus;
    }


    public void doShowAllButton() {
        final Button button = findViewById(R.id.show_all_menus_button);
        button.setOnClickListener((l) -> {
            final MenuAdapter menuAdapter = new MenuAdapter(this, menus);
            ListView foodServiceList = findViewById(R.id.menus);
            foodServiceList.setAdapter(menuAdapter);
        });
    }

}

