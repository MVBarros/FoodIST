package foodist.server.data;

import foodist.server.data.exception.ServiceException;
import foodist.server.grpc.contract.Contract;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Service {

    private final String name;
    private final Map<String, Menu> menus;

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


    public void addMenu(Menu menu) throws ServiceException {
        var curr = this.menus.putIfAbsent(menu.getTranslatedName("pt"), menu);
        if (curr != null) {
            throw new ServiceException();
        }
    }

    public List<Contract.Menu> getContractMenus(String language) {
        return this.menus.values().stream()
                .map(menu -> menu.toContract(language))
                .collect(Collectors.toList());
    }

    public List<Contract.Menu> getContractMenus() {
        return this.menus.values().stream()
                .map(Menu::toContract)
                .collect(Collectors.toList());
    }

    public void resetMenus() {
        menus.clear();
    }
}
