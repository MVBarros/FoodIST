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

import static org.junit.Assert.*;

public class LoginTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup    = new GrpcCleanupRule();

    private ServiceImplementation impl;

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;

    private static final String USERNAME = "USERNAME";
    private static final String INVALID_USERNAME = "INVALID USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String INVALID_PASSWORD = "INVALID PASSWORD";

    private static Map<Contract.FoodType, Boolean> validPreferences;
    private static Map<Integer, Boolean> preferences;

    private static Contract.Profile profile;

    @BeforeClass
    public static void oneTimeSetup() {
        validPreferences = new HashMap<>();
        Arrays.stream(Contract.FoodType.values()).forEach(type -> validPreferences.put(type, true));
        validPreferences.remove(Contract.FoodType.UNRECOGNIZED);

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
    }

    @Before
    public void setup() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        impl = new ServiceImplementation();

        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(impl).build().start());
        ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

        this.stub = FoodISTServerServiceGrpc.newBlockingStub(channel);

        stub.register(Contract.RegisterRequest.newBuilder().setProfile(profile).setPassword(PASSWORD).build());
    }

    @Test
    public void validLoginTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        var reply = stub.login(Contract.LoginRequest.newBuilder().setUsername(USERNAME).setPassword(PASSWORD).build());
        assertEquals(reply.getProfile().getPreferencesMap(), preferences);
        assertEquals(reply.getProfile().getRole(), Contract.Role.Student);
        assertEquals(reply.getProfile().getLanguage(), "pt");
        assertEquals(reply.getProfile().getName(), USERNAME);

        assertTrue(impl.getSessions().containsKey(reply.getCookie()));
        assertTrue(impl.getUsers().containsKey(USERNAME));

        Account account = impl.getSessions().get(reply.getCookie());
        assertTrue(account.checkPassword(PASSWORD));
        assertEquals(account.getLanguage(), "pt");
        assertEquals(account.getUsername(), USERNAME);
        assertEquals(account.getRole(), Contract.Role.Student);
        assertEquals(account.getPreferences(), validPreferences);
    }

    @Test
    public void invalidUsernameTest() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.login(Contract.LoginRequest.newBuilder().setUsername(INVALID_USERNAME).setPassword(PASSWORD).build());
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.UNAUTHENTICATED);
            throw e;
        }
    }

    @Test
    public void invalidPasswordTest() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.login(Contract.LoginRequest.newBuilder().setUsername(USERNAME).setPassword(INVALID_PASSWORD).build());
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.INVALID_ARGUMENT);
            throw e;
        }
    }
}
