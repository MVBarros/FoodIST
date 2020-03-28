package pt.ulisboa.tecnico.cmov.foodist.async;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;


public class UploadMenuTask extends AsyncTask<Menu, Integer, Boolean> {


    FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;

    public UploadMenuTask(FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub) {
        this.stub = stub;
    }

    private static final String TAG = "UPLOADMENU-TASK";

    @Override
    protected Boolean doInBackground(Menu... menu) {
        if (menu.length != 1) {
            return false;
        }
        Contract.AddMenuRequest.Builder addMenuBuilder = Contract.AddMenuRequest.newBuilder();

        addMenuBuilder.setFoodService(menu[0].getFoodServiceName());
        addMenuBuilder.setName(menu[0].getMenuName());
        addMenuBuilder.setPrice(menu[0].getPrice());

        Contract.AddMenuRequest request = addMenuBuilder.build();

        try {
            this.stub.addMenu(request);
        } catch (StatusRuntimeException e) {
            return false;
        }
        return true;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d(TAG, "Menu uploaded successfully");
            return;
        }
        Log.d(TAG, "Menu unable to be uploaded");
    }
}