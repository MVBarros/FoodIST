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

public class RegisterAsyncTask extends AsyncTask<Contract.RegisterRequest, Integer, Contract.AccountMessage> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private WeakReference<GlobalStatus> mContext;
    private boolean exists = false;

    public RegisterAsyncTask(LoginActivity activity) {
        this.mContext = new WeakReference<>(activity.getGlobalStatus());
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
        if (status != null) {
            if (message == null) {
                if (exists) {
                    Toast.makeText(status, status.getString(R.string.username_exists_message), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(status, status.getString(R.string.username_exists_message), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            status.saveProfile(message.getProfile());
            status.saveCookie(message.getCookie());
            Toast.makeText(status, status.getString(R.string.register_success_message), Toast.LENGTH_SHORT).show();
        }
    }
}
