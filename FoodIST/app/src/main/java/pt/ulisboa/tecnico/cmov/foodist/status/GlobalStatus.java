package pt.ulisboa.tecnico.cmov.foodist.status;

import android.app.Application;

import java.util.List;

import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import pt.ulisboa.tecnico.cmov.foodist.R;
import io.grpc.android.AndroidChannelBuilder;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;

public class GlobalStatus extends Application {
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub = null;

    private List<FoodService> services;

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

    public List<FoodService> getServices() {
        return services;
    }

    public void setServices(List<FoodService> services) {
        this.services = services;
    }


}
