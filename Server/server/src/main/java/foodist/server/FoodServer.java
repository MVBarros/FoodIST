package foodist.server;

import foodist.server.service.FoodServiceImpl;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

import java.io.IOException;

public class FoodServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello World Server");

        final BindableService service = new FoodServiceImpl();
        Server server = NettyServerBuilder.forPort(Integer.parseInt(args[0])).addService(service).build();
        server.start();

        server.awaitTermination();
        return;
    }
}