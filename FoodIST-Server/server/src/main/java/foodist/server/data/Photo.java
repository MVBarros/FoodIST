package foodist.server.data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Photo {
    private static final AtomicLong menuCounter = new AtomicLong(0);

    private final long photoId;
    private final byte[] content;
    private final Account account;
    private final AtomicInteger flagCount;

    public Photo(byte[] content, Account account) {
        checkArguments(content, account);
        this.photoId = menuCounter.getAndIncrement();
        this.content = content;
        this.account = account;
        this.flagCount = new AtomicInteger(account.getFlagCount());
        account.addPhoto(this);
    }

    public static void checkArguments(byte[] content, Account account) {
        if (content == null) {
            throw new IllegalArgumentException();
        }
        if (account == null) {
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

    public int getFlagCount() {
        return flagCount.get();
    }

    public void flag() {
        flagCount.addAndGet(1);
    }
}
