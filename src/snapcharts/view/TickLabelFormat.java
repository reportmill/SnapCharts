/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.gfx.Font;
import snap.util.FormatUtils;
import snap.util.MathUtils;
import snapcharts.model.Axis;
import snapcharts.model.Intervals;
import java.text.DecimalFormat;

/**
 * A class to format tick labels.
 */
public class TickLabelFormat {

    // The AxisView
    private AxisView<?>  _axisView;

    // The Axis
    private Axis  _axis;

    // Whether axis is log
    private boolean  _isLog;

    // The intervals to format
    private Intervals  _intervals;

    // The DecimalFormat
    private DecimalFormat  _format;

    // The format pattern
    private String  _formatPattern;

    // The long sample
    private String  _longSample;

    // Shared log/generic format
    private static DecimalFormat LOG_FORMAT = new DecimalFormat("#.###");

    // Format to round values
    private static DecimalFormat ROUND_FORMAT = new DecimalFormat("#.####");

    /**
     * Constructor.
     */
    public TickLabelFormat(AxisView anAxisView)
    {
        _axisView = anAxisView;
        _axis = anAxisView.getAxis();
        _isLog = _axis.isLog();
    }

    /**
     * Returns the intervals to format.
     */
    public Intervals getIntervals()
    {
        // If already set, just return
        if (_intervals != null) return _intervals;

        // Get intervals
        ChartHelper chartHelper = _axisView._chartHelper;
        double axisMin = chartHelper.getAxisMinForIntervalCalc(_axisView);
        double axisMax = chartHelper.getAxisMaxForIntervalCalc(_axisView);
        boolean minFixed = _axisView.isAxisMinFixed();
        boolean maxFixed = _axisView.isAxisMaxFixed();

        // Handle Log
        if (_isLog)
            return _intervals = Intervals.getIntervalsSimple(axisMin, axisMax, minFixed, maxFixed);

        // Handle normal
        Intervals intervals = Intervals.getIntervalsForMinMaxLen(axisMin, axisMax, 200, 10, false, false);

        // If user configured GridSpacing is defined (non-zero), change intervals to be based on GridSpacing/GridBase
        double gridSpacing = _axis.getGridSpacing();
        if (gridSpacing != 0) {
            double gridBase = _axis.getGridBase();
            intervals = Intervals.getIntervalsForSpacingAndBase(intervals, gridSpacing, gridBase, minFixed, maxFixed);
        }

        // Set/return intervals
        return _intervals = intervals;
    }

    /**
     * Sets the intervals.
     */
    public void setIntervals(Intervals theIntervals)
    {
        if (theIntervals == _intervals) return;
        _intervals = theIntervals;
        updateFormatForIntervals();
    }

    /**
     * Updates the format for current intervals.
     */
    private void updateFormatForIntervals()
    {
        // If Log axis, handle special
        if (_isLog) {
            updateFormatForIntervalsLog();
            return;
        }

        // Get intervals and interval delta
        Intervals intervals = getIntervals();
        double delta = intervals.getDelta();

        // Get number of whole digits in interval delta
        int wholeDigitCount = getWholeDigitCount(delta);

        // Handle anything above 10 (since intervals will be factor 10 and ends factor of 1)
        String pattern = "0";

        // Handle weird delta (e.g. user configured GridSpacing)
        if (delta != (int) delta) {
            pattern = "#.#";
            int significantDigits = getSignificantDigitsAfterDecimal(delta, 3);
            for (int i = 1; i < significantDigits; i++)
                pattern += '#';
        }

        // Handle fractional delta
        else if (wholeDigitCount <= 0) {
            int fractDigitCount = getFractionDigitCount(delta);
            String str = "0.0";
            for (int i = 1; i < fractDigitCount; i++) str += '0';
            pattern = str;
        }

        // Set new pattern
        _formatPattern = pattern;
        _format = null;

        // Set new LongSample
        String longSample = getLongSampleCalculated();
        setLongSample(longSample);
    }

