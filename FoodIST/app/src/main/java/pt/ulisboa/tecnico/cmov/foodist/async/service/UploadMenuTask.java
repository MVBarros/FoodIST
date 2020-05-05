package pt.ulisboa.tecnico.cmov.foodist.async.service;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.AddMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.menu.UploadPhotoTask;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;
import pt.ulisboa.tecnico.cmov.foodist.domain.Photo;


public class UploadMenuTask extends AsyncTask<Menu, Integer, Contract.AddMenuReply> {


    private final FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private final WeakReference<AddMenuActivity> activity;
    private final UploadPhotoTask task;
    private final String taskPhoto;
    private final String cookie;
    private final boolean hasPhotoTaken;
    private static final String TAG = "UPLOADMENU-TASK";


    public UploadMenuTask(AddMenuActivity activity, UploadPhotoTask task, boolean hasPhotoTaken, String taskPhoto, String cookie) {
        this.stub = activity.getGlobalStatus().getStub();
        this.task = task;
        this.hasPhotoTaken = hasPhotoTaken;
        this.taskPhoto = taskPhoto;
        this.cookie = cookie;
        this.activity = new WeakReference<>(activity);
    }


    @Override
    protected Contract.AddMenuReply doInBackground(Menu... menu) {
            if (menu.length != 1) {
                return null;
            }
            Contract.AddMenuRequest.Builder addMenuBuilder = Contract.AddMenuRequest.newBuilder();

            addMenuBuilder.setFoodService(menu[0].getFoodServiceName());
            addMenuBuilder.setName(menu[0].getMenuName());
            addMenuBuilder.setPrice(menu[0].getPrice());
            addMenuBuilder.setType(menu[0].getType());
            addMenuBuilder.setLanguage(menu[0].getLanguage());
            addMenuBuilder.setCookie(cookie);
            Contract.AddMenuRequest request = addMenuBuilder.build();

            try {
                return this.stub.addMenu(request);
            } catch (StatusRuntimeException e) {
                return null;
            }
    }


    @Override
    protected void onPostExecute(Contract.AddMenuReply result) {
        if (hasPhotoTaken && taskPhoto != null) {
            if (result != null) {
                task.execute(new Photo(String.valueOf(result.getMenuId()), taskPhoto));
                return;
            }
        }
        AddMenuActivity act = activity.get();
        if (act != null && !act.isFinishing() && !act.isDestroyed()) {
            if (result == null) {
               act.showToast(act.getString(R.string.menu_upload_error_message));
               return;
            }
            act.showToast(act.getString(R.string.Menu_uploaded_successfully_message));
            act.finish();
        }
    }
}
