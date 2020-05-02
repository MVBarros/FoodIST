package foodist.server.data;

import foodist.server.data.exception.ServiceException;
import foodist.server.grpc.contract.Contract;
import org.junit.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ServiceTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String NAME = "NAME";
    private static final String NAME2 = "NAME2";

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
        menu.flag();
        menu.flag();
        menu2.flag();

        assertEquals(service.getContractMenus().size(), 2);
        assertEquals(service.getContractMenus().get(0).getMenuId(), 1);
        assertEquals(service.getContractMenus().get(1).getMenuId(), 0);
        //No Exception is thrown
    }

    @Test
    public void tooManyFlagsTest() throws ServiceException {
        Service service = new Service(NAME);
        service.addMenu(menu);
        service.addMenu(menu2);
        for(int i = 0; i < 5; i++) {
            menu.flag();
        }
        menu2.flag();

        assertEquals(service.getContractMenus().size(), 1);
        assertEquals(service.getContractMenus().get(0).getMenuId(), 1);
        //No Exception is thrown
    }

}
