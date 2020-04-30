package pt.ulisboa.tecnico.cmov.foodist.async.profile;

import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.LoginActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.ProfileActivity;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class ChangeProfileAsyncTask extends AsyncTask<Contract.AccountMessage, Integer, Boolean> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private WeakReference<GlobalStatus> mContext;
    private WeakReference<ProfileActivity> mActivity;
    private Contract.Profile profile;

    public ChangeProfileAsyncTask(ProfileActivity activity) {
        this.mContext = new WeakReference<>(activity.getGlobalStatus());
        this.mActivity = new WeakReference<>(activity);
        this.stub = activity.getGlobalStatus().getStub();

    }

    @Override
    protected Boolean doInBackground(Contract.AccountMessage... profiles) {
        if (profiles.length != 1) {
            return false;
        }
        this.profile = profiles[0].getProfile();
        try {
            stub.changeProfile(profiles[0]);
        } catch (StatusRuntimeException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onPostExecute(Boolean message) {
        GlobalStatus status = mContext.get();
        //Just in case...
        if (status == null) {
            return;
        }
        if (!message) {
            Toast.makeText(status, R.string.could_not_change_profile_message, Toast.LENGTH_SHORT).show();
            return;
        }
        status.saveProfile(profile);

        Toast.makeText(status, R.string.profile_synched_mesage, Toast.LENGTH_SHORT).show();

        ProfileActivity act = mActivity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            act.returnToMain();
        }
    }
}
