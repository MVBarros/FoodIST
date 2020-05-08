package pt.ulisboa.tecnico.cmov.foodist.async.menu;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.protobuf.Empty;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen.FullscreenPhotoActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class UploadRatingTask extends AsyncTask<Double, Integer, Empty> {

    private static final String TAG = "UPLOAD-RATING-TASK";

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private WeakReference<GlobalStatus> status;
    private String cookie;
    private String menuId;
    private String username;
    private double rating;
    private WeakReference<FoodMenuActivity> activity;

    public UploadRatingTask(FoodMenuActivity activity) {
        this.stub = activity.getGlobalStatus().getStub();
        this.menuId = activity.getMenuId();
        this.status = new WeakReference<>(activity.getGlobalStatus());
        this.cookie = activity.getGlobalStatus().getCookie();
        this.activity = new WeakReference<>(activity);
    }


    @Override
    protected Empty doInBackground(Double... doubles) {
        if (doubles.length == 0) {
            return null;
        }
        this.rating = doubles[0];
        Contract.RatingRequest.Builder ratingRequestBuilder = Contract.RatingRequest.newBuilder();

        ratingRequestBuilder.setMenuId(Long.parseLong(menuId));
        ratingRequestBuilder.setCookie(cookie);
        ratingRequestBuilder.setRating(this.rating);

        Contract.RatingRequest request = ratingRequestBuilder.build();

        try {
            return this.stub.uploadRating(request);
        } catch (StatusRuntimeException e) {
            return null;
        }

    }

    @Override
    public void onPostExecute(Empty result) {
        GlobalStatus stats = status.get();
        if (stats == null) {
            return;
        }
        if (result != null) {
            stats.setRated(menuId, (float) rating);
        }
        FoodMenuActivity act = activity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            if (result == null) {
                act.showToast(act.getString(R.string.rate_menu_error_string));
                return;
            }
            act.showToast(act.getString(R.string.menu_rated_success_successfully));
            act.launchUpdateMenuTask();
        }
    }
}
