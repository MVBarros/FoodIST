package pt.ulisboa.tecnico.cmov.foodist;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.foodist.async.GetMenusTask;
import pt.ulisboa.tecnico.cmov.foodist.broadcast.ServiceNetworkReceiver;

public class FoodServiceActivity extends BaseActivity {

    private static final String SERVICE_NAME = "Service Name";
    private static final String DISTANCE = "Distance";
    private static final String QUEUE_TIME = "Queue time";
    private String foodServiceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);

        setFoodServiceName();
        setQueueTime();
        setButtons();
    }


    public void addReceivers() {
        Set<Button> buttons = Collections.singleton(findViewById(R.id.add_menu_button));
        addReceiver(new ServiceNetworkReceiver(buttons), ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION);
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

    private void setFoodServiceName() {
        TextView foodServiceName = findViewById(R.id.foodServiceName);
        Intent intent = getIntent();
        String foodService = intent.getStringExtra(SERVICE_NAME) == null ? "" : intent.getStringExtra(SERVICE_NAME);
        this.foodServiceName = foodService;
        foodServiceName.setText(foodService);
    }


    public void updateMenus() {
        if (isNetworkAvailable()) {
            new GetMenusTask(this).execute(this.foodServiceName);
        } else {
            showToast("No internet connection: Cannot get menus");
        }
    }


}

