package pt.ulisboa.tecnico.cmov.foodist.async.menu;

import android.util.Log;

import java.util.ArrayList;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;

public class UpdateMenuInfoTask extends BaseAsyncTask<String, Integer, Contract.UpdateMenuReply, FoodMenuActivity> {

    private FoodISTServerServiceBlockingStub stub;

    public UpdateMenuInfoTask(FoodMenuActivity activity) {
        super(activity);
        this.stub = activity.getGlobalStatus().getStub();
    }

    private static final String TAG = "UPDATE-MENU-INFO-TASK";


    @Override
    protected Contract.UpdateMenuReply doInBackground(String... menuId) {
        if (menuId.length != 1) {
            return null;
        }

        Contract.UpdateMenuRequest req = Contract.UpdateMenuRequest.newBuilder()
                .setMenuId(Long.parseLong(menuId[0]))
                .build();


        try {
            return this.stub.updateMenu(req);
        } catch (StatusRuntimeException e) {
            return null;
        }
    }

    @Override
    public void onPostExecute(Contract.UpdateMenuReply reply) {
        if (reply == null) {
            Log.e(TAG, "Menu does not exist");
            return;
        }
        getActivity().updatePhotos(new ArrayList<>(reply.getPhotoIDList()));
    }
}
