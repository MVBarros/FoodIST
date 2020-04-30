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

public class LoginAsyncTask extends AsyncTask<Contract.LoginRequest, Integer, Contract.AccountMessage> {


    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private WeakReference<GlobalStatus> mContext;
    private WeakReference<LoginActivity> mActivity;

    public LoginAsyncTask(LoginActivity activity) {
        this.mContext = new WeakReference<>(activity.getGlobalStatus());
        this.mActivity = new WeakReference<>(activity);
        this.stub = activity.getGlobalStatus().getStub();

    }

    @Override
    protected Contract.AccountMessage doInBackground(Contract.LoginRequest... loginRequests) {
        if (loginRequests.length != 1) {
            return null;
        }
        try {
            return stub.login(loginRequests[0]);
        } catch (StatusRuntimeException e) {
            return null;
        }
    }


    @Override
    public void onPostExecute(Contract.AccountMessage message) {
        GlobalStatus status = mContext.get();
        //Just in case...
        if (status != null) {
            if (message == null) {
                errorMessage(status);
                return;
            }
            status.saveProfile(message.getProfile());
            status.saveCookie(message.getCookie());
            Toast.makeText(status, R.string.login_successful_message, Toast.LENGTH_SHORT).show();
        }
        LoginActivity act = mActivity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            act.returnToMain();
        }
    }

    private void errorMessage(GlobalStatus status) {
        Toast.makeText(status, R.string.login_error_message, Toast.LENGTH_SHORT).show();
    }
}
