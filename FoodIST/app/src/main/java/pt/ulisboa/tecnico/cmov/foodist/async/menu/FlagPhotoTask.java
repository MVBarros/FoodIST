package pt.ulisboa.tecnico.cmov.foodist.async.menu;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen.FullscreenPhotoActivity;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;


public class FlagPhotoTask extends AsyncTask<String, Boolean, Boolean> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private WeakReference<FullscreenPhotoActivity> activity;
    private String cookie;
    private WeakReference<GlobalStatus> status;

    private String photoId;

    public FlagPhotoTask(FullscreenPhotoActivity activity) {
        this.activity = new WeakReference<>(activity);
        this.stub = activity.getGlobalStatus().getStub();
        this.cookie = activity.getGlobalStatus().getCookie();
        this.status = new WeakReference<>(activity.getGlobalStatus());
    }

    private static final String TAG = "DOWNLOAD-PHOTOS-TASK";

    @Override
    protected Boolean doInBackground(String... photos) {
        if (photos.length != 1) {
            return false;
        }
        photoId = photos[0];
        try {
            stub.flagPhoto(Contract.FlagRequest.newBuilder().setCookie(cookie).setPhotoId(Long.parseLong(photoId)).build());
        } catch (StatusRuntimeException e) {
            return false;
        }
        return true;
    }

    @Override
    public void onPostExecute(Boolean result) {
        GlobalStatus stats = status.get();
        if (stats == null) {
            return;
        }
        if (result) {
            stats.setFlagged(photoId);
        }
        FullscreenPhotoActivity act = activity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            if (!result) {
                act.showToast(act.getString(R.string.flag_menu_error_message));
                return;
            }
            act.showToast(act.getString(R.string.menu_flag_successfull_message));
            act.setButtonClickable();
        }
    }
}

