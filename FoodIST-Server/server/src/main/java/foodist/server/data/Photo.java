package foodist.server.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class Photo {
    private static final AtomicLong menuCounter = new AtomicLong(0);

    private final long photoId;
    private final byte[] content;
    private final Account account;
    private final int initialFlagCount;
    private final Set<String> userFlags;

    public Photo(byte[] content, Account account) {
        checkArguments(content, account);
        this.photoId = menuCounter.getAndIncrement();
        this.content = content;
        this.account = account;
        this.initialFlagCount = account.getFlagCount();
        this.userFlags = Collections.synchronizedSet(new HashSet<>());
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
        return initialFlagCount + userFlags.size();
    }

    public void flag(String username) {
        userFlags.add(username);
    }
}
