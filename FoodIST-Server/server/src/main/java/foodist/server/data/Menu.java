package foodist.server.data;

import foodist.server.grpc.contract.Contract;
import foodist.server.utils.TranslationUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Menu {
    private static final int MAX_FLAG_COUNT = 5;
    private static final AtomicLong menuCounter = new AtomicLong(0);
     
    private final String name;
    private final double price;
    private final List<Photo> photos;
    private final Contract.FoodType type;
    private final String language;
    private final long menuId;
    private final Map<String, String> translatedNames;
    private final Account account;
    private final int initialFlagCount;
    private final Set<String> userFlags;
    private final Map<String, Double> userRatings;
    
    public Menu(String name, double price, Contract.FoodType type, String language, Account account) {
        checkArguments(name, price, type, language, account);
        this.price = price;
        this.type = type;
        this.language = language;
        this.menuId = menuCounter.getAndIncrement();
        this.translatedNames = new ConcurrentHashMap<>();
        this.photos = Collections.synchronizedList(new ArrayList<>());
        this.name = name;
        this.account = account;
        this.initialFlagCount = account.getFlagCount();
        this.userFlags = Collections.synchronizedSet(new HashSet<>());
        account.addMenu(this);
        this.userRatings = new HashMap<String, Double>();
    }
        
    public static void checkArguments(String name, double price, Contract.FoodType type, String language, Account account) {
        if (account == null) {
            throw new IllegalArgumentException();
        }
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

    public synchronized String getTranslatedName(String language) {
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

    public synchronized List<String> getPhotos() {
        return photos.stream()
                .filter(photo -> photo.getFlagCount() < MAX_FLAG_COUNT)
                .sorted(Comparator.comparing(Photo::getFlagCount))
                .map(Photo::getPhotoId)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public synchronized List<String> getPhotos(int num) {
        return photos.stream()
                .filter(photo -> photo.getFlagCount() < MAX_FLAG_COUNT)
                .sorted(Comparator.comparing(Photo::getFlagCount))
                .limit(num)
                .map(Photo::getPhotoId)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public synchronized void addPhoto(Photo photo) {
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

    public Account getAccount() {
        return account;
    }

    public static Menu fromContract(Contract.AddMenuRequest request, Account account) {
        String name = request.getName();
        double price = request.getPrice();
        Contract.FoodType type = request.getType();
        String language = request.getLanguage();
        return new Menu(name, price, type, language, account);
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
        builder.setRating(averageRating());

        return builder.build();
    }


    public Contract.Menu toContract() {
        return toContract(this.language);
    }

    public int getFlagCount() {
        return initialFlagCount + userFlags.size();
    }

    public void flag(String username) {
        userFlags.add(username);
    }

    public static void resetCounter() {
        menuCounter.set(0);
    }
    
    public synchronized void addRating(String username, double rating) {
    	userRatings.put(username, rating);
    }
    
    public synchronized double averageRating() {
    	double average = -1.0;     	
    	if(!userRatings.isEmpty()) {    		
    		average = userRatings.entrySet().stream().mapToDouble(Map.Entry::getValue).average().orElse(Double.NaN); 
    	}
    	return average;
    }
    
    public Collection<Double> getRatings() {
    	return userRatings.values();
    }

}
