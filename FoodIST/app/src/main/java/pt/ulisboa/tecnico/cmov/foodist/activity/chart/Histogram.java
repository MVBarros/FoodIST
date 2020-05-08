package pt.ulisboa.tecnico.cmov.foodist.activity.chart;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.domain.Menu;

public class Histogram extends BaseActivity {

    public void draw(Collection<Object> objects, BaseActivity baseActivity) {
        if (baseActivity instanceof FoodMenuActivity) {
            dFoodMenu(objects.iterator(), (FoodMenuActivity) baseActivity);
        }
        if (baseActivity instanceof FoodServiceActivity) {
            dFoodService(objects.iterator(), (FoodServiceActivity) baseActivity);
        }
    }

    private void dFoodMenu(Iterator ratingsIterator , FoodMenuActivity foodMenuActivity) {
        HorizontalBarChart chart = foodMenuActivity.findViewById(R.id.food_menu_histogram);
        ArrayList displayRatings = new ArrayList();

        int one_stars = 0, two_stars = 0, three_stars = 0, four_stars = 0, five_stars = 0;
        while(ratingsIterator.hasNext()) {
            Double rating = (Double) ratingsIterator.next();
            int current = rating.intValue();
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
        displayRatings.add(new BarEntry(1, one_stars));
        displayRatings.add(new BarEntry(2, two_stars));
        displayRatings.add(new BarEntry(3, three_stars));
        displayRatings.add(new BarEntry(4, four_stars));
        displayRatings.add(new BarEntry(5, five_stars));

        // Picks the chart type
        BarDataSet barDataSet = new BarDataSet(displayRatings, "Star ratings");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        // Set histogram bars
        BarData barData = new BarData(barDataSet);
        barData.setValueFormatter(new IntegerFormater());

        // Time to display bars
        chart.animateY(500);

        // Disables the Right Y axis
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setEnabled(false);

        // Operations regarding the Left Y axis
        chart.getAxisLeft().setValueFormatter(new IntegerFormater());
        chart.getAxisLeft().setGranularity(1);
        chart.getAxisLeft().setGranularityEnabled(true);

        ViewPortHandler handler = chart.getViewPortHandler();

        // Operations regarding the X axis
        chart.getXAxis().setGranularity(1);
        chart.getXAxis().setGranularityEnabled(true);
        chart.setVisibleXRange(1, 5);

        // Turns the default description label off
        chart.getDescription().setEnabled(false);

        // Sets the data onto the histogram
        chart.setData(barData);
    }

    private void dFoodService(Iterator menuIterator , FoodServiceActivity foodServiceActivity) {
        HorizontalBarChart chart = foodServiceActivity.findViewById(R.id.food_service_histogram);
        ArrayList displayRatings = new ArrayList();

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
        displayRatings.add(new BarEntry(1, one_stars));
        displayRatings.add(new BarEntry(2, two_stars));
        displayRatings.add(new BarEntry(3, three_stars));
        displayRatings.add(new BarEntry(4, four_stars));
        displayRatings.add(new BarEntry(5, five_stars));

        // Picks the chart type
        BarDataSet barDataSet = new BarDataSet(displayRatings, "Star ratings");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        // Set histogram bars
        BarData barData = new BarData(barDataSet);
        barData.setValueFormatter(new IntegerFormater());

        // Time to display bars
        chart.animateY(500);

        // Disables the Right Y axis
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisRight().setEnabled(false);

        // Operations regarding the Left Y axis
        chart.getAxisLeft().setValueFormatter(new IntegerFormater());
        chart.getAxisLeft().setGranularity(1);
        chart.getAxisLeft().setGranularityEnabled(true);

        ViewPortHandler handler = chart.getViewPortHandler();

        // Operations regarding the X axis
        chart.getXAxis().setGranularity(1);
        chart.getXAxis().setGranularityEnabled(true);
        chart.setVisibleXRange(1, 5);

        // Turns the default description label off
        chart.getDescription().setEnabled(false);

        // Sets the data onto the histogram
        chart.setData(barData);
    }

}
