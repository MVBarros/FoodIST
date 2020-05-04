package foodist.server.data.queue;

import java.time.LocalDateTime;

public class QueuePosition {
    private final LocalDateTime entryTime;
    private final int numberOfPeople;


    public QueuePosition(LocalDateTime entryTime, int numberOfPeople) {
        this.entryTime = entryTime;
        this.numberOfPeople = numberOfPeople;

    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

}
