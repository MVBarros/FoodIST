package pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Iterator;

abstract class BarEntries {

    private int one_stars = 0;
    private int two_stars = 0;
    private int three_stars = 0;
    private int four_stars = 0;
    private int five_stars = 0;

    private ArrayList<BarEntry> barEntries = new ArrayList();

    abstract ArrayList<BarEntry> calculate(Iterator iterator);

    void incrementOneStars() {
        one_stars++;
    }
    void incrementTwoStars() {
        two_stars++;
    }
    void incrementThreeStars() {
        three_stars++;
    }
    void incrementFourStars() {
        four_stars++;
    }
    void incrementFiveStars() {
        five_stars++;
    }

    ArrayList<BarEntry> assemble() {
        this.barEntries.add(new BarEntry(1, one_stars));
        this.barEntries.add(new BarEntry(2, two_stars));
        this.barEntries.add(new BarEntry(3, three_stars));
        this.barEntries.add(new BarEntry(4, four_stars));
        this.barEntries.add(new BarEntry(5, five_stars));

        return this.barEntries;
    }

}
