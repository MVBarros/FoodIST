package foodist.server.data;

import foodist.server.data.exception.ServiceException;
import foodist.server.data.queue.LinearRegression;
import foodist.server.data.queue.Mean;
import foodist.server.data.queue.QueuePosition;
import foodist.server.grpc.contract.Contract;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Service {

    private static final int MAX_FLAG_COUNT = 5;

    private final String name;
    private final Map<String, Menu> menus;

    /**Current people in the queue*/
    private final Map<String, QueuePosition> queue;

    /**Maps the number of people in the queue to the wait time in seconds*/
    private final Map<Integer, Mean> queueWaitTimes;

    public Service(String name) {
        checkArguments(name);
        this.name = name;
        this.menus = new ConcurrentHashMap<>();
        this.queue = new ConcurrentHashMap<>();
        this.queueWaitTimes = new ConcurrentHashMap<>();
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
        return this.menus.values().stream()
                .filter(menu -> menu.getFlagCount() < MAX_FLAG_COUNT)
                .sorted(Comparator.comparing(Menu::getFlagCount))
                .map(menu -> menu.toContract(language))
                .collect(Collectors.toList());
    }

    public synchronized List<Contract.Menu> getContractMenus() {
        return this.menus.values().stream()
                .filter(menu -> menu.getFlagCount() < MAX_FLAG_COUNT)
                .sorted(Comparator.comparing(Menu::getFlagCount))
                .map(Menu::toContract)
                .collect(Collectors.toList());
    }

    public void resetMenus() {
        menus.clear();
    }

    public void addToQueue(String uuid) {
        queue.remove(uuid); //Just in case...
        QueuePosition entry = new QueuePosition(LocalDateTime.now(), queue.size());
        queue.put(uuid, entry);
    }

    public void removeFromQueue(String uuid) {
        LocalDateTime currTime = LocalDateTime.now();
        QueuePosition entry = queue.remove(uuid);
        if (entry == null) {
            return;
        }

        double delta = (double) ChronoUnit.SECONDS.between(entry.getEntryTime(), currTime);
        queueWaitTimes.computeIfAbsent(entry.getNumberOfPeople(), key -> new Mean()).add(delta);
    }

    public String currentQueueWaitTime() {
        if (queueWaitTimes.size() == 0) {
            return null;
        }
        int queueSize = queue.size();
        Mean waitTime = queueWaitTimes.get(queueSize);
        if (waitTime != null) {
            return String.valueOf((int)waitTime.getCurrValue());
        } else {
            return predictQueueWaitTime();
        }
    }

    private String predictQueueWaitTime() {
        synchronized (queueWaitTimes) {
            int queueSize = queue.size();
            double[] xAxis = queueWaitTimes.keySet().stream().mapToDouble(Integer::doubleValue).toArray();
            double[] yAxis = queueWaitTimes.values().stream().mapToDouble(Mean::getCurrValue).toArray();
            double value = new LinearRegression(xAxis, yAxis).predict(queueSize);
            return String.valueOf((int)value);
        }
    }

    public Map<String, QueuePosition> getQueue() {
        return queue;
    }

    public Map<Integer, Mean> getQueueWaitTimes() {
        return queueWaitTimes;
    }
}
