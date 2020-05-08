package pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Iterator;

public class FoodMenuBarEnties extends BarEntries {

    @Override
    public ArrayList<BarEntry> calculate(Iterator iterator) {

        while (iterator.hasNext()) {
            Double rating = (Double) iterator.next();
            int current = rating.intValue();
            switch (current) {
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

        return this.assemble();
    }
}
