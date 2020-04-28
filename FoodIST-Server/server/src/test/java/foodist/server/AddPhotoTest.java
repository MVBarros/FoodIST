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
    private static final int LONG_PHOTO_SIZE = CHUNK_SIZE * 6;
    private static final int SHORT_PHOTO_SIZE = 3;


    private static Contract.AddMenuRequest request;

    private long menuId;

    private final byte[] shortPhoto = new byte[SHORT_PHOTO_SIZE];
    private final byte[] longPhoto = new byte[LONG_PHOTO_SIZE];


    @BeforeClass
    public static void oneTimeSetup() {
        request = Contract.AddMenuRequest.newBuilder()
                .setName(NAME)
                .setPrice(PRICE)
                .setLanguage(LANGUAGE)
                .setFoodService(SERVICE)
                .setType(Contract.FoodType.Meat)
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

        menuId = stub.addMenu(request).getMenuId();
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
            public void onNext(Empty value) {

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
        assertNull(assertThrowable);
        assertEquals(impl.getPhotos().size(), 1);
        assertArrayEquals(impl.getPhotos().get(PHOTO_ID).getContent(), shortPhoto);
    }

    @Test
    public void validLongTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
            @Override
            public void onNext(Empty value) {

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

        int seq = 0;
        for (int i = 0; i < LONG_PHOTO_SIZE; i += CHUNK_SIZE) {
            byte[] chunk = Arrays.copyOfRange(longPhoto, i, i + CHUNK_SIZE);
            Contract.AddPhotoRequest request = Contract.AddPhotoRequest.newBuilder()
                    .setMenuId(menuId)
                    .setSequenceNumber(seq)
                    .setContent(ByteString.copyFrom(chunk))
                    .build();

            observer.onNext(request);
            seq++;
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
            public void onNext(Empty value) {

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
                .setContent(ByteString.copyFrom(shortPhoto))
                .build();

        observer.onNext(request);
        observer.onCompleted();

        latch.await();
        assertNotNull(assertThrowable);
        assertEquals(impl.getPhotos().size(), 0);
    }
}
