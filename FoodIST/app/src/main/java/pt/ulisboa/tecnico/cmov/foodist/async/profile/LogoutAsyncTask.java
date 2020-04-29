package pt.ulisboa.tecnico.cmov.foodist.async.profile;

import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.LoginActivity;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class LogoutAsyncTask extends AsyncTask<String, Integer, Boolean> {


    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private WeakReference<GlobalStatus> mContext;
    private WeakReference<LoginActivity> mActivity;

    public LogoutAsyncTask(LoginActivity activity) {
        this.mContext = new WeakReference<>(activity.getGlobalStatus());
        this.mActivity = new WeakReference<>(activity);
        this.stub = activity.getGlobalStatus().getStub();

    }

    @Override
    protected Boolean doInBackground(String... cookies) {
        if (cookies.length != 1) {
            return false;
        }
        try {
            stub.logout(Contract.LogoutRequest.newBuilder().setCookie(cookies[0]).build());
            return true;
        } catch (StatusRuntimeException e) {
            return false;
        }
    }


    @Override
    public void onPostExecute(Boolean message) {
        GlobalStatus status = mContext.get();
        //Just in case...
        if (status != null) {
            if (message == null) {
                errorMessage(status);
                return;
            }
            status.removeCookie();
            Toast.makeText(status, status.getString(R.string.logout_successful_message), Toast.LENGTH_SHORT).show();
        }
        LoginActivity act = mActivity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            act.setButtons();
        }
    }

    private void errorMessage(GlobalStatus status) {
        Toast.makeText(status, status.getString(R.string.logout_error_message), Toast.LENGTH_SHORT).show();
    }
}
