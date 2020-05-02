package foodist.server.data;

import java.util.concurrent.atomic.AtomicLong;

public class Photo {

    private static final AtomicLong menuCounter = new AtomicLong(0);

    private final long photoId;
    private final byte[] content;
    private final Account account;

    public Photo(byte[] content, Account account) {
        checkArguments(content);
        this.photoId = menuCounter.getAndIncrement();
        this.content = content;
        this.account = account;
        account.addPhoto(this);
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
