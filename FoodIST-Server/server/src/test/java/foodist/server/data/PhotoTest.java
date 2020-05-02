package foodist.server.data;

import foodist.server.grpc.contract.Contract;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static foodist.server.data.Account.NUM_PHOTOS;
import static org.junit.Assert.*;

public class PhotoTest {

    private static final String USERNAME = "USERNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final byte[] PHOTO_CONTENT = new byte[0];
    private static final long PHOTO_ID = 0;
    private static final String NAME = "NAME";
    private static final double PRICE = 2.0d;
    private static final String LANGUAGE = "pt";

    private static Map<Contract.FoodType, Boolean> validPreferences;
    private static Account account;
    private static Menu menu;

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
    public void flagSinglePhotoTest() {
        Photo photo = new Photo(PHOTO_CONTENT, account);
        photo.flag(String.valueOf(0));
        assertEquals(photo.getFlagCount(), 1);

        Photo photo2 = new Photo(PHOTO_CONTENT, account);
        photo2.flag(String.valueOf(0));
        photo2.flag(String.valueOf(1));
        assertEquals(photo2.getFlagCount(), 2);


        Photo photo3 = new Photo(PHOTO_CONTENT, account);
        photo3.flag(String.valueOf(0));
        photo3.flag(String.valueOf(1));
        photo3.flag(String.valueOf(2));
        assertEquals(photo3.getFlagCount(), 3);

        menu.addPhoto(photo2);
        menu.addPhoto(photo);
        menu.addPhoto(photo3);

        Contract.Menu contractMenu = menu.toContract();
        //Get photos ordered by flag count
        assertEquals(contractMenu.getPhotoId(0), "0");
        assertEquals(contractMenu.getPhotoId(1), "1");
        assertEquals(contractMenu.getPhotoId(2), "2");
    }

    @Test
    public void flagPhotoTest() {
        Photo photo = new Photo(PHOTO_CONTENT, account);
        for (int i = 0; i < 6; i++) {
            photo.flag(String.valueOf(i));
            assertEquals(photo.getFlagCount(), i + 1);
        }
        assertEquals(account.getFlagCount(), 1);
        Photo photo2 = new Photo(PHOTO_CONTENT, account);
        assertEquals(photo2.getFlagCount(), 1);
    }

    @Test
    public void repeatedFlagCount() {
        Photo photo = new Photo(PHOTO_CONTENT, account);
        for (int i = 0; i < 6; i++) {
            photo.flag("0");
            assertEquals(photo.getFlagCount(), 1);
        }
        assertEquals(account.getFlagCount(), 0);
        Photo photo2 = new Photo(PHOTO_CONTENT, account);
        assertEquals(photo2.getFlagCount(), 0);
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

    @Test
    public void getPhotoOrderTest() {
        Photo photo = new Photo(PHOTO_CONTENT, account);
        Photo photo2 = new Photo(PHOTO_CONTENT, account);
        Photo photo3 = new Photo(PHOTO_CONTENT, account);
        Photo photo4 = new Photo(PHOTO_CONTENT, account);
        Photo photo5 = new Photo(PHOTO_CONTENT, account);
        Photo photo6 = new Photo(PHOTO_CONTENT, account);

        menu.addPhoto(photo);
        menu.addPhoto(photo2);
        menu.addPhoto(photo3);
        menu.addPhoto(photo4);
        menu.addPhoto(photo5);
        menu.addPhoto(photo6);


        for (int i = 0; i < 4; i++) {
            photo.flag(String.valueOf(i));
            photo2.flag(String.valueOf(i));
            photo3.flag(String.valueOf(i));
        }

        for (int i = 0; i < 2; i++) {
            photo4.flag(String.valueOf(i));
            photo5.flag(String.valueOf(i));
        }
        photo6.flag("0");

        Set<String> seen = new HashSet<>();
        assertEquals(menu.getPhotos().size(), 6);
        assertEquals(menu.getPhotos().get(0), "5");
        seen.add(menu.getPhotos().get(0));

        for (int i = 1; i < 3; i++) {
            //The two following menus are 4 and 5 in any order
            assertTrue(menu.getPhotos().get(i).equals("3") || menu.getPhotos().get(i).equals("4"));
            assertFalse(seen.contains(menu.getPhotos().get(i)));
            seen.add(menu.getPhotos().get(i));
        }
        for (int i = 3; i < 6; i++) {
            //The two following menus are 0, 1 and 2 in any order
            assertTrue(menu.getPhotos().get(i).equals("0") || menu.getPhotos().get(i).equals("1") || menu.getPhotos().get(i).equals("2"));
            assertFalse(seen.contains(menu.getPhotos().get(i)));
            seen.add(menu.getPhotos().get(i));
        }

        assertEquals(account.getRecentPhotos().size(), NUM_PHOTOS);
        assertEquals(account.getRecentPhotos().get(0).getPhotoId(), 1);
        assertEquals(account.getRecentPhotos().get(1).getPhotoId(), 2);
        assertEquals(account.getRecentPhotos().get(2).getPhotoId(), 3);
        assertEquals(account.getRecentPhotos().get(3).getPhotoId(), 4);
        assertEquals(account.getRecentPhotos().get(4).getPhotoId(), 5);

        assertEquals(account.getFlagCount(), 2);
    }
}
