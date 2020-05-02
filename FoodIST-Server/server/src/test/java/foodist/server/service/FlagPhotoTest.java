package foodist.server.service;

import com.google.protobuf.ByteString;
import foodist.server.data.Menu;
import foodist.server.data.Photo;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

public class FlagPhotoTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ServiceImplementation impl;
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private FoodISTServerServiceGrpc.FoodISTServerServiceStub asyncStub;

    private static final String NAME = "NAME";
    private static final double PRICE = 2.0d;
    private static final String LANGUAGE = "pt";
    private static final String SERVICE = "SERVICE";
    private static final int SHORT_PHOTO_SIZE = 3;
    private static final byte[] shortPhoto = new byte[SHORT_PHOTO_SIZE];
    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";

    private static Contract.Profile profile;
    private static Contract.AddMenuRequest request;

    private String cookie;
    private String photoId;

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
    public void setup() throws IOException, InterruptedException {
        String serverName = InProcessServerBuilder.generateName();

        impl = new ServiceImplementation();

        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(impl).build().start());
        ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

        this.stub = FoodISTServerServiceGrpc.newBlockingStub(channel);
        this.asyncStub = FoodISTServerServiceGrpc.newStub(channel);

        cookie = stub.register(Contract.RegisterRequest.newBuilder()
                .setProfile(profile)
                .setPassword(PASSWORD)
                .build()).getCookie();

        long menuId = stub.addMenu(request.toBuilder()
                .setCookie(cookie)
                .build())
                .getMenuId();

        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
            @Override
            public void onNext(Contract.UploadPhotoReply value) {
                photoId = value.getPhotoID();
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Contract.AddPhotoRequest request = Contract.AddPhotoRequest.newBuilder()
                .setMenuId(menuId)
                .setContent(ByteString.copyFrom(shortPhoto))
                .setCookie(cookie)
                .build();

        observer.onNext(request);
        observer.onCompleted();

        latch.await();
    }

    @After
    public void teardown() {
        Menu.resetCounter();
        Photo.resetCounter();
    }

    @Test
    public void validTest() {
        stub.flagPhoto(Contract.FlagRequest.newBuilder().setCookie(cookie).setPhotoId(Long.parseLong(photoId)).build());
        assertEquals(impl.getPhotos().get(Long.parseLong(photoId)).getFlagCount(), 1);
    }

    @Test
    public void validRepeatedTest() {
        stub.flagPhoto(Contract.FlagRequest.newBuilder().setCookie(cookie).setPhotoId(Long.parseLong(photoId)).build());
        assertEquals(impl.getPhotos().get(Long.parseLong(photoId)).getFlagCount(), 1);
        stub.flagPhoto(Contract.FlagRequest.newBuilder().setCookie(cookie).setPhotoId(Long.parseLong(photoId)).build());
        assertEquals(impl.getPhotos().get(Long.parseLong(photoId)).getFlagCount(), 1);
    }

    @Test
    public void invalidCookie() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.flagPhoto(Contract.FlagRequest.newBuilder().setPhotoId(Long.parseLong(photoId)).build());
        } catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.UNAUTHENTICATED);
            assertEquals(impl.getPhotos().get(Long.parseLong(photoId)).getFlagCount(), 0);
            throw e;
        }
    }

    @Test
    public void invalidPhotoId() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.flagPhoto(Contract.FlagRequest.newBuilder().setPhotoId(10).setCookie(cookie).build());
        } catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.NOT_FOUND);
            assertEquals(impl.getPhotos().get(Long.parseLong(photoId)).getFlagCount(), 0);
            throw e;
        }
    }
}
