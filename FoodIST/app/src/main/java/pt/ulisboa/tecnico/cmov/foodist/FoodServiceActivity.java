package pt.ulisboa.tecnico.cmov.foodist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.foodist.async.campus.GetMenusTask;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class FoodServiceActivity extends BaseActivity {

    private String SERVICE_NAME = "Service Name";
    private String foodServiceName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);

        TextView foodServiceName = findViewById(R.id.foodServiceName);
        TextView openingTimes = findViewById(R.id.openingTimes);
        TextView queueTime = findViewById(R.id.queueTime);

        Intent intent = getIntent();
        this.foodServiceName = intent.getStringExtra("Service Name");
        foodServiceName.setText(this.foodServiceName);

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
    protected void onResume(){
        super.onResume();
        ViewGroup foodServiceList = findViewById(R.id.menus);
        foodServiceList.removeAllViews();
        new GetMenusTask(FoodServiceActivity.this, ((GlobalStatus)FoodServiceActivity.this.getApplicationContext()).getStub()).execute(this.foodServiceName);
    }
}
