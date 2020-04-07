package pt.ulisboa.tecnico.cmov.foodist.async;

import android.util.Log;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;

public class UpdateMenuInfoTask extends BaseAsyncTask<String, Integer, Contract.Menu, FoodMenuActivity> {

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private String foodService;
    private String menuName;

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
        this.foodService = foodService[0];
        this.menuName = foodService[1];

        Contract.UpdateMenuRequest req = Contract.UpdateMenuRequest.newBuilder()
                .setFoodService(this.foodService)
                .setMenuName(menuName)
                .build();


        try {
            Contract.Menu reply = this.stub.updateMenu(req);
            return reply;
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
        int currPhotoIndex = activity.getNumPhoto();
        String[] currPhotos = activity.getPhotoIDs();

        String[] newPhotos = menu.getPhotoIdList().toArray(new String[menu.getPhotoIdCount()]);
        int newIndex = 0;
        if (currPhotos.length != 0) {
            int i = 0;
            for (String photo : newPhotos) {
                if (photo.equals(currPhotos[currPhotoIndex])) {
                    newIndex = i;
                    break;
                }
                i++;
            }
        }
        activity.setPhotoIDs(newPhotos);
        activity.setNumPhoto(newIndex);
        activity.downloadCurrentPhoto();
    }
}
