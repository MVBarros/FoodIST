package foodist.server.data;

import foodist.server.grpc.contract.Contract;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.queue.CircularFifoQueue;

public class Account {
    public static final int NUM_MENUS = 3;
    public static final int NUM_PHOTOS = 5;

    private static final int SALT_SIZE = 16;
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = SALT_SIZE * 8;
    public static final String PASSWORD_HASHING_ALGORITHM = "PBKDF2WithHmacSHA1";

    private final String username;
    private final byte[] password;
    private final byte[] salt;
    private final String language;
    private final Contract.Role role;
    private final Map<Contract.FoodType, Boolean> preferences;
    private final CircularFifoQueue<Menu> recentMenus;
    private final CircularFifoQueue<Photo> recentPhotos;

    public Account(String username, String password, String language, Contract.Role role,
                   Map<Contract.FoodType, Boolean> preferences) throws NoSuchAlgorithmException, InvalidKeySpecException {
        checkArguments(username, password, language, role, preferences);
        this.username = username;
        this.salt = generateSalt();
        this.password = hashPassword(password);
        this.language = language;
        this.role = role;
        this.preferences = preferences;
        this.recentMenus = new CircularFifoQueue<>(NUM_MENUS);
        this.recentPhotos = new CircularFifoQueue<>(NUM_PHOTOS);
    }

    public Account(String username, byte[] password, byte[] salt, String language, Contract.Role role,
                   Map<Contract.FoodType, Boolean> preferences) {
        checkArguments(language, role, preferences);
        this.username = username;
        this.salt = salt;
        this.password = password;
        this.language = language;
        this.role = role;
        this.preferences = preferences;
        this.recentMenus = new CircularFifoQueue<>();
        this.recentPhotos = new CircularFifoQueue<>();
    }


    public static void checkArguments(String username, String password, String language, Contract.Role role,
                               Map<Contract.FoodType, Boolean> preferences) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException();
        }
        checkArguments(language, role, preferences);
    }

    public static void checkArguments(String language, Contract.Role role,
                               Map<Contract.FoodType, Boolean> preferences) {
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (role == null) {
            throw new IllegalArgumentException();
        }
        if (!preferences.keySet().containsAll(Account.getAllFoodTypes())) {
            throw new IllegalArgumentException();
        }
    }

    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return salt;
    }

    public byte[] hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), this.salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PASSWORD_HASHING_ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    public boolean checkPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = this.salt;
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PASSWORD_HASHING_ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return Arrays.equals(hash, this.password);
    }

    public String getUsername() {
        return username;
    }

    public String getLanguage() {
        return language;
    }

    public Contract.Role getRole() {
        return role;
    }

    public Map<Contract.FoodType, Boolean> getPreferences() {
        return preferences;
    }

    public byte[] getPassword() {
        return password;
    }

    public byte[] getSalt() {
        return salt;
    }


    public synchronized void addMenu(Menu menu) {
        recentMenus.add(menu);
    }

    public synchronized void addPhoto(Photo photo) {
        recentPhotos.add(photo);
    }

    public Contract.Profile toProfile() {
        var builder = Contract.Profile.newBuilder();
        builder.setLanguage(this.language);
        builder.setName(this.username);
        builder.setRole(this.role);
        Map<Integer, Boolean> prefs;

        prefs = preferences.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getNumber(), Map.Entry::getValue));


        builder.putAllPreferences(prefs);
        return builder.build();
    }

    public Contract.AccountMessage toReply(String cookie) {
        return Contract.AccountMessage.newBuilder()
                .setProfile(this.toProfile())
                .setCookie(cookie)
                .build();
    }

    public static List<Contract.FoodType> getAllFoodTypes() {
        return Arrays.stream(Contract.FoodType.values())
                .filter(type -> type != Contract.FoodType.UNRECOGNIZED)
                .collect(Collectors.toList());
    }

    public static Account fromContract(Contract.Profile profile, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {

        Map<Contract.FoodType, Boolean> preferences = profile.getPreferencesMap().entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> Contract.FoodType.forNumber(entry.getKey()), Map.Entry::getValue));

        return new Account(profile.getName(), password,
                profile.getLanguage(), profile.getRole(), preferences);
    }


    public static Account fromContract(Contract.Profile profile, byte[] password, byte[] salt) {

        Map<Contract.FoodType, Boolean> preferences = profile.getPreferencesMap().entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> Contract.FoodType.forNumber(entry.getKey()), Map.Entry::getValue));

        return new Account(profile.getName(), password, salt, profile.getLanguage(), profile.getRole(), preferences);
    }

    public CircularFifoQueue<Menu> getRecentMenus() {
        return recentMenus;
    }

    public CircularFifoQueue<Photo> getRecentPhotos() {
        return recentPhotos;
    }

    public synchronized int getFlagCount() {
        int sumMenus = recentMenus.stream().mapToInt(Menu::getFlagCount).sum();
        int sumPhotos = recentPhotos.stream().mapToInt(Photo::getFlagCount).sum();
        int totalSum = sumMenus + sumPhotos;
        float val =  ((float) totalSum) / ((float) (NUM_PHOTOS + NUM_MENUS));
        return Math.round(val);
    }
}
