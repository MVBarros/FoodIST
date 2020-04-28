package foodist.server.data;

import foodist.server.grpc.contract.Contract;
import foodist.server.utils.TranslationUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Menu {

    private static final AtomicLong menuCounter = new AtomicLong(0);

    private final String name; //This name is always in english
    private final double price;
    private final List<Long> photos;
    private final Contract.FoodType type;
    private final String language;
    private final long menuId;
    private final Map<String, String> translatedNames;

    public Menu(String name, double price, Contract.FoodType type, String language, long menuId) {
        checkArguments(name, price, type, language);
        this.price = price;
        this.type = type;
        this.language = language;
        this.menuId = menuId;
        this.translatedNames = new HashMap<>();
        this.photos = Collections.synchronizedList(new ArrayList<>());
        this.name = name;
    }

    public void checkArguments(String name, double price, Contract.FoodType type, String language) {
        if (price < 0) {
            throw new IllegalArgumentException();
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (type == null) {
            throw new IllegalArgumentException();
        }
    }

    private synchronized String getTranslatedName(String language) {
        if (translatedNames.containsKey(language)) {
            return translatedNames.get(language);
        }
        if (this.language.equals(language)) {
            return this.name;
        }
        String translation = TranslationUtils.translate(name, this.language, language);
        translatedNames.put(language, translation);
        return translation;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public List<String> getPhotos() {
        return photos.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public List<String> getPhotos(int num) {
        return photos.stream()
                .limit(num)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public void addPhoto(long photo) {
        photos.add(photo);
    }

    public Contract.FoodType getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }

    public long getMenuId() {
        return menuId;
    }

    public static Menu fromContract(Contract.AddMenuRequest request) {
        String name = request.getName();
        double price = request.getPrice();
        Contract.FoodType type = request.getType();
        String language = request.getLanguage();
        long menuId = menuCounter.getAndIncrement();
        return new Menu(name, price, type, language, menuId);
    }

    public Contract.Menu toContract(String language) {
        Contract.Menu.Builder builder = Contract.Menu.newBuilder();
        builder.setOriginalName(this.name);
        builder.setTranslatedName(getTranslatedName(language));
        builder.setLanguage(this.language);
        builder.setMenuId(this.menuId);
        builder.addAllPhotoId(getPhotos());
        builder.setPrice(this.price);
        builder.setType(this.type);

        return builder.build();
    }


    public Contract.Menu toContract() {
        return toContract(this.language);
    }

    public static void resetCounter() {
        menuCounter.set(0);
    }

}
