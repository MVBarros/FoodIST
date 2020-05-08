package pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Iterator;

import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;

public class FoodServiceBarEntries extends BarEntries {

    public ArrayList<BarEntry> calculate(Iterator menuIterator) {

        while(menuIterator.hasNext()) {
            Menu menu = (Menu) menuIterator.next();
            Iterator<Double> ratingsIterator = menu.getRatings().iterator();

            while(ratingsIterator.hasNext()) {
                int current = ratingsIterator.next().intValue();
                switch(current) {
                    case 1:
                        incrementOneStars();
                        break;
                    case 2:
                        incrementTwoStars();
                        break;
                    case 3:
                        incrementThreeStars();
                        break;
                    case 4:
                        incrementFourStars();
                        break;
                    case 5:
                        incrementFiveStars();
                        break;
                    default:
                        // This will not ever happen
                        break;
                }
            }
        }

        return this.assemble();
    }
}
