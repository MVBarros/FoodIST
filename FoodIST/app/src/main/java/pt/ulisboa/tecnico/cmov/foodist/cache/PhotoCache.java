package pt.ulisboa.tecnico.cmov.foodist.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

public class PhotoCache {

    private LruCache<String, Bitmap> cache;
    private static final int cacheSize = 100 * 1024 * 1024;
    public static PhotoCache instance;

    private PhotoCache() {
        this.cache = new LruCache<String, Bitmap>(cacheSize){
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public static PhotoCache getInstance(){
        if(instance == null){
            instance = new PhotoCache();
        }
        return instance;
    }

    public synchronized void addPhoto(String photoID, Bitmap photo){
        if(cache.get(photoID) == null) {
            cache.put(photoID, photo);
        }
    }

    public Bitmap getPhoto(String photoID){
        return cache.get(photoID);
    }

    public synchronized Bitmap addAndGetPhoto(String photoID, Bitmap photo){
        addPhoto(photoID, photo);
        return getPhoto(photoID);
    }
}
