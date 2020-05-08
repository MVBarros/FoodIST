package pt.ulisboa.tecnico.cmov.foodist.activity.chart;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collection;

import pt.ulisboa.tecnico.cmov.foodist.R;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodMenuActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.FoodServiceActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.base.BaseActivity;
import pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter.FoodMenuBarEnties;
import pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter.FoodServiceBarEntries;
import pt.ulisboa.tecnico.cmov.foodist.domain.FoodService;

public class Histogram extends BaseActivity {

    public void draw(Collection<Object> objects, BaseActivity baseActivity) {
        ArrayList<BarEntry> barEntries;
        if (baseActivity instanceof FoodMenuActivity) {
            barEntries = new FoodMenuBarEnties().calculate(objects.iterator());
            HorizontalBarChart chart = this.getChart((FoodMenuActivity) baseActivity);
            this.displayGraph(chart, barEntries);
        }
        if (baseActivity instanceof FoodServiceActivity) {
            barEntries = new FoodServiceBarEntries().calculate(objects.iterator());
            HorizontalBarChart chart = this.getChart((FoodServiceActivity) baseActivity);
            this.displayGraph(chart, barEntries);
        }

    }

    private void displayGraph(HorizontalBarChart horizontalBarChart, ArrayList<BarEntry> barEntries) {
        if(barEntries!=null || !barEntries.isEmpty()) {
            // Picks the chart type
            BarDataSet barDataSet = new BarDataSet(barEntries, "Star ratings");
            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            // Set histogram bars
            BarData barData = new BarData(barDataSet);
            barData.setValueFormatter(new IntegerFormater());

            // Time to display bars
            horizontalBarChart.animateY(500);

            // Disables the Right Y axis
            horizontalBarChart.getAxisRight().setDrawLabels(false);
            horizontalBarChart.getAxisRight().setEnabled(false);

            // Operations regarding the Left Y axis
            horizontalBarChart.getAxisLeft().setValueFormatter(new IntegerFormater());
            horizontalBarChart.getAxisLeft().setGranularity(1);
            horizontalBarChart.getAxisLeft().setGranularityEnabled(true);

            // Operations regarding the X axis
            horizontalBarChart.getXAxis().setGranularity(1);
            horizontalBarChart.getXAxis().setGranularityEnabled(true);
            horizontalBarChart.setVisibleXRange(1, 5);

            // Turns the default description label off
            horizontalBarChart.getDescription().setEnabled(false);

            // Sets the data onto the histogram
            horizontalBarChart.setData(barData);
        }
    }

    private HorizontalBarChart getChart(FoodMenuActivity foodMenuActivity) {
        return foodMenuActivity.findViewById(R.id.food_menu_histogram);
    }

    private HorizontalBarChart getChart(FoodServiceActivity foodServiceActivity) {
        return foodServiceActivity.findViewById(R.id.food_service_histogram);
    }
}
