package foodist.server.data;

import foodist.server.grpc.contract.Contract;
import org.junit.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AccountConstructorTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String INVALID_PASSWORD = "INVALID PASSWORD";

    private static Map<Contract.FoodType, Boolean> validPreferences;
    private static Map<Contract.FoodType, Boolean> invalidPreferences;

    private static Contract.Profile profile;

    @BeforeClass
    public static void oneTimeSetup() {
        validPreferences = new HashMap<>();
        invalidPreferences = new HashMap<>();
        Arrays.stream(Contract.FoodType.values()).forEach(type -> validPreferences.put(type, true));
        Arrays.stream(Contract.FoodType.values()).forEach(type -> invalidPreferences.put(type, true));
        invalidPreferences.remove(Contract.FoodType.Fish);
        validPreferences.remove(Contract.FoodType.UNRECOGNIZED);
        invalidPreferences.remove(Contract.FoodType.UNRECOGNIZED);

        Map<Integer, Boolean> preferences = new HashMap<>();
        preferences.put(Contract.FoodType.Vegan_VALUE, true);
        preferences.put(Contract.FoodType.Meat_VALUE, true);
        preferences.put(Contract.FoodType.Fish_VALUE, true);
        preferences.put(Contract.FoodType.Vegetarian_VALUE, true);

        profile = Contract.Profile.newBuilder()
                .setName(USERNAME)
                .setLanguage(Contract.Language.pt)
                .setRole(Contract.Role.Student)
                .putAllPreferences(preferences)
                .build();
    }

    @Before
    public void setup() {

    }

    @After
    public void teardown() {

    }

    @AfterClass
    public static void oneTimeTeardown() {

    }

    @Test
    public void validTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        Account account = new Account(USERNAME, PASSWORD, Contract.Language.pt, Contract.Role.Student, validPreferences);
        assertTrue(account.checkPassword(PASSWORD));
        assertEquals(account.getLaguage(), Contract.Language.pt);
        assertEquals(account.getUsername(), USERNAME);
        assertEquals(account.getRole(), Contract.Role.Student);
        assertEquals(account.getPreferences(), validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullUsernameTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(null, PASSWORD, Contract.Language.pt, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPasswordTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, null, Contract.Language.pt, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyUsername() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account("", PASSWORD, Contract.Language.pt, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, "", Contract.Language.pt, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankUsername() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(" ", PASSWORD, Contract.Language.pt, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, " ", Contract.Language.pt, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullLanguageTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, PASSWORD, null, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullRoleTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, null, Contract.Language.pt, null, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPreferenceTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, null, Contract.Language.pt, Contract.Role.Student, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPreferenceTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, null, Contract.Language.pt, Contract.Role.Student, invalidPreferences);
    }

    @Test
    public void verifyWrongPasswordTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        Account account = new Account(USERNAME, PASSWORD, Contract.Language.pt, Contract.Role.Student, validPreferences);
        assertFalse(account.checkPassword(INVALID_PASSWORD));
    }

    @Test
    public void toContractTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        Account account = new Account(USERNAME, PASSWORD, Contract.Language.pt, Contract.Role.Student, validPreferences);
        Contract.Profile profile = account.toProfile();
        assertEquals(profile.getName(), USERNAME);
        assertEquals(profile.getLanguage(), Contract.Language.pt);
        assertEquals(profile.getRole(), Contract.Role.Student);
        assertTrue(profile.getPreferencesOrDefault(0, false));
        assertTrue(profile.getPreferencesOrDefault(1, false));
        assertTrue(profile.getPreferencesOrDefault(2, false));
        assertTrue(profile.getPreferencesOrDefault(3, false));
    }

    @Test
    public void fromContractTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        Account account = Account.fromContract(profile, PASSWORD);
        assertTrue(account.checkPassword(PASSWORD));
        assertEquals(account.getLaguage(), Contract.Language.pt);
        assertEquals(account.getUsername(), USERNAME);
        assertEquals(account.getRole(), Contract.Role.Student);
        assertEquals(account.getPreferences(), validPreferences);
    }
}
