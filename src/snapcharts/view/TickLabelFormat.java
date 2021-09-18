/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.text.NumberFormat;
import snap.text.TextFormat;
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

    // The Axis
    private Axis  _axis;

    // Whether axis is log
    private boolean  _isLog;

    // The intervals to format
    protected Intervals  _intervals;

    // The best exponent for current intervals
    private Integer  _intervalsExponent;

    // The width of the longest label
    private int  _maxLabelWidth = -1;

    /**
     * Constructor.
     */
    public TickLabelFormat(AxisView anAxisView, Intervals theIntervals)
    {
        super(null);
        _axis = anAxisView.getAxis();
        _isLog = _axis.isLog();
        _intervals = theIntervals;

        TextFormat textFormat = _axis.getTextFormat();
        NumberFormat numFormat = NumberFormat.getFormatOrDefault(textFormat);
        String pattern = numFormat.isPatternSet() ? numFormat.getPattern() : null;
        setPattern(pattern);
        setExpStyle(numFormat.getExpStyle());
    }

    /**
     * Returns the intervals to format.
     */
    public Intervals getIntervals()  { return _intervals; }

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
        NumberFormat numFormat = NumberFormat.getFormatOrDefault(_axis.getTextFormat());
        String axisFormat = numFormat.isPatternSet() ? numFormat.getPattern() : null;
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
                ", MaxLabelWidth='" + _maxLabelWidth + '\'' +
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
