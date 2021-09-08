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

    // The Exponent Style
    private Axis.ExpStyle  _expStyle;

    // The best exponent for current intervals
    private Integer  _intervalsExponent;

    // The long sample
    private String  _longSample;

    // Format to round values
    private static String ROUND_FORMAT = "#.######";

    // Format for ExpStyle.Scientific
    private static DecimalFormat EXP_FORMAT = new DecimalFormat("#.##");

    // Constant for upper value at which ExpStyle.Scientific starts formatting in Scientific notation
    private static final double SCI_UPPER_START = 10000;

    // Constant for lower value at which ExpStyle.Scientific starts formatting in Scientific notation
    private static final double SCI_LOWER_START = .001;

    /**
     * Constructor.
     */
    public TickLabelFormat(AxisView anAxisView)
    {
        _axisView = anAxisView;
        _axis = anAxisView.getAxis();
        _isLog = _axis.isLog();
        _expStyle = _axis.getTickLabelExpStyle();
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
        // If already set, just return
        if (theIntervals == _intervals) return;

        // Clear format
        _formatPattern = null;
        _format = null;
        _intervalsExponent = null;

        // Clear FormatPattern, Format and IntervalsExponent
        _formatPattern = null;
        _format = null;
        _intervalsExponent = null;

        // Update LongSample
        updateLongSample();
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
        // If already set, just return
        if (_formatPattern != null) return _formatPattern;

        // Get pattern, set and return
        String pattern = getFormatPatternImpl();
        return _formatPattern = pattern;
    }

    /**
     * Returns the best basic decimal format pattern for current intervals
     */
    private String getFormatPatternImpl()
    {
        // If Axis defines TickLabelFormat, use that
        String axisFormat = _axis.getTickLabelFormat();
        if (axisFormat != null)
            return axisFormat;

        // If financial, use standard
        if (_expStyle == Axis.ExpStyle.Financial || _expStyle == Axis.ExpStyle.Scientific)
            return ROUND_FORMAT;

        // Basic pattern for whole number intervals is just "0"
        String pattern = "0";

        // If fractional digits, add fractional digits to pattern
        int fractionDigits = getFractionDigitsMaxForIntervals();
        if (fractionDigits > 0) {
            pattern = "#.#";
            for (int i = 1; i < fractionDigits; i++)
                pattern += '#';
        }

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
     * Returns a formatted value.
     */
    public String format(double aValue)
    {
        // Get value (if log, convert to original data scale)
        double value = aValue;
        if (_isLog)
            value = Math.pow(10, value);

        // Handle exponent styles
        switch (_expStyle) {
            case Financial: return formatFinancial(value);
            case Scientific: return formatScientific(value);
            default: return formatBasicDecimal(value);
        }
    }

    /**
     * Returns a formatted value.
     */
    private String formatBasicDecimal(double aValue)
    {
        // Return formatted value
        DecimalFormat format = getFormat();
        try {
            return format.format(aValue);
        }

        // TeaVM 0.6.0 threw an exception here
        catch (RuntimeException e) {
            System.err.println("Failed to format with: " + format.toPattern() + ", value: " + aValue);
            return FormatUtils.formatNum(aValue);
        }
    }

    /**
     * Does format with financial exponents (k, M, B, T).
     */
    private String formatFinancial(double aValue)
    {
        // Get absolute value
        double absVal = Math.abs(aValue);
        long divisor = 1;
        String suffix = "";

        // Handle case of value in the trillions
        if (absVal >= 1000000000000L) {
            divisor = 1000000000000L;
            suffix = "T";
        }

        // Handle case of value in the billions
        else if (absVal >= 1000000000) {
            divisor = 1000000000;
            suffix = "B";
        }

        // Handle case of value in the millions
        else if (absVal >= 1000000) {
            divisor = 1000000;
            suffix = "M";
        }

        // Handle case of value in the thousands
        else if (absVal >= 1000) {
            divisor = 1000;
            suffix = "k";
        }

        // Handle case of value in the thousands
        String valStr = formatBasicDecimal(aValue / divisor);
        return valStr + suffix;
    }

    /**
     * Does format with Scientific notation.
     */
    private String formatScientific(double aValue)
    {
        // Zero is special case, just format as basic decimal
        if (aValue == 0)
            return formatBasicDecimal(0);

        // Get exponent for intervals (if log axis, get for value since tick labels aren't linearly spaced)
        int exp = getExponentForIntervals();
        if (_isLog)
            exp = getExponentForValue(aValue);

        // If exponent is zero, just format as basic decimal
        if (exp == 0)
            return formatBasicDecimal(aValue);

        // Format value for exponent
        double baseValue = aValue / Math.pow(10, exp);
        String baseStr = formatBasicDecimal(baseValue);
        String expStr = baseStr + "x10^" + exp;
        return expStr;
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
        int exp = getExponentForValue(maxValue);
        return _intervalsExponent = exp;
    }

    /**
     * Returns the exponent for given value.
     */
    private int getExponentForValue(double aValue)
    {
        // If value in reasonable range, just format as decimal (if no more than 3 zeros of magnitude in value)
        if (aValue < SCI_UPPER_START && aValue >= SCI_LOWER_START)
            return 0;

        // Calculate exponent from magnitude of maxValue (if negative, bump by -1 so 1 <= maxValue <= 10)
        double logValue = Math.log10(aValue);
        int exp = (int) logValue;
        if (logValue < 0 && logValue != (int) logValue)
            exp--;

        // Return exponent
        return exp;
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
