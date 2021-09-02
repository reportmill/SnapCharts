/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.gfx.Font;
import snap.util.FormatUtils;
import snap.util.MathUtils;
import snap.view.ViewUpdater;
import snapcharts.model.Intervals;
import java.text.DecimalFormat;

/**
 * A class to format tick labels.
 */
public class TickLabelFormat {

    // The AxisView
    private AxisView<?>  _axisView;

    // The format pattern
    private String  _formatPattern;

    // The long sample
    private String  _longSample;

    // The DecimalFormat
    private DecimalFormat  _format;

    // Runnable to trigger check if default tick format has changed
    private Runnable  _checkForRelayout, CHECK_FOR_RELAYOUT = () -> checkForTickFormatChangeImpl();

    // Shared log/generic format
    private static DecimalFormat TICKS_FORMAT = new DecimalFormat("#.###");

    /**
     * Constructor.
     */
    public TickLabelFormat(AxisView anAxisView)
    {
        _axisView = anAxisView;
    }

    /**
     * Returns the format pattern.
     */
    public String getFormatPattern()
    {
        if (_formatPattern!=null) return _formatPattern;
        setPatternAndSample();
        return _formatPattern;
    }

    /**
     * Returns the long sample.
     */
    public String getLongSample()
    {
        if (_longSample != null) return _longSample;
        setPatternAndSample();
        return _longSample;
    }

    /**
     * Returns the DecimalFormat.
     */
    public DecimalFormat getFormat()
    {
        if (_format == null)
            _format = FormatUtils.getDecimalFormat(getFormatPattern());
        return _format;
    }

    /**
     * Returns the max label width.
     */
    public double getLongSampleStringWidth()
    {
        String tickStr = getLongSample();
        Font font = _axisView.getFont();
        int tickW = (int) Math.ceil(font.getStringAdvance(tickStr));
        return tickW;
    }

    /**
     * Returns a formatted value.
     */
    public String format(double aValue)
    {
        DecimalFormat fmt = getFormat();
        double delta = _axisView.getIntervals().getDelta();
        return format(aValue, fmt, delta);
    }

    /**
     * Returns a formatted value.
     */
    public String format(double aValue, DecimalFormat aFormat, double aDelta)
    {
        // Handle Log axis: Only show text for  values that are a factor of 10 (1[0]* or 0.[0]*1)
        boolean isLog = _axisView.getAxis().isLog();
        if (isLog)
            return formatLog(aValue);

        // If large delta, format with exponent
        if (aDelta >= 1000)
            return getFormatWithExponent(aValue, aDelta);

        // Return formatted value
        try {
            return aFormat.format(aValue);
        }

        // TeaVM 0.6.0 threw an exception here
        catch (RuntimeException e) {
            System.err.println("Failed to format with: " + aFormat.toPattern() + ", value: " + aValue);
            return FormatUtils.formatNum(aValue);
        }
    }

    /**
     * Formats a log value.
     */
    private String formatLog(double aValue)
    {
        // If not whole number, return empty string
        if (!MathUtils.equals(aValue, (int) aValue))
            return "";

        // Get value
        double value = Math.pow(10, aValue);
        if (value >= 1000)
            return getFormatWithExponent(value, value);

        // Return
        return TICKS_FORMAT.format(value);
    }

    /**
     * Does format with exponents.
     */
    private String getFormatWithExponent(double aValue, double aDelta)
    {
        // Handle case where delta is in the trillions
        if (aDelta >= 1000000000000L) {
            int val = (int) Math.round(aValue / 1000000000000L);
            return val + "T";
        }

        // Handle case where delta is in the billions
        if (aDelta >= 1000000000) {
            int val = (int) Math.round(aValue / 1000000000);
            return val + "B";
        }

        // Handle case where delta is in the millions
        if (aDelta >= 1000000) {
            int val = (int) Math.round(aValue / 1000000);
            return val + "M";
        }

        // Handle case where delta is in the thousands
        int val = (int) Math.round(aValue / 1000);
        return val + "k";
    }

    /**
     * Returns the suggested format pattern.
     */
    private void setPatternAndSample()
    {
        String[] patternAndSample = getAutoFormatPatternForTickLabelsAndLongSample();
        _formatPattern = patternAndSample[0];
        _longSample = patternAndSample[1];
    }

