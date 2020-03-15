package pt.ulisboa.tecnico.cmov.foodist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FoodServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);

        TextView foodServiceName = (TextView) findViewById(R.id.foodServiceName);
        TextView openingTimes = (TextView) findViewById(R.id.openingTimes);
        TextView queueTime = (TextView) findViewById(R.id.queueTime);

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
