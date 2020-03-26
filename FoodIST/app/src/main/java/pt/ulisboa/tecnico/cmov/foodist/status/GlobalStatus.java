package pt.ulisboa.tecnico.cmov.foodist.status;

import android.app.Application;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.android.AndroidChannelBuilder;
import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;

public class GlobalStatus extends Application {
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub = null;

    private List<FoodService> services = new ArrayList<>();

    private boolean freshBootFlag = true;

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


    public void updateServicesDistance(List<FoodService> services) {
        for (FoodService service: services) {
            for (FoodService myService: this.services) {
                if (myService.getName().equals(service.getName())) {
                    myService.setDistance(service.getDistance());
                    break;
                }
            }
        }
    }

    public String getApiKey() {
        return "AIzaSyCwTYl8KmYZE3J4vSQj7AMm2Hdk1RhjnyQ";
    }

    public boolean isFreshBootFlag() {
        return freshBootFlag;
    }

    public void setFreshBootFlag(boolean freshBootFlag) {
        this.freshBootFlag = freshBootFlag;
    }
}
