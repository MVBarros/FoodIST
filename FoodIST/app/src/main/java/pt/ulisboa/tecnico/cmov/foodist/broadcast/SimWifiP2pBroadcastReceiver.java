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
    private boolean inLine = false;
    public SimWifiP2pBroadcastReceiver(BaseActivity activity) {
        super();
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "RECEIVED_CONTEXT");
        SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
                SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
        //Assuming only 1 membership ever exists
        Log.d(TAG, "ClientX:" + ginfo.askIsClient());
        if(ginfo.askIsConnected()){
            //
            if(!inLine){
                inLine = true;
                Toast.makeText(mActivity, "Entered line",
                        Toast.LENGTH_SHORT).show();
                //Send info to the server to add me in line;
            }
        }
        else{
            if(inLine){
                inLine = false;
                Toast.makeText(mActivity, "Left line",
                        Toast.LENGTH_SHORT).show();
                //Send info to the server removing me from line
            }
        }
        ginfo.print();
    }
}
