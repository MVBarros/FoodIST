package foodist.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import foodist.server.service.ServiceImplementation;
import foodist.server.thread.Cleanup;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.out.println("Usage: [prog] port");
            return;
        }
        
        final BindableService bindableService = new ServiceImplementation();
        int port = Integer.parseInt(args[0]);
        
        Server server = ServerBuilder.forPort(port).addService(bindableService).build();

        System.out.println("Server Starting");
        server.start();
        
        System.out.println("Starting memory cleaning thread");
        new Thread(new Cleanup(23, 59, 59, 999)).start();
        
        System.out.println("Server Started at port " + port);
        server.awaitTermination();
    }
}
