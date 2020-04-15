package pt.ulisboa.tecnico.cmov.foodist.activity.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public abstract class ActivityWithMap extends BaseActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap map;

    private boolean isReceiving = false;

    private static final String TAG = "ACTIVITY-WITH-MAP";

    private LocationManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    public abstract String getMarkerName();

    public abstract double getLatitude();

    public abstract double getLongitude();

    public abstract SupportMapFragment getMapFragment();

    public abstract void mapClick(LatLng latLng);

    @Override
    protected void onResume() {
        super.onResume();
        initMap();
    }
    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = getMapFragment();

        mapFragment.getMapAsync(this);
    }

    public void fillMap(Direction direction) {
        List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
        ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(getApplicationContext(), stepList, 5, Color.RED, 3, Color.BLUE);
        for (PolylineOptions polylineOption : polylineOptionList) {
            map.addPolyline(polylineOption);
        }
    }

    public LatLng getNorthEast(LatLng first, LatLng second) {
        double latitude = Math.max(first.latitude, second.latitude);
        double longitude = Math.max(first.longitude, second.longitude);
        return new LatLng(latitude, longitude);
    }


    public LatLng getSouthWest(LatLng first, LatLng second) {
        double latitude = Math.min(first.latitude, second.latitude);
        double longitude = Math.min(first.longitude, second.longitude);
        return new LatLng(latitude, longitude);
    }

    public void updateMap(LatLng source, LatLng dest) {
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(getSouthWest(source, dest), getNorthEast(source, dest)), 30));
        map.clear();
        map.addMarker(new MarkerOptions().position(dest).title(getMarkerName()));
        GoogleDirection.withServerKey(((GlobalStatus) getApplicationContext()).getApiKey())
                .from(source)
                .to(dest)
                .transportMode(TransportMode.WALKING)
                .execute((new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction) {
                        if (direction.isOK()) {
                            Log.v(TAG, "Direction is Ok");
                            fillMap(direction);
                        } else {
                            Log.v(TAG, "Direction is not Ok");
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.v(TAG, "Could not get Direction");
                        showToast("Could not get directions to food service");
                    }
                }));
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (hasLocationPermission() && isNetworkAvailable()) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            isReceiving = true;
        } else {
            showToast("No internet or location permission: cannot show directions to service");
        }
    }

    public void stopLocationUpdates() {
        if (isReceiving) {
            mLocationManager.removeUpdates(this);
            isReceiving = false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setIndoorEnabled(true);
        if (hasLocationPermission()) {
            map.setMyLocationEnabled(true);
        }
        LatLng destination = new LatLng(getLatitude(), getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 18));
        map.addMarker(new MarkerOptions().position(destination).title(getMarkerName()));
        map.setOnMapClickListener(this::mapClick);
        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v(TAG, "latitude: " + location.getLatitude());
            Log.v(TAG, "longitude: " + location.getLongitude());
            updateMap(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(getLatitude(), getLongitude()));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
