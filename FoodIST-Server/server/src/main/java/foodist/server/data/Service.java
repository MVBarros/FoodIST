package foodist.server.data;

import foodist.server.data.exception.ServiceException;
import foodist.server.grpc.contract.Contract;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Service {

    private static final int MAX_FLAG_COUNT = 5;

    private final String name;
    private final Map<String, Menu> menus;
    private AtomicInteger queue = new AtomicInteger(0);

    public Service(String name) {
        checkArguments(name);
        this.name = name;
        this.menus = new ConcurrentHashMap<>();
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

    public void addToQueue() { queue.incrementAndGet(); }

    public void removeFromQueue() { queue.decrementAndGet(); }

    public String getQueueTime() { return Integer.toString(queue.get()); }

}
