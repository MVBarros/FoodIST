package foodist.server.service;

import foodist.server.data.Menu;
import foodist.server.data.queue.Point;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GetQueueTimeTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ServiceImplementation impl;
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;


    private static final String NAME = "NAME";
    private static final double PRICE = 2.0d;
    private static final String LANGUAGE = "pt";
    private static final String SERVICE = "SERVICE";
    private static final String SERVICE2 = "SERVICE2";


    private static Contract.AddMenuRequest request;

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static Contract.Profile profile;

    @BeforeClass
    public static void oneTimeSetup() {
        request = Contract.AddMenuRequest.newBuilder()
                .setName(NAME)
                .setPrice(PRICE)
                .setLanguage(LANGUAGE)
                .setFoodService(SERVICE)
                .setType(Contract.FoodType.Meat)
                .build();

        Map<Integer, Boolean> preferences = new HashMap<>();
        preferences.put(Contract.FoodType.Vegan_VALUE, true);
        preferences.put(Contract.FoodType.Meat_VALUE, true);
        preferences.put(Contract.FoodType.Fish_VALUE, true);
        preferences.put(Contract.FoodType.Vegetarian_VALUE, true);

        profile = Contract.Profile.newBuilder()
                .setName(USERNAME)
                .setLanguage("pt")
                .setRole(Contract.Role.Student)
                .putAllPreferences(preferences)
                .build();

    }


    @Before
    public void setup() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        impl = new ServiceImplementation();

        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(impl).build().start());
        ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

        this.stub = FoodISTServerServiceGrpc.newBlockingStub(channel);

        String cookie = stub.register(Contract.RegisterRequest.newBuilder()
                .setProfile(profile)
                .setPassword(PASSWORD)
                .build()).getCookie();


        stub.addMenu(request.toBuilder()
                .setCookie(cookie)
                .build());
    }

    @After
    public void teardown() {
        Menu.resetCounter();
    }

    @Test
    public void serviceNoWaitingTime() {
        Contract.QueueTimeRequest request = Contract.QueueTimeRequest.newBuilder().addFoodService(SERVICE).build();
        Contract.QueueTimeResponse response = stub.getQueueTime(request);
        assertEquals(response.getQueueTimeCount(), 0);
    }

    @Test
    public void newServiceNoWaitingTime() {
        Contract.QueueTimeRequest request = Contract.QueueTimeRequest.newBuilder().addFoodService(SERVICE2).build();
        Contract.QueueTimeResponse response = stub.getQueueTime(request);
        assertEquals(response.getQueueTimeCount(), 0);
    }

    @Test
    public void insuficientInfoGetQueueTime() {
        impl.getService(SERVICE).getQueueWaitTimes().add(new Point(1d, 0d));
        Contract.QueueTimeRequest request = Contract.QueueTimeRequest.newBuilder().addFoodService(SERVICE).addFoodService(SERVICE2).build();
        Contract.QueueTimeResponse response = stub.getQueueTime(request);
        assertEquals(response.getQueueTimeCount(), 0);
    }


    @Test
    public void partialPredictGetQueueTime() {
        impl.getService(SERVICE).getQueueWaitTimes().add(new Point(1d, 4d));
        impl.getService(SERVICE).getQueueWaitTimes().add(new Point(2d, 6d));

        Contract.QueueTimeRequest request = Contract.QueueTimeRequest.newBuilder().addFoodService(SERVICE).addFoodService(SERVICE2).build();
        Contract.QueueTimeResponse response = stub.getQueueTime(request);
        assertEquals(response.getQueueTimeCount(), 1);
        assertEquals(response.getQueueTimeMap().get(SERVICE), String.valueOf(2));
    }

    @Test
    public void multipleValidGetQueueTime() {
        impl.getService(SERVICE).getQueueWaitTimes().add(new Point(1d, 4d));
        impl.getService(SERVICE).getQueueWaitTimes().add(new Point(2d, 6d));

        impl.getService(SERVICE2).getQueueWaitTimes().add(new Point(1d, 5d));
        impl.getService(SERVICE2).getQueueWaitTimes().add(new Point(5d, 11d));
        impl.getService(SERVICE2).getQueueWaitTimes().add(new Point(10d, 16d));


        Contract.QueueTimeRequest request = Contract.QueueTimeRequest.newBuilder().addFoodService(SERVICE).addFoodService(SERVICE2).build();
        Contract.QueueTimeResponse response = stub.getQueueTime(request);
        assertEquals(response.getQueueTimeCount(), 2);
        assertEquals(response.getQueueTimeMap().get(SERVICE), String.valueOf(2));
        assertEquals(response.getQueueTimeMap().get(SERVICE2), String.valueOf(4));

    }
}
