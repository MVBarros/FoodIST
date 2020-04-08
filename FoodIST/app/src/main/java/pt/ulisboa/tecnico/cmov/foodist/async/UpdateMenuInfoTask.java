package pt.ulisboa.tecnico.cmov.foodist.async;

import android.util.Log;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;

public class UpdateMenuInfoTask extends BaseAsyncTask<String, Integer, Contract.Menu, FoodMenuActivity> {

    private FoodISTServerServiceBlockingStub stub;

    public UpdateMenuInfoTask(FoodMenuActivity activity) {
        super(activity);
        this.stub = activity.getGlobalStatus().getStub();
    }

    private static final String TAG = "UPDATE-MENU-INFO-TASK";


    @Override
    protected Contract.Menu doInBackground(String... foodService) {
        if (foodService.length != 2) {
            return null;
        }
        String foodService1 = foodService[0];
        String menuName = foodService[1];

        Contract.UpdateMenuRequest req = Contract.UpdateMenuRequest.newBuilder()
                .setFoodService(foodService1)
                .setMenuName(menuName)
                .build();


        try {
            return this.stub.updateMenu(req);
        } catch (StatusRuntimeException e) {
            return null;
        }
    }

    @Override
    public void onPostExecute(Contract.Menu menu) {
        if (menu == null) {
            Log.e(TAG, "Menu does not exist");
            return;
        }
        FoodMenuActivity activity = getActivity();
        String[] newPhotos = menu.getPhotoIdList().toArray(new String[menu.getPhotoIdCount()]);
        activity.updatePhotos(newPhotos);
    }
}
