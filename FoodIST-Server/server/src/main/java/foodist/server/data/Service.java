package foodist.server.data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import foodist.server.data.exception.ServiceException;
import foodist.server.data.queue.LinearRegression;
import foodist.server.data.queue.Point;
import foodist.server.data.queue.QueuePosition;
import foodist.server.grpc.contract.Contract;

public class Service {

	private static final int MAX_FLAG_COUNT = 5;

	private final String name;
	private final Map<String, Menu> menus;

	/** Current people in the queue */
	private final Map<String, QueuePosition> queue;

	/** All Wait times seen currently */
	private final List<Point> queueWaitTimes;

	public Service(String name) {
		checkArguments(name);
		this.name = name;
		this.menus = new ConcurrentHashMap<>();
		this.queue = new ConcurrentHashMap<>();
		this.queueWaitTimes = Collections.synchronizedList(new ArrayList<>());
	}

	public void checkArguments(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException();
		}
	}

	public synchronized void addMenu(Menu menu) throws ServiceException {
		var curr = this.menus.putIfAbsent(menu.getTranslatedName("pt"), menu);
		if (curr != null) {
			throw new ServiceException();
		}
	}

	public synchronized List<Contract.Menu> getContractMenus(String language) {
		return this.menus.values().stream().filter(menu -> menu.getFlagCount() < MAX_FLAG_COUNT)
				.sorted(Comparator.comparing(Menu::getFlagCount)).map(menu -> menu.toContract(language))
				.collect(Collectors.toList());
	}

	public synchronized List<Contract.Menu> getContractMenus() {
		return this.menus.values().stream().filter(menu -> menu.getFlagCount() < MAX_FLAG_COUNT)
				.sorted(Comparator.comparing(Menu::getFlagCount)).map(Menu::toContract).collect(Collectors.toList());
	}

	public void resetMenus() {
		menus.clear();
	}

	public void addToQueue(String uuid) {
		queue.remove(uuid); // Remove entry from this user if it exists
		QueuePosition entry = new QueuePosition(LocalDateTime.now(), queue.size());
		queue.put(uuid, entry);
	}

	public void removeFromQueue(String uuid) {
		QueuePosition entry = removeQueueEntry(uuid);
		if (entry == null) {
			return;
		}
		//Remove previous entries for this service (if this user entered other users entered)
		queue.entrySet().stream()
		.filter(e -> e.getValue().getEntryTime().isBefore(entry.getEntryTime()))
		.map(e -> e.getKey())
		.forEach(this::removeQueueEntry);
	}
	
	public QueuePosition removeQueueEntry(String uuid) {
		QueuePosition entry = queue.remove(uuid);
		if (entry == null) {
			return null;
		}
		LocalDateTime currTime = LocalDateTime.now();
		double delta = (double) ChronoUnit.SECONDS.between(entry.getEntryTime(), currTime);
		queueWaitTimes.add(new Point(entry.getNumberOfPeople(), delta));
		return entry;
	}

	public void cancelQueueJoin(String uuid) {
		queue.remove(uuid);
	}

	public String currentQueueWaitTime() {
		int numPoints = queueWaitTimes.size();
		if (numPoints < 2) {
			return null;
		} else {
			Point[] regressionPoints;
			synchronized (queueWaitTimes) {
				regressionPoints = queueWaitTimes.toArray(new Point[0]);
			}
			LinearRegression regression = new LinearRegression(regressionPoints);
			if (regression.isValid()) {
				return String.valueOf((int) regression.predict(queue.size()));
			} else {
				return null;
			}
		}
	}

	public Map<String, QueuePosition> getQueue() {
		return queue;
	}

	public List<Point> getQueueWaitTimes() {
		return queueWaitTimes;
	}

	public String getName() {
		return name;
	}
}
