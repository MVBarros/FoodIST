package pt.ulisboa.tecnico.cmov.foodist.async;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;


public class GetMenusTask extends AsyncTask<String, Integer, List<Contract.Menu>> {

    private WeakReference<FoodServiceActivity> foodServiceActivity;

    FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;

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
        Contract.ListMenuRequest.Builder listMenuBuilder = Contract.ListMenuRequest.newBuilder();

        listMenuBuilder.setFoodService(foodService[0]);

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
        ViewGroup foodServiceList = activity.findViewById(R.id.menus);
        if (result == null || result.size() == 0) {
            Log.d(TAG, "Unable to request menus");
            TextView errorText = new TextView(activity);
            errorText.setText(R.string.error_menus);
            foodServiceList.addView(errorText);
            return;
        }

        for(final Contract.Menu menu : result){
            //number of info
            LayoutInflater vi = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.food_menu, null);

            TextView name = v.findViewById(R.id.menuFood);
            TextView cost = v.findViewById(R.id.menuCost);

            name.setText(menu.getName());
            cost.setText(String.format(Locale.US,"%.2f", menu.getPrice()));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, FoodMenuActivity.class);
                    intent.putExtra("Number_photos", menu.getPhotoIdCount());
                    activity.startActivity(intent);
                }
            });

            foodServiceList.addView(v);
        }
        Log.d(TAG, "Menus obtained successfully");
    }
}
