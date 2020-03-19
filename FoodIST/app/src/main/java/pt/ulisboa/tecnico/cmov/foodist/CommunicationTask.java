package pt.ulisboa.tecnico.cmov.foodist;

import android.os.AsyncTask;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.cmov.protocol.FoodISTServiceGrpc;
import pt.ulisboa.tecnico.cmov.protocol.PingRequest;
import pt.ulisboa.tecnico.cmov.protocol.PingResponse;

public class CommunicationTask extends AsyncTask<Void, Void, Void> {

    private static final String PING = "ping";

    private String command;

    public CommunicationTask(String command) {
        this.command = command;
    }

    @Override
    protected Void doInBackground(Void... params) {
        switch (command) {
            case PING:
                ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
                FoodISTServiceGrpc.FoodISTServiceBlockingStub stub = FoodISTServiceGrpc.newBlockingStub(channel);
                PingResponse response = stub.ping(PingRequest.newBuilder().setPing("ping").build());
                channel.shutdown();
            default:
                System.out.println("That command does not exist...");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
