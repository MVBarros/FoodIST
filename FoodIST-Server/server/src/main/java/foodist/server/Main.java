package foodist.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import foodist.server.service.ServiceImplementation;
import foodist.server.thread.Cleanup;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Lisbon"));
        ZonedDateTime nextRun = now.withHour(20).withMinute(57).withSecond(59);
        
        if(now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusDays(1);
        }
        Duration duration = Duration.between(now, nextRun);
        long initial_delay = duration.getSeconds();
        
        System.out.println("Starting memory cleaning thread");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);   
        scheduler.scheduleAtFixedRate(new Cleanup(), initial_delay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        
        System.out.println("Server Started at port " + port);
        server.awaitTermination();
    }
}
