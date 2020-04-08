package pt.ulisboa.tecnico.cmov.foodist.cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class PhotoCache {

    private LruCache<String, Bitmap> cache;
    private static final int cacheSize = 100 * 1024 * 1024;
    public static PhotoCache instance;
    private static final String TAG = "TAG_PHOTO_CACHE";

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
            Log.d(TAG, String.format("Photo added: %s", photoID));
        }
        else{
            Log.d(TAG, String.format("Photo added already exists: %s", photoID));
        }
    }

    public synchronized Boolean addPhotoIfNotFull(String photoID, Bitmap photo){
        if(cache.size() + photo.getByteCount() > cacheSize){
            Log.d(TAG, "Cache Full");
            return false;
        }
        addPhoto(photoID, photo);
        return true;
    }

    public Bitmap getPhoto(String photoID){
        Bitmap photo = cache.get(photoID);
        if(photo == null){
            Log.d(TAG, String.format("Cache Miss: %s", photoID));
        }
        else{
            Log.d(TAG, String.format("Cache Hit: %s", photoID));
        }
        return photo;
    }

    public synchronized Bitmap addAndGetPhoto(String photoID, Bitmap photo){
        addPhoto(photoID, photo);
        return getPhoto(photoID);
    }
}
