package pt.ulisboa.tecnico.cmov.foodist.listener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

public abstract class BaseNetworkClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        if (isNetworkAvailable(v.getContext())) {
            clickWhenUp(v);
        } else {
            clickWhenDown(v);
        }
    }

    public abstract void clickWhenUp(View v);

    public abstract void clickWhenDown(View v);

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
