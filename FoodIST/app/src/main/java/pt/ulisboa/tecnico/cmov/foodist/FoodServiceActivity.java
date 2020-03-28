package pt.ulisboa.tecnico.cmov.foodist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmov.foodist.async.GetMenusTask;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class FoodServiceActivity extends BaseActivity {

    private String SERVICE_NAME = "Service Name";
    private static final String DISTANCE = "Distance";
    private static final String QUEUE_TIME = "Queue time";
    private String foodServiceName;
    private String queueTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);

        TextView foodServiceName = findViewById(R.id.foodServiceName);
        TextView openingTimes = findViewById(R.id.openingTimes);
        TextView queueTime = findViewById(R.id.queueTime);

        Intent intent = getIntent();
        this.foodServiceName = intent.getStringExtra(SERVICE_NAME);
        foodServiceName.setText(this.foodServiceName);
        String queueValue = intent.getStringExtra(QUEUE_TIME);
        if(queueValue != null){
            this.queueTime = queueValue;
        }

        queueTime.setText(this.queueTime);

        Button addMenu = findViewById(R.id.add_menu_button);

        addMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent oldIntent = getIntent();
                String serviceName = oldIntent.getStringExtra("Service Name");

                Intent intent = new Intent(FoodServiceActivity.this, AddMenuActivity.class);
                intent.putExtra(SERVICE_NAME, serviceName);

                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //ListView foodServiceList = findViewById(R.id.menus);
        //foodServiceList.removeAllViews();
        new GetMenusTask(FoodServiceActivity.this, ((GlobalStatus) FoodServiceActivity.this.getApplicationContext()).getStub()).execute(this.foodServiceName);
    }
}
