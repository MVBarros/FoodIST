package pt.ulisboa.tecnico.cmov.foodist.activity.chart.counter;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Iterator;

public interface BarEntriesInterface {

    ArrayList<BarEntry> calculate(Iterator iterator);
}
