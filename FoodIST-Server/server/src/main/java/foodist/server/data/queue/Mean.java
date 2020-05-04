package foodist.server.data.queue;

public class Mean {

    private double currValue;
    private int numEntries;

    public Mean() {
        this.currValue = 0;
        this.numEntries = 0;
    }

    public Mean(double value) {
        this.currValue = value;
        this.numEntries = 1;
    }

    public synchronized double add(double value) {
        this.currValue = (this.currValue * numEntries + value) / (numEntries + 1);
        numEntries++;
        return this.currValue;
    }

    public double getCurrValue() {
        return currValue;
    }
}
