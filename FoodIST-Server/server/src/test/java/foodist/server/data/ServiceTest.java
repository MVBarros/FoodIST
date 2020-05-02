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
        menu.flag("0");
        menu.flag("1");
        menu2.flag("2");

        assertEquals(service.getContractMenus().size(), 2);
        assertEquals(service.getContractMenus().get(0).getMenuId(), 1);
        assertEquals(service.getContractMenus().get(1).getMenuId(), 0);
        assertEquals(account.getFlagCount(), 0);
        //No Exception is thrown
    }

    @Test
    public void tooManyFlagsGetMenuTest() throws ServiceException {
        Service service = new Service(NAME);
        service.addMenu(menu);
        service.addMenu(menu2);
        for (int i = 0; i < 5; i++) {
            menu.flag(String.valueOf(i));
        }
        menu2.flag("0");

        assertEquals(service.getContractMenus().size(), 1);
        assertEquals(service.getContractMenus().get(0).getMenuId(), 1);
        //No Exception is thrown
        assertEquals(account.getFlagCount(), 1);
    }


    @Test
    public void getMenuOrderTest() throws ServiceException {
        Menu menu3 = new Menu(NAME3, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        Menu menu4 = new Menu(NAME4, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        Menu menu5 = new Menu(NAME5, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        Menu menu6 = new Menu(NAME6, PRICE, Contract.FoodType.Meat, LANGUAGE, account);

        Service service = new Service(NAME);
        service.addMenu(menu);
        service.addMenu(menu2);
        service.addMenu(menu3);
        service.addMenu(menu4);
        service.addMenu(menu5);
        service.addMenu(menu6);

        for(int i = 0; i <3; i++) {
            menu.flag(String.valueOf(i));
            menu2.flag(String.valueOf(i));
            menu3.flag(String.valueOf(i));

        }
        for(int i = 0; i <2; i++) {
            menu4.flag(String.valueOf(i));
            menu5.flag(String.valueOf(i));
        }
        menu6.flag("0");

        Set<Long> seen = new HashSet<>();
        assertEquals(service.getContractMenus().size(), 6);
        assertEquals(service.getContractMenus().get(0).getMenuId(), 5);
        seen.add(service.getContractMenus().get(0).getMenuId());

        for(int i = 1; i < 3; i++) {
            //The two following menus are 4 and 5 in any order
            assertTrue(service.getContractMenus().get(i).getMenuId() >= 3);
            assertTrue(service.getContractMenus().get(i).getMenuId() <= 4);
            assertFalse(seen.contains(service.getContractMenus().get(i).getMenuId()));
            seen.add(service.getContractMenus().get(i).getMenuId());
        }
        for(int i = 3; i < 6; i++) {
            //The two following menus are 0, 1 and 2 in any order
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
