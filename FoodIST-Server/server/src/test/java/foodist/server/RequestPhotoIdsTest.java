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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RequestPhotoIdsTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ServiceImplementation impl;
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;
    private FoodISTServerServiceGrpc.FoodISTServerServiceStub asyncStub;


    private static final String NAME = "NAME";
    private static final String NAME2 = "NAME2";
    private static final String NAME3 = "NAME3";
    private static final String NAME4 = "NAME4";

    private static final double PRICE = 2.0d;
    private static final String LANGUAGE = "pt";
    private static final String SERVICE = "SERVICE";
    private static final long PHOTO_ID = 0;

    private static final int CHUNK_SIZE = 1024;
    private static final int LONG_PHOTO_SIZE = CHUNK_SIZE * 1024 * 6;
    private static final int SHORT_PHOTO_SIZE = 3;


    private static Contract.AddMenuRequest request;
    private static Contract.AddMenuRequest request2;
    private static Contract.AddMenuRequest request3;
    private static Contract.AddMenuRequest request4;

    private long menuId;
    private long menuId2;
    private long menuId3;
    private long menuId4;

    private final byte[] shortPhoto = new byte[SHORT_PHOTO_SIZE];


    @BeforeClass
    public static void oneTimeSetup() {
        request = Contract.AddMenuRequest.newBuilder()
                .setName(NAME)
                .setPrice(PRICE)
                .setLanguage(LANGUAGE)
                .setFoodService(SERVICE)
                .setType(Contract.FoodType.Meat)
                .build();
        request2 = Contract.AddMenuRequest.newBuilder()
                .setName(NAME2)
                .setPrice(PRICE)
                .setLanguage(LANGUAGE)
                .setFoodService(SERVICE)
                .setType(Contract.FoodType.Meat)
                .build();
        request3 = Contract.AddMenuRequest.newBuilder()
                .setName(NAME3)
                .setPrice(PRICE)
                .setLanguage(LANGUAGE)
                .setFoodService(SERVICE)
                .setType(Contract.FoodType.Meat)
                .build();
        request4 = Contract.AddMenuRequest.newBuilder()
                .setName(NAME4)
                .setPrice(PRICE)
                .setLanguage(LANGUAGE)
                .setFoodService(SERVICE)
                .setType(Contract.FoodType.Meat)
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

        menuId = stub.addMenu(request).getMenuId();
        menuId2 = stub.addMenu(request2).getMenuId();
        menuId3 = stub.addMenu(request3).getMenuId();
        menuId4 = stub.addMenu(request4).getMenuId();

        for (int i = 0; i < 4; i++) {
            CountDownLatch latch = new CountDownLatch(1);
            StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
                @Override
                public void onNext(Empty value) {

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
                    .setSequenceNumber(0)
                    .build();

            observer.onNext(request);
            observer.onCompleted();

            latch.await();
        }

        for (int i = 0; i < 3; i++) {
            CountDownLatch latch = new CountDownLatch(1);
            StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
                @Override
                public void onNext(Empty value) {

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
                    .setMenuId(menuId2)
                    .setContent(ByteString.copyFrom(shortPhoto))
                    .setSequenceNumber(0)
                    .build();

            observer.onNext(request);
            observer.onCompleted();

            latch.await();
        }

        for (int i = 0; i < 2; i++) {
            CountDownLatch latch = new CountDownLatch(1);
            StreamObserver<Contract.AddPhotoRequest> observer = asyncStub.addPhoto(new StreamObserver<>() {
                @Override
                public void onNext(Empty value) {

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
                    .setMenuId(menuId3)
                    .setContent(ByteString.copyFrom(shortPhoto))
                    .setSequenceNumber(0)
                    .build();

            observer.onNext(request);
            observer.onCompleted();

            latch.await();
        }

    }

    @After
    public void teardown() {
        Menu.resetCounter();
        Photo.resetCounter();
    }

    @Test
    public void validTest() {
        Contract.PhotoReply reply = stub.requestPhotoIDs(Empty.newBuilder().build());
        Set<String> photoIds = new HashSet<>(reply.getPhotoIDList());

        assertEquals(reply.getPhotoIDCount(), 8);
        //First menu
        assertTrue(photoIds.contains(String.valueOf(PHOTO_ID)));
        assertTrue(photoIds.contains(String.valueOf(PHOTO_ID + 1)));
        assertTrue(photoIds.contains(String.valueOf(PHOTO_ID + 2)));
        //Jumps 4th photo

        //Second menu
        assertTrue(photoIds.contains(String.valueOf(PHOTO_ID + 4)));
        assertTrue(photoIds.contains(String.valueOf(PHOTO_ID + 5)));
        assertTrue(photoIds.contains(String.valueOf(PHOTO_ID + 6)));

        //Third menu
        assertTrue(photoIds.contains(String.valueOf(PHOTO_ID + 7)));
        assertTrue(photoIds.contains(String.valueOf(PHOTO_ID + 8)));

    }

}
