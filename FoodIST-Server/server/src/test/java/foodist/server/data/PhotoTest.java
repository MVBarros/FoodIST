package foodist.server.data;

import foodist.server.grpc.contract.Contract;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PhotoTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static Map<Contract.FoodType, Boolean> validPreferences;


    private static final byte[] PHOTO_CONTENT = new byte[0];
    private static final long PHOTO_ID = 0;

    private static final String NAME = "NAME";
    private static final double PRICE = 2.0d;
    private static final String LANGUAGE = "pt";


    private static Contract.AddMenuRequest request;
    private static Account account;
    private static Menu menu;

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
        account = new Account(USERNAME, PASSWORD, "pt", Contract.Role.Student, validPreferences);
        menu = new Menu(NAME, PRICE, Contract.FoodType.Meat, LANGUAGE, account);
    }

    @After
    public void teardown() {
        Menu.resetCounter();
        Photo.resetCounter();
    }

    @Test
    public void validTest() {
        Photo photo = new Photo(PHOTO_CONTENT, account);
        assertArrayEquals(photo.getContent(), PHOTO_CONTENT);
        assertEquals(photo.getPhotoId(), PHOTO_ID);
        assertEquals(photo.getFlagCount(), 0);
    }


    @Test
    public void flagPhotoTest() {
        Photo photo = new Photo(PHOTO_CONTENT, account);
        for(int i = 0; i < 6; i++) {
            photo.flag(String.valueOf(i));
            assertEquals(photo.getFlagCount(), i + 1);
        }
        assertEquals(account.getFlagCount(), 1);
        Photo photo2 = new Photo(PHOTO_CONTENT, account);
        assertEquals(photo2.getFlagCount(), 1);
    }

    @Test
    public void tooManyFlagsGetPhotoTest() {
        Photo photo = new Photo(PHOTO_CONTENT, account);
        Photo photo2 = new Photo(PHOTO_CONTENT, account);
        menu.addPhoto(photo);
        menu.addPhoto(photo2);
        for (int i = 0; i < 5; i++) {
            photo.flag(String.valueOf(i));
        }
        photo2.flag("0");

        assertEquals(menu.getPhotos().size(), 1);
        assertEquals(menu.getPhotos().get(0), "1");
        assertEquals(account.getFlagCount(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullContentTest() {
        new Photo(null, account);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAccountTest() {
        new Photo(PHOTO_CONTENT, null);
    }
}