    /**
     * Updates the format for current intervals (Log axis).
     */
    private void updateFormatForIntervalsLog()
    {
        // Set new pattern
        _formatPattern = "#.###";
        _format = null;

        // Set new LongSample
        String longSample = getLongSampleCalculated();
        setLongSample(longSample);
    }

    /**
     * Returns the DecimalFormat.
     */
    public DecimalFormat getFormat()
    {
        if (_format != null) return _format;
        String formatPattern = getFormatPattern();
        return _format = FormatUtils.getDecimalFormat(formatPattern);
    }

    /**
     * Returns the format pattern.
     */
    public String getFormatPattern()
    {
        if (_formatPattern != null) return _formatPattern;
        updateFormatForIntervals();
        return _formatPattern;
    }

    /**
     * Returns the long sample.
     */
    public String getLongSample()
    {
        if (_longSample != null) return _longSample;
        updateFormatForIntervals();
        return _longSample;
    }

    /**
     * Sets the longest tick label for current intervals and format.
     */
    private void setLongSample(String aString)
    {
        // If sample is longer than previous estimate, trigger relayout
        if (_longSample != null && _longSample.length() < aString.length()) {
            _axisView.relayout();
            _axisView.relayoutParent();
        }

        // Set value
        _longSample = aString;
    }

    /**
     * Returns the long sample for current intervals and format. Calculated, not cached.
     */
    private String getLongSampleCalculated()
    {
        // Get current intervals
        Intervals intervals = getIntervals();
        int intervalCount = intervals.getCount();

        // Iterate over intervals to find LongSample
        String longSample = "";
        for (int i = 0; i < intervalCount; i++) {

            // If not full interval, just skip
            if (!intervals.isFullInterval(i))
                continue;

            // Get val as string and swap it in if longer
            double val = intervals.getInterval(i);
            String valStr = format(val);
            if (valStr.length() > longSample.length())
                longSample = valStr;
        }

        // Return
        return longSample;
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
        return format(aValue, fmt);
    }

    /**
     * Returns a formatted value.
     */
    private String format(double aValue, DecimalFormat aFormat)
    {
        // Handle Log axis: Only show text for  values that are a factor of 10 (1[0]* or 0.[0]*1)
        if (_isLog)
            return formatLog(aValue);

        // If large value, format with exponent
        if (Math.abs(aValue) >= 1000)
            return formatWithExponent(aValue);

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
            return formatWithExponent(value);

        // Return
        return LOG_FORMAT.format(value);
    }

    /**
     * Does format with exponents.
     */
    private String formatWithExponent(double aValue)
    {
        // Get absolute value
        double absVal = Math.abs(aValue);

        // Handle case of value in the trillions
        if (absVal >= 1000000000000L) {
            String valStr = ROUND_FORMAT.format(aValue / 1000000000000L);
            return valStr + "T";
        }

        // Handle case of value in the billions
        if (absVal >= 1000000000) {
            String valStr = ROUND_FORMAT.format(aValue / 1000000000);
            return valStr + "B";
        }

        // Handle case of value in the millions
        if (absVal >= 1000000) {
            String valStr = ROUND_FORMAT.format(aValue / 1000000);
            return valStr + "M";
        }

        // Handle case of value in the thousands
        String valStr = ROUND_FORMAT.format(aValue / 1000);
        return valStr + "k";
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "TickLabelFormat { Axis=" + _axis.getType() +
                ", Format='" + _formatPattern + '\'' +
                ", LongSample='" + _longSample + '\'' +
                ", Log=" + _isLog +
                '}';
    }

    /**
     * Return the number of digits before decimal point in given value.
     */
    private int getWholeDigitCount(double aValue)
    {
        if (aValue < 1) return 0;
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

    /**
     * Returns the number of significant digits up to a maximum.
     */
    private int getSignificantDigitsAfterDecimal(double aValue, int aMax)
    {
        int count = 0;
        double value = aValue - (int) aValue;
        while (value != (int) value && count < aMax) {
            count++;
            value *= 10;
        }
        return count;
    }
}
