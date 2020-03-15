package pt.ulisboa.tecnico.cmov.foodist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String campus = intent.getStringExtra(ChooseCampusActivity.CAMPUS);

        Button userProfileButton = (Button) findViewById(R.id.userProfile);
        Button changeCampusButton = (Button) findViewById(R.id.changeCampus);

        //While in testing, intent can have message null
        if(campus != null){
            TextView textView = (TextView) findViewById(R.id.currentCampus);
            textView.setText(campus);
        }

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        changeCampusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChooseCampusActivity.class);
                startActivity(intent);
            }
        });



    }



}