    /**
     * Returns the suggested format pattern and a sample string that should represent the longest possible label string.
     */
    private String[] getAutoFormatPatternForTickLabelsAndLongSample()
    {
        // If Log axis, handle special
        boolean isLog = _axisView.getAxis().isLog();
        if (isLog)
            return getAutoFormatPatternForTickLabelsAndLongSampleLog();

        // Get max value
        ChartHelper chartHelper = _axisView._chartHelper;
        double axisMin = chartHelper.getAxisMinForIntervalCalc(_axisView);
        double axisMax = chartHelper.getAxisMaxForIntervalCalc(_axisView);

        // Get ideal intervals, interval delta and its number of whole digits
        Intervals ivals = Intervals.getIntervalsForMinMaxLen(axisMin, axisMax, 200, 10, false, false);
        double delta = ivals.getDelta();
        int wholeDigitCount = getWholeDigitCount(delta);

        // Handle anything above 10 (since intervals will be factor 10 and ends factor of 1)
        String pattern = "#";

        // Handle fractions
        if (wholeDigitCount <= 0) {
            int fractDigitCount = getFractionDigitCount(delta);
            String str = "#.#";
            for (int i = 1; i < fractDigitCount; i++) str += '#';
            pattern = str;
        }

        // We really want zero string
        pattern = pattern.replace('#', '0');

        // Get format, format min/max inset by delta/3 (to get repeating .33) and get longer string
        DecimalFormat format = FormatUtils.getDecimalFormat(pattern);
        String minSample = format(ivals.getMin(), format, delta);
        String maxSample = format(ivals.getMax(), format, delta);
        String longSample = minSample.length() > maxSample.length() ? minSample : maxSample;

        // Return pattern and long sample in array
        return new String[] { pattern, longSample };
    }

    /**
     * Returns the suggested format pattern and a sample string that should represent the longest possible label string.
     */
    private String[] getAutoFormatPatternForTickLabelsAndLongSampleLog()
    {
        // Get ideal intervals
        ChartHelper chartHelper = _axisView._chartHelper;
        double axisMin = chartHelper.getAxisMinForIntervalCalc(_axisView);
        double axisMax = chartHelper.getAxisMaxForIntervalCalc(_axisView);
        boolean minFixed = _axisView.isAxisMinFixed();
        boolean maxFixed = _axisView.isAxisMaxFixed();
        Intervals ivals = Intervals.getIntervalsSimple(axisMin, axisMax, minFixed, maxFixed);
        String longSample = "";

        // Iterate over intervals
        for (int i=0; i<ivals.getCount(); i++) {

            // If loop value not int, skip (log axis only shows int (power of 10) values)
            double val = ivals.getInterval(i);
            if (val != (int) val)
                continue;

            // Get val as string and swap it in if longer
            String valStr = formatLog(val);
            if (valStr.length() > longSample.length())
                longSample = valStr;
        }

        return new String[] { "#.###", longSample };
    }

    /**
     * Called when intervals change to see if default tick label format has changed (which triggers full chart relayout).
     */
    public void checkForFormatChange()
    {
        if (_checkForRelayout == null && _formatPattern != null) {
            ViewUpdater updater = _axisView.getUpdater();
            if (updater != null)
                updater.runBeforeUpdate(_checkForRelayout = CHECK_FOR_RELAYOUT);
        }
    }

    /**
     * Called when intervals change to see if default tick label format has changed (which triggers full chart relayout).
     */
    private void checkForTickFormatChangeImpl()
    {
        // Get new longest tick label sample string and its length
        String[] patternAndSample = getAutoFormatPatternForTickLabelsAndLongSample();
        String longSample = patternAndSample[1];
        int longSampleLen = longSample.length();

        // If length has changed, relayout chart
        if (longSampleLen != _longSample.length()) {
            _axisView.relayout();
            _axisView.relayoutParent();
            _formatPattern = patternAndSample[0];
            _longSample = longSample;
            _format = null;
        }

        // Reset runnable trigger
        _checkForRelayout = null;
    }

    /**
     * Return the number of digits before decimal point in given value.
     */
    private int getWholeDigitCount(double aValue)
    {
        if (aValue<1)
            return 0;
        double log10 = Math.log10(aValue);
        return (int) Math.floor(log10) + 1;
    }

    /**
     * Return the number of digits after decimal point in given value.
     */
    private int getFractionDigitCount(double aValue)
    {
        if (aValue >= 1) return 0;
        double log10 = Math.log10(aValue);
        return (int) Math.round(Math.abs(log10));
    }
}
