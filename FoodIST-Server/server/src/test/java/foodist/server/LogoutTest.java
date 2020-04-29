package foodist.server;

import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.service.ServiceImplementation;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LogoutTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ServiceImplementation impl;

    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;

    private static final String USERNAME = "USERNAME";
    private static final String INVALID_USERNAME = "INVALID USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String INVALID_PASSWORD = "INVALID PASSWORD";

    private static Map<Contract.FoodType, Boolean> validPreferences;
    private static Map<Integer, Boolean> preferences;

    private static Contract.Profile profile;

    private String cookie;

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

        cookie = stub.register(Contract.RegisterRequest.newBuilder().setProfile(profile).setPassword(PASSWORD).build()).getCookie();
    }

    @Test
    public void validLogoutTest() {
        stub.logout(Contract.LogoutRequest.newBuilder().setCookie(cookie).build());
        assertEquals(impl.getSessions().size(), 0);
    }

    @Test
    public void invalidLogoutTest() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.logout(Contract.LogoutRequest.newBuilder().setCookie("").build());
        } catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.UNAUTHENTICATED);
            throw e;
        }
    }

}
