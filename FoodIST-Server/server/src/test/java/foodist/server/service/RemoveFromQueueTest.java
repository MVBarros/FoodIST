package foodist.server.service;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import foodist.server.data.Menu;
import foodist.server.data.queue.Point;
import foodist.server.data.queue.QueuePosition;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

public class RemoveFromQueueTest {

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
	private static final String UUID = "1";
	private static final String UUID2 = "2";

	private static final double DELTA = 0.00001;

	private static Contract.AddMenuRequest request;

	private static final String USERNAME = "USERNAME";
	private static final String PASSWORD = "PASSWORD";

	private static Contract.Profile profile;

	private String cookie;

	@BeforeClass
	public static void oneTimeSetup() {
		request = Contract.AddMenuRequest.newBuilder().setName(NAME).setPrice(PRICE).setLanguage(LANGUAGE)
				.setFoodService(SERVICE).setType(Contract.FoodType.Meat).build();

		Map<Integer, Boolean> preferences = new HashMap<>();
		preferences.put(Contract.FoodType.Vegan_VALUE, true);
		preferences.put(Contract.FoodType.Meat_VALUE, true);
		preferences.put(Contract.FoodType.Fish_VALUE, true);
		preferences.put(Contract.FoodType.Vegetarian_VALUE, true);

		profile = Contract.Profile.newBuilder().setName(USERNAME).setLanguage("pt").setRole(Contract.Role.Student)
				.putAllPreferences(preferences).build();

	}

	@Before
	public void setup() throws IOException {
		String serverName = InProcessServerBuilder.generateName();

		impl = new ServiceImplementation();

		grpcCleanup
				.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(impl).build().start());
		ManagedChannel channel = grpcCleanup
				.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

		this.stub = FoodISTServerServiceGrpc.newBlockingStub(channel);

		cookie = stub.register(Contract.RegisterRequest.newBuilder().setProfile(profile).setPassword(PASSWORD).build())
				.getCookie();

		stub.addMenu(request.toBuilder().setCookie(cookie).build());
		stub.addToQueue(Contract.QueueRequest.newBuilder().setFoodService(SERVICE).setUuid(UUID).build());

	}

	@After
	public void teardown() {
		Menu.resetCounter();
	}

	@Test
	public void validTest() {
		stub.removeFromQueue(Contract.QueueRequest.newBuilder().setFoodService(SERVICE).setUuid(UUID).build());
		Map<String, QueuePosition> queue = impl.getService(SERVICE).getQueue();
		assertEquals(queue.size(), 0);
		List<Point> queueWaitTimes = impl.getService(SERVICE).getQueueWaitTimes();
		assertEquals(queueWaitTimes.size(), 1);
		assertEquals(queueWaitTimes.get(0).getY(), 0, DELTA);
	}

	@Test
	public void validOneSecondIntervalTest() throws InterruptedException {
		Thread.sleep(1000);
		stub.removeFromQueue(Contract.QueueRequest.newBuilder().setFoodService(SERVICE).setUuid(UUID).build());
		Map<String, QueuePosition> queue = impl.getService(SERVICE).getQueue();
		assertEquals(queue.size(), 0);
		List<Point> queueWaitTimes = impl.getService(SERVICE).getQueueWaitTimes();
		assertEquals(queueWaitTimes.size(), 1);
		assertEquals(queueWaitTimes.get(0).getY(), 1, DELTA);
	}

	@Test
	public void noSuchUUIDTest() {
		stub.removeFromQueue(Contract.QueueRequest.newBuilder().setFoodService(SERVICE).setUuid(UUID2).build());
		Map<String, QueuePosition> queue = impl.getService(SERVICE).getQueue();
		assertEquals(queue.size(), 1);
		List<Point> queueWaitTimes = impl.getService(SERVICE).getQueueWaitTimes();
		assertEquals(queueWaitTimes.size(), 0);
	}

	@Test
	public void noSuchServiceTest() {
		stub.removeFromQueue(Contract.QueueRequest.newBuilder().setFoodService(SERVICE2).setUuid(UUID).build());
		Map<String, QueuePosition> queue = impl.getService(SERVICE2).getQueue();
		assertEquals(queue.size(), 0);
		List<Point> queueWaitTimes = impl.getService(SERVICE2).getQueueWaitTimes();
		assertEquals(queueWaitTimes.size(), 0);
	}
}
