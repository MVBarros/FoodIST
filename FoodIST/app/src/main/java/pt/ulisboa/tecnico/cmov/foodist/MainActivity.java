package pt.ulisboa.tecnico.cmov.foodist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG_MainActivity";
    private static final int PHONE_LOCATION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String campus = intent.getStringExtra(ChooseCampusActivity.CAMPUS);

        if(campus != null){
            //Know Current Location from previous user choice
            setCampus(campus);
        }
        else {
            askPhonePermission();
        }

        Button userProfileButton = findViewById(R.id.userProfile);
        Button changeCampusButton = findViewById(R.id.changeCampus);

        LinearLayout foodService0 = findViewById(R.id.foodService0);



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

        foodService0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FoodServiceActivity.class);
                startActivity(intent);
            }
        });

    }


    private void askPhonePermission(){
        int hasPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(hasPhonePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PHONE_LOCATION_REQUEST_CODE);
        }
        else {
            guessCampusFromLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PHONE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "phone location permission granted");
                guessCampusFromLocation();
            } else {
                Log.d(TAG, "phone location permission NOT granted");
                //Ask permission manually
                Intent intent = new Intent(MainActivity.this, ChooseCampusActivity.class);
                finish();
                startActivity(intent);
            }
        }
    }

    private void guessCampusFromLocation() {
        //TODO Try and guess campus from location value
        setCampus(getString(R.string.campus_taguspark) );
    }

    private void setCampus(String campus) {
        //Update Interface
        TextView textView = findViewById(R.id.currentCampus);
        textView.setText(campus);

        //Save preferences for later
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.global_preferences_file), 0).edit();
        editor.putString(getString(R.string.global_preferences_location), campus);
        editor.apply();
    }
}
