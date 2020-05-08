package pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Iterator;

public class FoodMenuBarEnties implements BarEntriesInterface {

    @Override
    public ArrayList<BarEntry> calculate(Iterator iterator) {
        ArrayList<BarEntry> barEntries = new ArrayList();

        int one_stars = 0, two_stars = 0, three_stars = 0, four_stars = 0, five_stars = 0;
        while (iterator.hasNext()) {
            Double rating = (Double) iterator.next();
            int current = rating.intValue();
            switch (current) {
                case 1:
                    one_stars++;
                    break;
                case 2:
                    two_stars++;
                    break;
                case 3:
                    three_stars++;
                    break;
                case 4:
                    four_stars++;
                    break;
                case 5:
                    five_stars++;
                    break;
                default:
                    // This will not ever happen
                    break;
            }
        }
        barEntries.add(new BarEntry(1, one_stars));
        barEntries.add(new BarEntry(2, two_stars));
        barEntries.add(new BarEntry(3, three_stars));
        barEntries.add(new BarEntry(4, four_stars));
        barEntries.add(new BarEntry(5, five_stars));

        return barEntries;
    }
}
