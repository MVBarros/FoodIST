package pt.ulisboa.tecnico.cmov.foodist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.foodist.async.GetMenusTask;

public class FoodServiceActivity extends BaseActivity {

    private String SERVICE_NAME = "Service Name";
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

    private void setQueueTime() {
        TextView queueTime = findViewById(R.id.queueTime);

        Intent intent = getIntent();
        String queueValue = intent.getStringExtra(QUEUE_TIME) == null ? "" : intent.getStringExtra(QUEUE_TIME);
        queueTime.setText(queueValue);
    }

    private void setButtons() {
        Button addMenu = findViewById(R.id.add_menu_button);

        addMenu.setOnClickListener(v -> {
            Intent oldIntent = getIntent();
            String serviceName = oldIntent.getStringExtra("Service Name");

            Intent intent1 = new Intent(FoodServiceActivity.this, AddMenuActivity.class);
            intent1.putExtra(SERVICE_NAME, serviceName);

            startActivity(intent1);
        });

    }

    private void setFoodServiceName() {
        TextView foodServiceName = findViewById(R.id.foodServiceName);
        Intent intent = getIntent();
        String foodService = intent.getStringExtra(SERVICE_NAME) == null ? "" : intent.getStringExtra(SERVICE_NAME);
        this.foodServiceName = foodService;
        foodServiceName.setText(foodService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //ListView foodServiceList = findViewById(R.id.menus);
        //foodServiceList.removeAllViews();
        updateMenus();
    }

    private void updateMenus() {
        if (isNetworkAvailable()) {
            new GetMenusTask(this).execute(this.foodServiceName);
        } else {
            showToast("No internet connection: Cannot get menus");
        }
    }

}
