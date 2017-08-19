package com.tistory.puzzleleaf.rankofalcohol.contents.analysis.chart.formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;


public class XAxisValueFormatter implements IAxisValueFormatter
{
    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        String day = (int)value + "일";
        return day;
    }
}
