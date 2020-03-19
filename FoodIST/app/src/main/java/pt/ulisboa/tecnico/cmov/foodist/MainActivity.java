package pt.ulisboa.tecnico.cmov.foodist;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG_MainActivity";
    private static final int PHONE_LOCATION_REQUEST_CODE = 1;

    public ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button userProfileButton = findViewById(R.id.userProfile);
        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });


        Button changeCampusButton = findViewById(R.id.changeCampus);
        changeCampusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChooseCampusActivity.class);
                startActivity(intent);
            }
        });


        ListView listView = findViewById(R.id.foodServiceList);
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, FoodServiceActivity.class);
                intent.putExtra("Service Name", adapter.getItem(position));
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        String campus = intent.getStringExtra(ChooseCampusActivity.CAMPUS);

        if (campus != null) {
            //Know Current Location from previous user choice
            setCampus(campus);
        } else {
            askPhonePermission();
        }
    }


    private void askPhonePermission() {
        int hasPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasPhonePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PHONE_LOCATION_REQUEST_CODE);
        } else {
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
        setCampus(getString(R.string.campus_taguspark));
    }

    private void setCampus(String campus) {
        //Update Interface
        TextView textView = findViewById(R.id.currentCampus);
        textView.setText(campus);

        //Update Food Services List
        String[] services;
        if (campus.equals(getString(R.string.campus_alameda))) {
            services = getResources().getStringArray(R.array.Alameda);
        } else {
            services = getResources().getStringArray(R.array.Taguspark);
        }
        adapter.clear();
        adapter.addAll(services);

        //Save preferences for later
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.global_preferences_file), 0).edit();
        editor.putString(getString(R.string.global_preferences_location), campus);
        editor.apply();
    }
}
