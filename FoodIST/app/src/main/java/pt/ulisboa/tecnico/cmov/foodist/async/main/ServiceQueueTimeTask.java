package pt.ulisboa.tecnico.cmov.foodist.async.main;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.MainActivity;
import pt.ulisboa.tecnico.cmov.foodist.async.base.BaseAsyncTask;

public class ServiceQueueTimeTask extends BaseAsyncTask<String, Integer, Map<String, String>, MainActivity> {

    private static final String TAG = "TAG_SERVICEQUEUETIMETASK";

    public static AtomicBoolean isRunning = new AtomicBoolean(false);
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;

    public ServiceQueueTimeTask(MainActivity activity) {
        super(activity);
        this.stub = activity.getGlobalStatus().getStub();
    }

    @Override
    protected Map<String, String> doInBackground(String... services) {
        if (services == null || services.length != 1) {
            return null;
        }
        try {

            Contract.QueueTimeResponse response = stub.getQueueTime(Contract.QueueTimeRequest.newBuilder().addAllFoodService(Arrays.asList(services)).build());
            return response.getQueueTimeMap();
        } catch (StatusRuntimeException e) {
            return null;
        }

    }

    @Override
    public void onPostExecute(Map<String, String> result) {
        if (result == null) {
            getActivity().showToast(getActivity().getString(R.string.could_not_get_queue_time_message));
            return;
        }
        getActivity().getGlobalStatus().setQueueTimes(result);
        //Services of global status are now updated, just need to draw them
        // (If they have been overridden nothing new will happen)
        getActivity().drawServices();
    }
}

