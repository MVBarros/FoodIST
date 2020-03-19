package pt.ulisboa.tecnico.cmov.foodist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FoodServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);

        TextView foodServiceName = findViewById(R.id.foodServiceName);
        TextView openingTimes = findViewById(R.id.openingTimes);
        TextView queueTime = findViewById(R.id.queueTime);

        Intent intent = getIntent();
        String serviceName = intent.getStringExtra("Service Name");
        foodServiceName.setText(serviceName);

        //TODO - Add menus dinamically
        LinearLayout menu0 = (LinearLayout) findViewById(R.id.menu0);

        menu0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodServiceActivity.this, FoodMenuActivity.class);
                startActivity(intent);
            }
        });
    }
}
