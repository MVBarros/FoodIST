package pt.ulisboa.tecnico.cmov.foodist.async.service;

import android.os.AsyncTask;
import android.util.Log;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.async.menu.UploadPhotoTask;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;
import pt.ulisboa.tecnico.cmov.foodist.domain.Photo;


public class UploadMenuTask extends AsyncTask<Menu, Integer, Contract.AddMenuReply> {


    private final FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private final UploadPhotoTask task;
    private final String taskPhoto;

    private static final String TAG = "UPLOADMENU-TASK";

    public UploadMenuTask(FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub) {
        this.stub = stub;
        this.task = null;
        this.taskPhoto = null;
    }


    public UploadMenuTask(FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub, UploadPhotoTask task, String taskPhoto) {
        this.stub = stub;
        this.task = task;
        this.taskPhoto = taskPhoto;
    }


    @Override
    protected Contract.AddMenuReply doInBackground(Menu... menu) {
        synchronized (stub) {
            if (menu.length != 1) {
                return null;
            }
            Contract.AddMenuRequest.Builder addMenuBuilder = Contract.AddMenuRequest.newBuilder();

            addMenuBuilder.setFoodService(menu[0].getFoodServiceName());
            addMenuBuilder.setName(menu[0].getMenuName());
            addMenuBuilder.setPrice(menu[0].getPrice());
            addMenuBuilder.setType(menu[0].getType());
            addMenuBuilder.setLanguage(menu[0].getLanguage());

            Contract.AddMenuRequest request = addMenuBuilder.build();

            try {
                return this.stub.addMenu(request);
            } catch (StatusRuntimeException e) {
                return null;
            }
        }

    }


    @Override
    protected void onPostExecute(Contract.AddMenuReply result) {
        if (result != null) {
            Log.d(TAG, "Menu uploaded successfully");
            if (task != null) {
                task.execute(new Photo(String.valueOf(result.getMenuId()), taskPhoto));
            }
            return;
        }
        Log.d(TAG, "Menu unable to be uploaded");

    }
}
