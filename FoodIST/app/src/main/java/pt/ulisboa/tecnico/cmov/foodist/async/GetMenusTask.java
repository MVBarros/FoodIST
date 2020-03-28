package pt.ulisboa.tecnico.cmov.foodist.async;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.adapters.MenuAdapter;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;


public class GetMenusTask extends AsyncTask<String, Integer, List<Contract.Menu>> {

    private WeakReference<FoodServiceActivity> foodServiceActivity;

    FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;

    private String foodService;
    public GetMenusTask(FoodServiceActivity foodServiceActivity, FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub) {
        this.foodServiceActivity = new WeakReference<>(foodServiceActivity);
        this.stub = stub;
    }

    private static final String TAG = "GETMENU-TASK";

    @Override
    protected List<Contract.Menu> doInBackground(String... foodService) {
        if(foodService.length != 1){
            return null;
        }
        this.foodService = foodService[0];

        Contract.ListMenuRequest.Builder listMenuBuilder = Contract.ListMenuRequest.newBuilder();

        listMenuBuilder.setFoodService(this.foodService);

        Contract.ListMenuRequest request = listMenuBuilder.build();

        try{
            Contract.ListMenuReply reply = this.stub.listMenu(request);
            return reply.getMenusList();
        } catch (StatusRuntimeException e){
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Contract.Menu> result) {
        final FoodServiceActivity activity = foodServiceActivity.get();
        ListView foodServiceList = activity.findViewById(R.id.menus);

        if (result == null || result.size() == 0) {
            menuError(activity, foodServiceList);
            return;
        }

        ArrayList<Menu> menus = new ArrayList<>();

        for(final Contract.Menu menu : result){
            menus.add(Menu.parseContractMenu(this.foodService, menu));
        }

        final MenuAdapter menuAdapter = new MenuAdapter(activity, menus);

        foodServiceList.setAdapter(menuAdapter);
        Log.d(TAG, "Menus obtained successfully");

    }

    private void menuError(FoodServiceActivity activity, ListView foodServiceList){
        Log.d(TAG, "Unable to request menus");
        Toast.makeText(activity, "No menus available...", Toast.LENGTH_SHORT).show();
    }

}
