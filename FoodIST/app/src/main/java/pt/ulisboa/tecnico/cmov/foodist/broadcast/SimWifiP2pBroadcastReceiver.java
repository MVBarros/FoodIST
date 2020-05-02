package pt.ulisboa.tecnico.cmov.foodist.broadcast;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private BaseActivity mActivity;
    private final static String TAG = "WIFI-DIRECT-RECEIVER";

    public SimWifiP2pBroadcastReceiver(BaseActivity activity) {
        super();
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "RECEIVED_CONTEXT");
        SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
        ginfo.print();
        Toast.makeText(mActivity, "Network membership changed",
                Toast.LENGTH_SHORT).show();

    }
}
