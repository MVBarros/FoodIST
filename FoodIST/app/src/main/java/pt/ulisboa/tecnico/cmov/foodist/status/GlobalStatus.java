package pt.ulisboa.tecnico.cmov.foodist.status;

import android.app.Application;

import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.cmov.foodist.R;
import io.grpc.android.AndroidChannelBuilder;

public class GlobalStatus extends Application {
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub = null;

    public FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub getStub() {
        if (stub == null) {
            String address = getString(R.string.foodist_server_address);
            int port = Integer.parseInt(getString(R.string.foodist_server_port));
            ManagedChannel channel = AndroidChannelBuilder.forAddress(address, port)
                                        .context(getApplicationContext())
                                        .usePlaintext()
                                        .build();
            stub = FoodISTServerServiceGrpc.newBlockingStub(channel);
        }
        return stub;
    }
}
