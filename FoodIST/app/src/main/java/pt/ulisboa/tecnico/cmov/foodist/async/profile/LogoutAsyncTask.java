package pt.ulisboa.tecnico.cmov.foodist.async.profile;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.LoginActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
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
        if (status == null) {
            return;
        }
        if (message != null) {
            status.removeCookie();
        }

        LoginActivity act = mActivity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            if (message == null) {
                errorMessage(act);
                return;
            }
            act.showToast(act.getString(R.string.logout_successful_message));
            act.setButtons();
        }
    }

    private void errorMessage(BaseActivity activity) {
        activity.showToast(activity.getString(R.string.logout_error_message));
    }
}
