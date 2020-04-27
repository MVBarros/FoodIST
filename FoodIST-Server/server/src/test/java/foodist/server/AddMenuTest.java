package foodist.server;

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
import java.util.List;

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
    }

    @After
    public void teardown() {
        Menu.resetCounter();
    }

    @Test
    public void validTest() {
        stub.addMenu(request);
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
    }

    @Test
    public void repeatedMenu() {
        stub.addMenu(request);
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

        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.addMenu(request);
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.ALREADY_EXISTS);
            throw e;
        }
    }

    @Test
    public void invalidMenu() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.addMenu(request.toBuilder().setName("").build());
        }catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.INVALID_ARGUMENT);
            throw e;
        }
    }

}
