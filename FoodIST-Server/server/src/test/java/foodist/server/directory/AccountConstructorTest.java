package foodist.server.directory;

import foodist.server.data.Account;
import foodist.server.grpc.contract.Contract;
import org.junit.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AccountConstructorTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String INVALID_PASSWORD = "INVALID PASSWORD";

    private static Map<Contract.FoodType, Boolean> validPreferences;
    private static Map<Contract.FoodType, Boolean> invalidPreferences;


    @BeforeClass
    public static void oneTimeSetup() {
        validPreferences = new HashMap<>();
        invalidPreferences = new HashMap<>();
        Arrays.stream(Contract.FoodType.values()).forEach(type -> validPreferences.put(type, true));
        Arrays.stream(Contract.FoodType.values()).forEach(type -> invalidPreferences.put(type, true));
        invalidPreferences.remove(Contract.FoodType.Fish);
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
        new Account(USERNAME, PASSWORD, Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
        /*No exception should occur*/
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullUsernameTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(null, PASSWORD, Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPasswordTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, null, Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyUsername() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account("", PASSWORD, Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, "", Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankUsername() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(" ", PASSWORD, Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankPassword() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, " ", Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullLanguageTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, PASSWORD, null, Contract.Role.Student, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullRoleTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, null, Contract.Language.Portuguese, null, validPreferences);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPreferenceTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, null, Contract.Language.Portuguese, Contract.Role.Student, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPreferenceTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        new Account(USERNAME, null, Contract.Language.Portuguese, Contract.Role.Student, invalidPreferences);
    }

    @Test
    public void verifyPasswordTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        Account account = new Account(USERNAME, PASSWORD, Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
        assertTrue(account.checkPassword(PASSWORD));
    }

    @Test
    public void verifyWrongPasswordTest() throws InvalidKeySpecException, NoSuchAlgorithmException {
        Account account = new Account(USERNAME, PASSWORD, Contract.Language.Portuguese, Contract.Role.Student, validPreferences);
        assertFalse(account.checkPassword(INVALID_PASSWORD));
    }

}
