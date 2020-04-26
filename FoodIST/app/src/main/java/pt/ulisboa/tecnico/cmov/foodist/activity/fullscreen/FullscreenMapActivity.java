package pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.ActivityWithMap;

public class FullscreenMapActivity extends ActivityWithMap {

    private static final String SERVICE_NAME = "Service Name";

    private static final String LATITUDE_KEY = "Latitude";

    private static final String LONGITUDE_KEY = "Longitude";

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
        return getIntent().getDoubleExtra(LATITUDE_KEY, 0d);
    }

    @Override
    public double getLongitude() {
        return getIntent().getDoubleExtra(LONGITUDE_KEY, 0d);
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
    }
}
