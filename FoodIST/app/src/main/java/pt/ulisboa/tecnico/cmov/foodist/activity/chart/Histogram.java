package pt.ulisboa.tecnico.cmov.foodist.activity.chart;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.foodist.R;

public class Histogram extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram);
        BarChart chart = findViewById(R.id.histogram);

        ArrayList userRatings = new ArrayList();

        userRatings.add(new BarEntry(1, 4));
        userRatings.add(new BarEntry(2, 5));
        userRatings.add(new BarEntry(3, 6));
        userRatings.add(new BarEntry(4, 6));
        userRatings.add(new BarEntry(5, 7));

        BarDataSet bardataset = new BarDataSet(userRatings, "User ratings");
        chart.animateY(500);
        BarData data = new BarData(bardataset);
        bardataset.setColors(ColorTemplate.COLORFUL_COLORS);
        chart.setData(data);
    }
}