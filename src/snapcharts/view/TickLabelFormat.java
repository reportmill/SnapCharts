/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.gfx.Font;
import snap.text.NumberFormat;
import snap.util.FormatUtils;
import snap.util.MathUtils;
import snapcharts.model.Axis;
import snapcharts.model.Intervals;

/**
 * This NumberFormat subclass assists tick label formatting.
 *
 * Main features are to determine maximum label size and to provide consistent exponent for Scientific format.
 */
public class TickLabelFormat extends NumberFormat {

    // The AxisView
    private AxisView<?>  _axisView;

    // The Axis
    private Axis  _axis;

    // Whether axis is log
    private boolean  _isLog;

    // The intervals to format
    private Intervals  _intervals;

    // The best exponent for current intervals
    private Integer  _intervalsExponent;

    // The long sample
    private String  _longSample;

    /**
     * Constructor.
     */
    public TickLabelFormat(AxisView anAxisView)
    {
        super(null);
        _axisView = anAxisView;
        _axis = anAxisView.getAxis();
        _isLog = _axis.isLog();

        setPattern(_axis.getTickLabelFormat());
        setExpStyle(_axis.getTickLabelExpStyle());
    }

    /**
     * Returns the intervals to format.
     */
    public Intervals getIntervals()
    {
        // If already set, just return
        if (_intervals != null) return _intervals;

        // Get, set and return intervals
        Intervals intervals = getIntervalsImpl();
        return _intervals = intervals;
    }

    /**
     * Returns the intervals to format.
     */
    private Intervals getIntervalsImpl()
    {
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

        // Return intervals
        return intervals;
    }

    /**
     * Sets the intervals.
     */
    public void setIntervals(Intervals theIntervals)
    {
        // If already set, just return
        if (theIntervals == _intervals) return;

        // Clear Pattern, Format and IntervalsExponent
        _pattern = null;
        _format = null;
        _intervalsExponent = null;

        // Update LongSample
        updateLongSample();
    }

    /**
     * Returns the format pattern.
     */
    public String getPattern()
    {
        // If already set, just return
        if (_pattern != null) return _pattern;

        // Get pattern, set and return
        String pattern = getPatternImpl();
        return _pattern = pattern;
    }

    /**
     * Returns the best basic decimal format pattern for current intervals
     */
    private String getPatternImpl()
    {
        // If Axis defines TickLabelFormat, use that
        String axisFormat = _axis.getTickLabelFormat();
        if (axisFormat != null)
            return axisFormat;

        // If financial, use standard
        ExpStyle expStyle = getExpStyle();
        if (expStyle == ExpStyle.Financial || expStyle == ExpStyle.Scientific)
            return NULL_PATTERN;

        // Get pattern for number of fraction digits
        int fractionDigits = getFractionDigitsMaxForIntervals();
        String pattern = fractionDigits > 0 ? FormatUtils.getPatternForFractionDigits(fractionDigits) : "0";

        // Return pattern
        return pattern;
    }

    /**
     * Returns the long sample.
     */
    public String getLongSample()
    {
        // If already set, just return
        if (_longSample != null) return _longSample;

        // Get, set and return longSample for current intervals
        String longSample = getLongSampleCalculated();
        return _longSample = longSample;
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
     * Updates the longest tick label for current intervals and format.
     */
    private void updateLongSample()
    {
        // If not yet set, just return
        if (_longSample == null) return;

        // If longSample for current intervals is longer, set and trigger axis relayout
        String longSample = getLongSampleCalculated();
        if (_longSample.length() < longSample.length()) {
            _longSample = longSample;
            _axisView._tickLabelBox.relayout();
            _axisView._tickLabelBox.relayoutParent();
        }
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
     * Override to support Axis isLog.
     */
    public String format(double aValue)
    {
        // Get value (if log, convert to original data scale)
        double value = aValue;
        if (_isLog)
            value = Math.pow(10, value);

        // Do normal version
        return super.format(value);
    }

    /**
     * Override to return standard exponent for all tick labels.
     */
    protected int getExponentForValue(double aValue)
    {
        // If Axis.isLog, do normal version
        if (_isLog)
            return super.getExponentForValue(aValue);

        // Otherwise, return consistent exponent for current intervals
        return getExponentForIntervals();
    }

    /**
     * Returns the exponent for intervals.
     */
    private int getExponentForIntervals()
    {
        // If already set, just return
        if (_intervalsExponent != null) return _intervalsExponent;

        // Get max value of interval range (only use full intervals)
        Intervals intervals = getIntervals();
        int intervalCount = intervals.getCount();
        double intervalMin = intervalCount > 3 && !intervals.isFullInterval(0) ? intervals.getInterval(1) : intervals.getMin();
        double intervalMax = intervalCount > 3 && !intervals.isFullInterval(intervalCount - 1) ? intervals.getInterval(intervalCount - 2) : intervals.getMax();
        double maxValue = Math.max(Math.abs(intervalMin), intervalMax);

        // Get, set and return exponent
        int exp = super.getExponentForValue(maxValue);
        return _intervalsExponent = exp;
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
            int digitsCount = getFractionDigits(value, 5);
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
                ", Format='" + getPattern() + '\'' +
                ", ExpStyle='" + getExpStyle() + '\'' +
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
