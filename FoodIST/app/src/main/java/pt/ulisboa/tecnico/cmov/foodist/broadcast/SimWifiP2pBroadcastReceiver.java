package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private BaseActivity mActivity;
    private final static String TAG = "WIFI-DIRECT-RECEIVER";
    private boolean isInQueue = false;
    private List<String> foodServiceNames;

    public SimWifiP2pBroadcastReceiver(BaseActivity activity, List<String> foodServiceNames) {
        super();
        this.mActivity = activity;
        this.foodServiceNames = foodServiceNames;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SimWifiP2pDeviceList deviceInfo = (SimWifiP2pDeviceList) intent.getSerializableExtra(SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);

        List foodServiceBeacons = deviceInfo.getDeviceList()
                .stream()
                .filter(Objects::nonNull)
                .map(device -> device.deviceName.replaceAll("_", " "))
                .filter(deviceName -> foodServiceNames.contains(deviceName))
                .collect(Collectors.toList());

        if(foodServiceBeacons.size() > 0){
            if(!isInQueue){
                Log.d(TAG, "Entered line of food service " + foodServiceBeacons.get(0));
                isInQueue = true;
                Toast.makeText(mActivity, "Entered line of food service " + foodServiceBeacons.get(0) ,
                        Toast.LENGTH_SHORT).show();
            }
        }

        else{
            if(isInQueue){
                Log.d(TAG, "Left line of food service");
                isInQueue = false;
                Toast.makeText(mActivity, "Left line of food service",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
