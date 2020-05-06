package pt.ulisboa.tecnico.cmov.foodist.async.menu;

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
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;

public class UploadRatingTask extends BaseAsyncTask<Double, Integer, Empty, FoodMenuActivity> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private GlobalStatus mContext;
    private String cookie;
    private String menuId;
    private String username;
    private double rating;

    public UploadRatingTask(String username, FoodMenuActivity activity) {
        super(activity);
        this.stub = activity.getGlobalStatus().getStub();
        this.menuId = activity.getMenuId();
        this.username = username;
        mContext = activity.getGlobalStatus();
        this.cookie = mContext.getCookie();
    }

    private static final String TAG = "UPLOAD-RATING-TASK";

    @Override
    protected Empty doInBackground(Double... doubles) {
        if (doubles.length == 0) {
            return null;
        }
        this.rating = doubles[0];
        Contract.RatingRequest.Builder ratingRequestBuilder = Contract.RatingRequest.newBuilder();

        ratingRequestBuilder.setUsername(username);
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
        if (result != null) {
            mContext.setRated(menuId, (float) rating);
            getActivity().launchUpdateMenuTask();
        } else {
            getActivity().showToast(getActivity().getString(R.string.rate_menu_test));
        }
        
    }

}
