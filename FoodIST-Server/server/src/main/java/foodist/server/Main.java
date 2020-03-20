package foodist.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import foodist.server.service.ServiceImplementation;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.out.println("Usage: [prog] port");
            return;
        }
        final BindableService bindableService = new ServiceImplementation();
        int port = Integer.parseInt(args[0]);
        
        Server server = NettyServerBuilder
                .forPort(port)
                .addService(bindableService)
                .build();

        System.out.println("Server Starting");
        server.start();

        System.out.println("Server Started, terminate with Ctrl+C");
        server.awaitTermination();
    }
}
