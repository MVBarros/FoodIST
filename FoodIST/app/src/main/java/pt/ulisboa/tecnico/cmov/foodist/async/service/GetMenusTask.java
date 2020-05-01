package pt.ulisboa.tecnico.cmov.foodist.async.service;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;


public class GetMenusTask extends BaseAsyncTask<String, Integer, List<Contract.Menu>, FoodServiceActivity> {

    private final FoodISTServerServiceBlockingStub stub;

    public GetMenusTask(FoodServiceActivity activity) {
        super(activity);
        this.stub = activity.getGlobalStatus().getStub();
    }

    private static final String TAG = "GET-MENU-TASK";

    @Override
    protected List<Contract.Menu> doInBackground(String... foodServices) {
        if (foodServices.length != 1) {
            return null;
        }
        String foodService = foodServices[0];
        SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getString(R.string.profile_file), 0);

        Contract.ListMenuRequest.Builder listMenuBuilder = Contract.ListMenuRequest.newBuilder();

        listMenuBuilder.setFoodService(foodService);
        listMenuBuilder.setLanguage(pref.getString(getActivity().getString(R.string.shared_prefs_profile_language), "en"));

        Contract.ListMenuRequest request = listMenuBuilder.build();

        try {
            Contract.ListMenuReply reply = this.stub.listMenu(request);
            return reply.getMenusList();
        } catch (StatusRuntimeException e) {
            return null;
        }
    }

    @Override
    public void onPostExecute(List<Contract.Menu> result) {
        if (result == null) {
            menuError(getActivity(), getActivity().getString(R.string.get_menu_task_error_getting_menus_message));
            return;
        }

        if (result.size() == 0) {
            getActivity().showToast(getActivity().getString(R.string.get_menu_task_no_menus_available_message));
            return;
        }

        List<Menu> menus = result.stream()
                .map(Menu::parseContractMenu)
                .collect(Collectors.toList());

        getActivity().setMenus(new ArrayList<>(menus));
        getActivity().drawServices();
        if (getActivity().getFilter()) {
            getActivity().showToast(getActivity().getString(R.string.get_menu_filter_menu_message));
        }
        getActivity().doShowAllButton();
        Log.d(TAG, "Menus obtained successfully");

    }

    private void menuError(FoodServiceActivity activity, String message) {
        activity.showToast(message);
        Log.d(TAG, "Unable to request menus");
    }

}
