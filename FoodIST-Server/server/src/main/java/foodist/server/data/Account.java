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

public class Account {

    private static final int SALT_SIZE = 16;
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = SALT_SIZE * 8;

    private final String username;
    private final byte[] password;
    private final byte[] salt;

    private Contract.Language laguage;
    private Contract.Role role;
    private Map<Contract.FoodType, Boolean> preferences;

    public Account(String username, String password, Contract.Language language, Contract.Role role,
                   Map<Contract.FoodType, Boolean> preferences) throws NoSuchAlgorithmException, InvalidKeySpecException {
        checkArguments(username, password, language, role, preferences);
        this.username = username;
        this.salt = generateSalt();
        this.password = hashPassword(password);
        this.laguage = language;
        this.role = role;
        this.preferences = preferences;
    }

    public void checkArguments(String username, String password, Contract.Language language, Contract.Role role,
                               Map<Contract.FoodType, Boolean> preferences) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (language == null) {
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
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }

    public String getUsername() {
        return username;
    }

    public Contract.Language getLaguage() {
        return laguage;
    }

    public Contract.Role getRole() {
        return role;
    }

    public Map<Contract.FoodType, Boolean> getPreferences() {
        return preferences;
    }

    public synchronized void setLaguage(Contract.Language laguage) {
        this.laguage = laguage;
    }

    public synchronized void setRole(Contract.Role role) {
        this.role = role;
    }

    public synchronized void setPreferences(Map<Contract.FoodType, Boolean> preferences) {
        this.preferences = preferences;
    }

    public boolean checkPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = this.salt;
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return Arrays.equals(hash, this.password);
    }


    public Contract.Profile toProfile() {
        var builder = Contract.Profile.newBuilder();
        builder.setLanguage(this.laguage);
        builder.setName(this.username);
        builder.setRole(this.role);

        Map<Integer, Boolean> prefs = preferences.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getNumber(),
                        Map.Entry::getValue
                ));

        builder.putAllPreferences(prefs);
        return builder.build();
    }

    public Contract.AccountMessage toReply(String cookie) {
        return  Contract.AccountMessage.newBuilder()
                .setProfile(this.toProfile())
                .setCookie(cookie)
                .build();
    }

    public static List<Contract.FoodType> getAllFoodTypes() {
        var types = Contract.FoodType.values();
        return Arrays.stream(types)
                .filter(type -> type != Contract.FoodType.UNRECOGNIZED)
                .collect(Collectors.toList());
    }

    public static Account fromContract(Contract.Profile profile, String password) throws InvalidKeySpecException, NoSuchAlgorithmException {

        Map<Contract.FoodType, Boolean> preferences = profile.getPreferencesMap().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> Contract.FoodType.forNumber(entry.getKey()),
                        Map.Entry::getValue));

        return new Account(profile.getName(), password,
                profile.getLanguage(), profile.getRole(), preferences);
    }

}
