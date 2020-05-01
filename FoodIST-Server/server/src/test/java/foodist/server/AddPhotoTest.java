package foodist.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import foodist.server.data.Menu;
import foodist.server.data.Photo;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.service.ServiceImplementation;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class AddPhotoTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ServiceImplementation impl;
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private FoodISTServerServiceGrpc.FoodISTServerServiceStub asyncStub;

    private Throwable assertThrowable = null;


    private static final String NAME = "NAME";
    private static final double PRICE = 2.0d;
    private static final String LANGUAGE = "pt";
    private static final String SERVICE = "SERVICE";
    private static final long PHOTO_ID = 0;

    private static final int CHUNK_SIZE = 1024;
    private static final int LONG_PHOTO_SIZE = CHUNK_SIZE * 1024 * 6;
    private static final int SHORT_PHOTO_SIZE = 3;


    private static Contract.AddMenuRequest request;

    private long menuId;

    private final byte[] shortPhoto = new byte[SHORT_PHOTO_SIZE];
    private final byte[] longPhoto = new byte[LONG_PHOTO_SIZE];


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
        this.asyncStub = FoodISTServerServiceGrpc.newStub(channel);

        cookie = stub.register(Contract.RegisterRequest.newBuilder()
                .setProfile(profile)
                .setPassword(PASSWORD)
                .build()).getCookie();

        menuId = stub.addMenu(request.toBuilder()
                .setCookie(cookie)
                .build())
                .getMenuId();
    }

    @After
    public void teardown() {
        Menu.resetCounter();
        Photo.resetCounter();
    }

    @Test
    public void validShortTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
            @Override
            public void onNext(Contract.UploadPhotoReply value) {

            }

            @Override
            public void onError(Throwable t) {
                assertThrowable = t;
                latch.countDown();

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Contract.AddPhotoRequest request = Contract.AddPhotoRequest.newBuilder()
                .setMenuId(menuId)
                .setCookie(cookie)
                .setContent(ByteString.copyFrom(shortPhoto))
                .build();

        observer.onNext(request);
        observer.onCompleted();

        latch.await();
        assertNull(assertThrowable);
        assertEquals(impl.getPhotos().size(), 1);
        assertArrayEquals(impl.getPhotos().get(PHOTO_ID).getContent(), shortPhoto);
    }

    @Test
    public void validLongTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
            @Override
            public void onNext(Contract.UploadPhotoReply value) {

            }

            @Override
            public void onError(Throwable t) {
                assertThrowable = t;
                latch.countDown();

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        for (int i = 0,  seq = 0; i < LONG_PHOTO_SIZE; i += CHUNK_SIZE, seq++) {
            byte[] chunk = Arrays.copyOfRange(longPhoto, i, i + CHUNK_SIZE);
            Contract.AddPhotoRequest request = Contract.AddPhotoRequest.newBuilder()
                    .setMenuId(menuId)
                    .setSequenceNumber(seq)
                    .setCookie(cookie)
                    .setContent(ByteString.copyFrom(chunk))
                    .build();

            observer.onNext(request);
        }
        observer.onCompleted();

        latch.await();
        assertNull(assertThrowable);
        assertEquals(impl.getPhotos().size(), 1);
        assertArrayEquals(impl.getPhotos().get(PHOTO_ID).getContent(), longPhoto);
    }

    @Test
    public void invalidMenuIdTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
            @Override
            public void onNext(Contract.UploadPhotoReply value) {

            }

            @Override
            public void onError(Throwable t) {
                assertThrowable = t;
                latch.countDown();

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Contract.AddPhotoRequest request = Contract.AddPhotoRequest.newBuilder()
                .setMenuId(-1)
                .setCookie(cookie)
                .setContent(ByteString.copyFrom(shortPhoto))
                .build();

        observer.onNext(request);
        observer.onCompleted();

        latch.await();
        assertNotNull(assertThrowable);
        assertEquals(impl.getPhotos().size(), 0);
    }

    @Test
    public void invalidCookieTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
            @Override
            public void onNext(Contract.UploadPhotoReply value) {

            }

            @Override
            public void onError(Throwable t) {
                assertThrowable = t;
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
                .build();

        observer.onNext(request);
        observer.onCompleted();

        latch.await();
        assertNotNull(assertThrowable);
        assertEquals(impl.getPhotos().size(), 0);
    }

}
