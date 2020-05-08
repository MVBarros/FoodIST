package pt.ulisboa.tecnico.cmov.foodist.activity.chart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class IntegerFormater extends ValueFormatter {

    private DecimalFormat format = new DecimalFormat("###,##0");

    @Override
    public String getPointLabel(Entry entry) {
        return format.format(entry.getY());
    }

    @Override
    public String getBarLabel(BarEntry barEntry) {
        return format.format(barEntry.getY());
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        return format.format(value);
    }
}

