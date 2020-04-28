package foodist.server.data;

import java.util.concurrent.atomic.AtomicLong;

public class Photo {

    private static final AtomicLong menuCounter = new AtomicLong(0);

    private long photoId;
    private byte[] content;

    public Photo(byte[] content) {
        checkArguments(content);
        this.photoId = menuCounter.getAndIncrement();
        this.content = content;
    }

    public void checkArguments(byte[] content) {
        if (content == null) {
            throw new IllegalArgumentException();
        }
    }

    public static void resetCounter() {
        menuCounter.set(0);
    }

    public long getPhotoId() {
        return photoId;
    }

    public byte[] getContent() {
        return content;
    }
}
