package pt.ulisboa.tecnico.cmov.foodist.async.menu;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen.FullscreenMapActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.fullscreen.FullscreenPhotoActivity;
import pt.ulisboa.tecnico.cmov.foodist.status.GlobalStatus;


public class FlagMenuTask extends AsyncTask<String, Boolean, Boolean> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private WeakReference<FoodMenuActivity> activity;
    private String cookie;
    private WeakReference<GlobalStatus> status;

    private String menuId;

    public FlagMenuTask(FoodMenuActivity activity) {
        this.activity = new WeakReference<>(activity);
        this.stub = activity.getGlobalStatus().getStub();
        this.cookie = activity.getGlobalStatus().getCookie();
        this.status = new WeakReference<>(activity.getGlobalStatus());
    }

    private static final String TAG = "DOWNLOAD-PHOTOS-TASK";

    @Override
    protected Boolean doInBackground(String... menus) {
        if (menus.length != 1) {
            return false;
        }
        menuId = menus[0];
        try {
            stub.flagMenu(Contract.FlagMenuRequest.newBuilder().setCookie(cookie).setMenuId(Long.parseLong(menuId)).build());
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
            stats.setMenuFlagged(menuId);
        }
        FoodMenuActivity act = activity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            if (!result) {
                act.showToast(act.getString(R.string.flag_food_menu_error_message));
                return;
            }
            act.showToast(act.getString(R.string.flag_food_menu_success_message));
            act.setFlagClickable();
        }
    }
}

