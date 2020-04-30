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

public class RegisterAsyncTask extends AsyncTask<Contract.RegisterRequest, Integer, Contract.AccountMessage> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private WeakReference<GlobalStatus> mContext;
    private WeakReference<LoginActivity> mActivity;
    private boolean exists = false;

    public RegisterAsyncTask(LoginActivity activity) {
        this.mContext = new WeakReference<>(activity.getGlobalStatus());
        this.mActivity = new WeakReference<>(activity);
        this.stub = activity.getGlobalStatus().getStub();

    }

    @Override
    protected Contract.AccountMessage doInBackground(Contract.RegisterRequest... profiles) {
        if (profiles.length != 1) {
            return null;
        }

        try {
            return stub.register(profiles[0]);
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == io.grpc.Status.ALREADY_EXISTS) {
                exists = true;
            }
            return null;
        }

    }

    @Override
    public void onPostExecute(Contract.AccountMessage message) {
        GlobalStatus status = mContext.get();
        //Just in case...
        if (status == null) {
            return;
        }

        if (message != null) {
            status.saveProfile(message.getProfile());
            status.saveCookie(message.getCookie());
        }

        LoginActivity act = mActivity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            if (message == null) {
                errorMessage(act);
                return;
            }
            act.showToast(act.getString(R.string.register_success_message));
            act.finish();
        }
    }

    private void errorMessage(BaseActivity activity) {
        activity.showToast(activity.getString(R.string.username_exists_message));
    }
}
