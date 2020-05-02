package foodist.server.data;

import foodist.server.grpc.contract.Contract;
import org.junit.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MenuTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String NAME = "NAME";
    private static final String NAME2 = "NAME2";
    private static final double PRICE = 2.0d;
    private static final double DELTA = 0.01d;
    private static final String LANGUAGE = "pt";
    private static final long MENU_ID = 0;

    private static Map<Contract.FoodType, Boolean> validPreferences;
    private static Contract.AddMenuRequest request;
    private static Account account;

    @BeforeClass
    public static void oneTimeSetup() {
        validPreferences = new HashMap<>();
        Arrays.stream(Contract.FoodType.values()).forEach(type -> validPreferences.put(type, true));
        validPreferences.remove(Contract.FoodType.UNRECOGNIZED);

        request = Contract.AddMenuRequest.newBuilder()
                .setName(NAME)
                .setPrice(PRICE)
                .setLanguage(LANGUAGE)
                .setType(Contract.FoodType.Meat)
                .build();

    }

    @Before
    public void setup() throws InvalidKeySpecException, NoSuchAlgorithmException {
        account = new Account(USERNAME, PASSWORD, LANGUAGE, Contract.Role.Student, validPreferences);

    }
    @After
    public void teardown() {
        Menu.resetCounter();
    }

    @Test
    public void validTest() {
        Menu menu = new Menu(NAME, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        assertEquals(menu.getName(), NAME);
        assertEquals(menu.getLanguage(), LANGUAGE);
        assertEquals(menu.getMenuId(), MENU_ID);
        assertEquals(menu.getPrice(), PRICE, DELTA);
        assertEquals(menu.getType(), Contract.FoodType.Meat);
        assertEquals(menu.getAccount().getUsername(), USERNAME);
        assertEquals(menu.getAccount().getRecentMenus().size(), 1);
        assertEquals(menu.getAccount().getRecentMenus().get(0), menu);
        assertEquals(menu.getPhotos(), new ArrayList<>());

        Contract.Menu contractMenu = menu.toContract();
        assertEquals(contractMenu.getOriginalName(), NAME);
        assertEquals(contractMenu.getLanguage(), LANGUAGE);
        assertEquals(contractMenu.getMenuId(), MENU_ID);
        assertEquals(contractMenu.getPrice(), PRICE, DELTA);
        assertEquals(contractMenu.getType(), Contract.FoodType.Meat);
        assertEquals(contractMenu.getPhotoIdList().size(), 0);
    }

    @Test
    public void menuFlagTest() {
        Menu menu = new Menu(NAME, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        for(int i = 0; i < 6; i++) {
            menu.flag(String.valueOf(i));
            assertEquals(menu.getFlagCount(), i + 1);
        }
        assertEquals(account.getFlagCount(), 1);
        Menu menu2 = new Menu(NAME2, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        assertEquals(menu2.getFlagCount(), 1);
    }

    @Test
    public void repeatedFlagCount() {
        Menu menu = new Menu(NAME, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        for(int i = 0; i < 6; i++) {
            menu.flag("0");
            assertEquals(menu.getFlagCount(), 1);
        }
        assertEquals(account.getFlagCount(), 0);
        Menu menu2 = new Menu(NAME2, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
        assertEquals(menu2.getFlagCount(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullName() {
        new Menu(null, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
    }


    @Test(expected = IllegalArgumentException.class)
    public void nullType() {
        new Menu(NAME, PRICE, null, LANGUAGE, account);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullLanguage() {
        new Menu(NAME, PRICE, Contract.FoodType.Meat, null, account);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAccount() {
        new Menu(NAME, PRICE, Contract.FoodType.Meat, LANGUAGE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativePrice() {
        new Menu(NAME, -5d, Contract.FoodType.Meat, null, account);
    }

    @Test
    public void fromContractTest() {
        Menu menu = Menu.fromContract(request, account);
        assertEquals(menu.getName(), NAME);
        assertEquals(menu.getLanguage(), LANGUAGE);
        assertEquals(menu.getMenuId(), MENU_ID);
        assertEquals(menu.getPrice(), PRICE, DELTA);
        assertEquals(menu.getType(), Contract.FoodType.Meat);
        assertEquals(menu.getPhotos(), new ArrayList<>());
    }

}
