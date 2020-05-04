package pt.ulisboa.tecnico.cmov.foodist.async.queue;

import android.os.AsyncTask;
import android.util.Log;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.StatusRuntimeException;

public class LeaveQueueTask extends AsyncTask<String, Integer, Boolean> {

    private static final String TAG = "REMOVE-QUEUE-TASK";

    private String uuid;
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;

    public LeaveQueueTask(String uuid, FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub) {
        this.uuid = uuid;
        this.stub = stub;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        if (strings == null || strings.length != 1) {
            return false;
        }
        Contract.QueueRequest request = Contract.QueueRequest.newBuilder()
                .setFoodService(strings[0])
                .setUuid(uuid)
                .build();
        try {
            stub.removeFromQueue(request);
        } catch (StatusRuntimeException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d(TAG, "Left queue successfully");
        } else {
            Log.d(TAG, "Could not leave queue");
        }
    }
}
