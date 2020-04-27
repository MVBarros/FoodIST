package pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.ActivityWithMap;

import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.LATITUDE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.LONGITUDE;
import static pt.ulisboa.tecnico.cmov.foodist.activity.data.IntentKeys.SERVICE_NAME;


public class FullscreenMapActivity extends ActivityWithMap {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_map);
    }

    @Override
    public String getMarkerName() {
        return getIntent().getStringExtra(SERVICE_NAME) == null ? "" : getIntent().getStringExtra(SERVICE_NAME);
    }

    @Override
    public double getLatitude() {
        return getIntent().getDoubleExtra(LATITUDE, 0d);
    }

    @Override
    public double getLongitude() {
        return getIntent().getDoubleExtra(LONGITUDE, 0d);
    }

    @Override
    public SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

    }

    @Override
    public void mapClick(LatLng latLng) {
        finish();
    }

    @Override
    public void fillTime(String time) {
        TextView textView = findViewById(R.id.map_time);
        textView.setText(getString(R.string.fullscreen_map_time, time));
        FoodServiceActivity.setDistance(time);
    }
}
