package foodist.server.service;

import foodist.server.data.Menu;
import foodist.server.data.Service;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.service.ServiceImplementation;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AddMenuTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ServiceImplementation impl;
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;


    private static final String NAME = "NAME";
    private static final double PRICE = 2.0d;
    private static final double DELTA = 0.01d;
    private static final String LANGUAGE = "pt";
    private static final String SERVICE = "SERVICE";
    private static final long MENU_ID = 0;

    private static Contract.AddMenuRequest request;

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static Contract.Profile profile;

    private String cookie;

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
        cookie = stub.register(Contract.RegisterRequest.newBuilder()
                .setProfile(profile)
                .setPassword(PASSWORD)
                .build()).getCookie();
    }

    @After
    public void teardown() {
        Menu.resetCounter();
    }

    @Test
    public void validTest() {
        Contract.AddMenuReply reply = stub.addMenu(request
                .toBuilder()
                .setCookie(cookie)
                .build());
        Service service = impl.getService(SERVICE);
        List<Contract.Menu> menus = service.getContractMenus();
        assertEquals(menus.size(), 1);
        Contract.Menu menu = menus.get(0);
        assertEquals(menu.getPhotoIdList().size(), 0);
        assertEquals(menu.getLanguage(), LANGUAGE);
        assertEquals(menu.getOriginalName(), NAME);
        assertEquals(menu.getTranslatedName(), NAME);
        assertEquals(menu.getMenuId(), MENU_ID);
        assertEquals(menu.getType(), Contract.FoodType.Meat);
        assertEquals(menu.getPrice(), PRICE, DELTA);
        assertEquals(reply.getMenuId(), MENU_ID);

    }

    @Test
    public void repeatedMenu() {
        Contract.AddMenuReply reply = stub.addMenu(request
                .toBuilder()
                .setCookie(cookie)
                .build());
        Service service = impl.getService(SERVICE);
        List<Contract.Menu> menus = service.getContractMenus();
        assertEquals(menus.size(), 1);
        Contract.Menu menu = menus.get(0);
        assertEquals(menu.getPhotoIdList().size(), 0);
        assertEquals(menu.getLanguage(), LANGUAGE);
        assertEquals(menu.getOriginalName(), NAME);
        assertEquals(menu.getTranslatedName(), NAME);
        assertEquals(menu.getMenuId(), MENU_ID);
        assertEquals(menu.getType(), Contract.FoodType.Meat);
        assertEquals(menu.getPrice(), PRICE, DELTA);
        assertEquals(reply.getMenuId(), MENU_ID);

        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.addMenu(request
                    .toBuilder()
                    .setCookie(cookie)
                    .build());
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.ALREADY_EXISTS);
            throw e;
        }
    }

    @Test
    public void invalidMenu() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.addMenu(request.toBuilder().setName("").setCookie(cookie).build());
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.INVALID_ARGUMENT);
            throw e;
        }
    }

    @Test
    public void invalidSession() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.addMenu(request);
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.UNAUTHENTICATED);
            throw e;
        }
    }

}
