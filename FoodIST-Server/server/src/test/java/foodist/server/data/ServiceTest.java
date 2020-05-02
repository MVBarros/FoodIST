package foodist.server.data;

import foodist.server.data.exception.ServiceException;
import foodist.server.grpc.contract.Contract;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static foodist.server.data.Account.NUM_MENUS;
import static org.junit.Assert.*;

public class ServiceTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String NAME = "NAME";
    private static final String NAME2 = "NAME2";
    private static final String NAME3 = "NAME3";
    private static final String NAME4 = "NAME4";
    private static final String NAME5 = "NAME5";
    private static final String NAME6 = "NAME6";

    private static final double PRICE = 2.0d;
    private static final String LANGUAGE = "pt";

    private static Map<Contract.FoodType, Boolean> validPreferences;


    private Account account;
    private Menu menu;
    private Menu menu2;
    private Menu menu3;
    private Menu menu4;
    private Menu menu5;
    private Menu menu6;


    @BeforeClass
    public static void oneTimeSetup() {
        validPreferences = new HashMap<>();
        Arrays.stream(Contract.FoodType.values()).forEach(type -> validPreferences.put(type, true));
        validPreferences.remove(Contract.FoodType.UNRECOGNIZED);
    }

    @Before
    public void setup() throws InvalidKeySpecException, NoSuchAlgorithmException {
        account = new Account(USERNAME, PASSWORD, "pt", Contract.Role.Student, validPreferences);
        menu = new Menu(NAME, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        menu2 = new Menu(NAME2, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        menu3 = new Menu(NAME3, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        menu4 = new Menu(NAME4, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        menu5 = new Menu(NAME5, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        menu6 = new Menu(NAME6, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
    }

    @After
    public void teardown() {
        Menu.resetCounter();
    }

    @Test
    public void validTest() {
        Service service = new Service(NAME);
        assertEquals(service.getContractMenus().size(), 0);
        //No Exception is thrown
    }


    @Test(expected = IllegalArgumentException.class)
    public void nullUsername() {
        new Service(null);
    }

    @Test
    public void getMenusTest() throws ServiceException {
        Service service = new Service(NAME);
        service.addMenu(menu);
        assertEquals(service.getContractMenus().size(), 1);
        assertEquals(service.getContractMenus().get(0).getMenuId(), 0);
        //No Exception is thrown
    }

    @Test
    public void flagGetMenuTest() throws ServiceException {
        Service service = new Service(NAME);
        service.addMenu(menu);
        service.addMenu(menu2);
        menu.flag();
        menu.flag();
        menu2.flag();

        assertEquals(service.getContractMenus().size(), 2);
        assertEquals(service.getContractMenus().get(0).getMenuId(), 1);
        assertEquals(service.getContractMenus().get(1).getMenuId(), 0);
        assertEquals(account.getFlagCount(), 1);
        //No Exception is thrown
    }

    @Test
    public void tooManyFlagsTest() throws ServiceException {
        Service service = new Service(NAME);
        service.addMenu(menu);
        service.addMenu(menu2);
        for (int i = 0; i < 5; i++) {
            menu.flag();
        }
        menu2.flag();

        assertEquals(service.getContractMenus().size(), 1);
        assertEquals(service.getContractMenus().get(0).getMenuId(), 1);
        //No Exception is thrown
        assertEquals(account.getFlagCount(), 3);

    }


    @Test
    public void toManyMenusFlagTest() throws ServiceException {
        Service service = new Service(NAME);
        service.addMenu(menu);
        service.addMenu(menu2);
        service.addMenu(menu3);
        service.addMenu(menu4);
        service.addMenu(menu5);
        service.addMenu(menu6);

        menu.flag();
        menu.flag();
        menu.flag();
        menu2.flag();
        menu2.flag();
        menu2.flag();
        menu3.flag();
        menu3.flag();
        menu3.flag();
        menu4.flag();
        menu4.flag();
        menu5.flag();
        menu5.flag();
        menu6.flag();

        Set<Long> seen = new HashSet<>();
        assertEquals(service.getContractMenus().size(), 6);
        for(int i = 0; i < 3; i++) {
            assertTrue(service.getContractMenus().get(i).getMenuId() >= 3);
            assertTrue(service.getContractMenus().get(i).getMenuId() <= 5);
            assertFalse(seen.contains(service.getContractMenus().get(i).getMenuId()));
            seen.add(service.getContractMenus().get(i).getMenuId());
        }
        for(int i = 3; i < 6; i++) {
            assertTrue(service.getContractMenus().get(i).getMenuId() >= 0);
            assertTrue(service.getContractMenus().get(i).getMenuId() <= 2);
            assertFalse(seen.contains(service.getContractMenus().get(i).getMenuId()));
            seen.add(service.getContractMenus().get(i).getMenuId());
        }

        assertEquals(account.getRecentMenus().size(), NUM_MENUS);
        assertEquals(account.getRecentMenus().get(0).getMenuId(), 3);
        assertEquals(account.getRecentMenus().get(1).getMenuId(), 4);
        assertEquals(account.getRecentMenus().get(2).getMenuId(), 5);
        assertEquals(account.getFlagCount(), 1);

    }

}
