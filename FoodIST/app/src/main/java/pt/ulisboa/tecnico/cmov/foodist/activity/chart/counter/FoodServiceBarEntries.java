package pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Iterator;

import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;

public class FoodServiceBarEntries implements BarEntriesInterface {

    public ArrayList<BarEntry> calculate(Iterator menuIterator) {
        ArrayList<BarEntry> barEntries = new ArrayList();

        int one_stars = 0, two_stars = 0, three_stars = 0, four_stars = 0, five_stars = 0;
        while(menuIterator.hasNext()) {
            Menu menu = (Menu) menuIterator.next();
            Iterator<Double> ratingsIterator = menu.getRatings().iterator();

            while(ratingsIterator.hasNext()) {
                int current = ratingsIterator.next().intValue();
                switch(current) {
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
        }
        barEntries.add(new BarEntry(1, one_stars));
        barEntries.add(new BarEntry(2, two_stars));
        barEntries.add(new BarEntry(3, three_stars));
        barEntries.add(new BarEntry(4, four_stars));
        barEntries.add(new BarEntry(5, five_stars));

        return barEntries;
    }
}
