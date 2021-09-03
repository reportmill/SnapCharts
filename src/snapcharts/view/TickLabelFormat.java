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

        // Get ideal intervals (i.e., for chart with plenty of room)
        Intervals intervals = Intervals.getIntervalsForMinMaxLen(axisMin, axisMax, 200, 10, minFixed, maxFixed);

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
        // Basic pattern for whole number intervals is just "0"
        String pattern = "0";

        // If fractional digits, add fractional digits to pattern
        int fractionDigits = getFractionDigitsMaxForIntervals();
        if (fractionDigits > 0) {
            pattern = "#.#";
            for (int i = 1; i < fractionDigits; i++)
                pattern += '#';
        }

        // Set new pattern
        _formatPattern = pattern;
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
        // If first time, just set
        if (_longSample == null)
            _longSample = aString;

        // If sample is longer than previous estimate, update and trigger relayout
        else if (_longSample != null && _longSample.length() < aString.length()) {
            _longSample = aString;
            _axisView._tickLabelBox.relayout();
            _axisView._tickLabelBox.relayoutParent();
        }
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
        // Get value (if log, convert to original data scale)
        double value = aValue;
        if (_isLog)
            value = Math.pow(10, value);

        // If large value, format with exponent
        if (Math.abs(value) >= 1000)
            return formatWithExponent(value);

        // Return formatted value
        DecimalFormat format = getFormat();
        try {
            return format.format(value);
        }

        // TeaVM 0.6.0 threw an exception here
        catch (RuntimeException e) {
            System.err.println("Failed to format with: " + format.toPattern() + ", value: " + value);
            return FormatUtils.formatNum(value);
        }
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
     * Returns the maximum number of significant digits after decimal for current intervals.
     */
    private int getFractionDigitsMaxForIntervals()
    {
        // Get intervals
        Intervals intervals = getIntervals();
        int intervalCount = intervals.getCount();
        int digitsMax = 0;

        // Iterate over intervals to find max fraction digits
        for (int i = 0; i < intervalCount; i++) {

            // If partial interval, skip
            if (!intervals.isFullInterval(i))
                continue;

            // Get interval value (if Log axis, convert to data value)
            double value = intervals.getInterval(i);
            if (_isLog)
                value = Math.pow(10, value);

            // Get number of fraction digits and combine with digitsMax
            int digitsCount = getFractionDigits(value, 6);
            digitsMax = Math.max(digitsMax, digitsCount);
        }

        // Return digitsMax
        return digitsMax;
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
     * Returns the number of fractional digits up to a maximum.
     */
    private static int getFractionDigits(double aValue, int aMax)
    {
        // Get simple decimal value (strip sign and integer digits)
        double value = Math.abs(aValue);
        value = value - (int) value;

        // Calculate equals() tolerance (should be below whatever Max fraction digits allows)
        double tolerance = 1 / Math.pow(10, aMax + 1);

        // Iterate through value most significant digit until it doesn't match
        int count = 0;
        while (!MathUtils.equals(value, (int) value, tolerance) && count < aMax) {
            count++;
            value *= 10;
        }

        // Return count
        return count;
    }
}
