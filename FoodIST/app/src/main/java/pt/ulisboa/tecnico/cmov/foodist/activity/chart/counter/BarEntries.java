package pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Iterator;

abstract class BarEntries {

    private int one_stars;
    private int two_stars;
    private int three_stars;
    private int four_stars;
    private int five_stars;

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
        if(one_stars!=0) {
            this.barEntries.add(new BarEntry(1, one_stars));
        }
        if(two_stars!=0) {
            this.barEntries.add(new BarEntry(2, two_stars));
        }
        if(three_stars!=0) {
            this.barEntries.add(new BarEntry(3, three_stars));
        }
        if(four_stars!=0) {
            this.barEntries.add(new BarEntry(4, four_stars));
        }
        if(five_stars!=0) {
            this.barEntries.add(new BarEntry(5, five_stars));
        }

        return this.barEntries;
    }

}
