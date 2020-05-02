package foodist.server.service;

import foodist.server.data.Account;
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
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChangeProfileTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ServiceImplementation impl;
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private String cookie;

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static Map<Contract.FoodType, Boolean> validPreferences;
    private static Map<Contract.FoodType, Boolean> validPreferences2;

    private static Map<Integer, Boolean> preferences;
    private static Map<Integer, Boolean> preferences2;

    private static Contract.Profile profile;
    private static Contract.Profile profile2;


    @BeforeClass
    public static void oneTimeSetup() {
        validPreferences = new HashMap<>();
        Arrays.stream(Contract.FoodType.values()).forEach(type -> validPreferences.put(type, true));
        validPreferences.remove(Contract.FoodType.UNRECOGNIZED);

        validPreferences2 = new HashMap<>();
        Arrays.stream(Contract.FoodType.values()).forEach(type -> validPreferences2.put(type, false));
        validPreferences2.remove(Contract.FoodType.UNRECOGNIZED);

        preferences = new HashMap<>();
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

        preferences2 = new HashMap<>();
        preferences2.put(Contract.FoodType.Vegan_VALUE, false);
        preferences2.put(Contract.FoodType.Meat_VALUE, false);
        preferences2.put(Contract.FoodType.Fish_VALUE, false);
        preferences2.put(Contract.FoodType.Vegetarian_VALUE, false);


        profile2 = Contract.Profile.newBuilder()
                .setName(USERNAME)
                .setLanguage("en")
                .setRole(Contract.Role.Professor)
                .putAllPreferences(preferences2)
                .build();

    }

    @Before
    public void setup() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        impl = new ServiceImplementation();

        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(impl).build().start());
        ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

        this.stub = FoodISTServerServiceGrpc.newBlockingStub(channel);
        cookie = stub.register(Contract.RegisterRequest.newBuilder().setProfile(profile).setPassword(PASSWORD).build()).getCookie();

    }

    @After
    public void teardown() {

    }

    @AfterClass
    public static void oneTimeTeardown() {

    }

    @Test
    public void validChangeProfile() throws InvalidKeySpecException, NoSuchAlgorithmException {
        stub.changeProfile(Contract.AccountMessage.newBuilder().setProfile(profile2).setCookie(cookie).build());

        Account account = impl.getSessions().get(cookie);
        assertTrue(account.checkPassword(PASSWORD));
        assertEquals(account.getLanguage(), "en");
        assertEquals(account.getUsername(), USERNAME);
        assertEquals(account.getRole(), Contract.Role.Professor);
        assertEquals(account.getPreferences(), validPreferences2);

        account = impl.getUsers().get(USERNAME);
        assertTrue(account.checkPassword(PASSWORD));
        assertEquals(account.getLanguage(), "en");
        assertEquals(account.getUsername(), USERNAME);
        assertEquals(account.getRole(), Contract.Role.Professor);
        assertEquals(account.getPreferences(), validPreferences2);
    }

    @Test
    public void unauthenticatedChangeProfile() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.changeProfile(Contract.AccountMessage.newBuilder().setProfile(profile2).setCookie("").build());
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.UNAUTHENTICATED);
            throw e;
        }
    }

    @Test
    public void invalidChangeProfile() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.changeProfile(Contract.AccountMessage.newBuilder().setCookie(cookie).build());
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.INVALID_ARGUMENT);
            throw e;
        }
    }
}
