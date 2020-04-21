package foodist.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import foodist.server.service.ServiceImplementation;
import foodist.server.thread.Cleanup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: [prog] port");
            return;
        }
        File priv = getPriv();
        File cert = getPub();
        
        final BindableService bindableService = new ServiceImplementation(false);
        int port = Integer.parseInt(args[0]);
        
        Server server = ServerBuilder
                .forPort(port)
                .useTransportSecurity(cert, priv)
                .addService(bindableService)
                .build();

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

    public static File getPub() throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource("server.pem");
        Path path = Paths.get(url.toURI());
        return path.toFile();
    }


    public static File getPriv() throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource("server.key");
        Path path = Paths.get(url.toURI());
        return path.toFile();
    }

}
