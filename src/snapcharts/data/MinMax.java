/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.util.Convert;
import snap.util.FormatUtils;
import snap.util.MathUtils;

/**
 * A class to hold a min and max value.
 */
public class MinMax implements Cloneable {

    // The min value
    private double  _min;

    // The max value
    private double  _max;

    // Min/max values with epsilon added (if min/max equal)
    private double  _minE, _maxE;

    /**
     * Constructor.
     */
    public MinMax(double aMin, double aMax)
    {
        _min = _minE = aMin;
        _max = _maxE = aMax;

        // Handle equal/invalid
        if (aMax <= aMin) {

            // Handle Equal case
            if (aMax == aMin) {
                System.err.println("MinMax.new: Equal min/max: I'm going to let it slide this time.");
                double epsilon = Math.abs(aMin) * .001;
                if (epsilon == 0) epsilon = .0000001;
                _minE = aMin - epsilon;
                _maxE = aMax + epsilon;
            }

            // Handle invalid case
            else {
                System.err.println("MinMax.new: Invalid min/max: I'm going to let it slide this time.");
                double tmp = aMin; aMin = aMax; aMax = tmp;
                _min = _minE = aMin;
                _max = _maxE = aMax;
            }
        }
    }

    /**
     * Returns the min value.
     */
    public double getMin()  { return _min; }

    /**
     * Returns the max value.
     */
    public double getMax()  { return _max; }

    /**
     * Returns the min value.
     */
    public double getMinE()  { return _minE; }

    /**
     * Returns the max value.
     */
    public double getMaxE()  { return _maxE; }

    /**
     * Returns the range length.
     */
    public double getRangeLength()
    {
        return _max - _min;
    }

    /**
     * Returns the given fractional value expressed in range 0 to 1 mapped to this MinMax range.
     */
    public double mapFractionalToRangeValue(double aValue)
    {
        return MathUtils.mapFractionalToRangeValue(aValue, _min, _max);
    }

    /**
     * Returns the given fractional value expressed in range 0 to 1 mapped to this MinMax range.
     */
    public double mapRangeValueToFractional(double aValue)
    {
        return MathUtils.mapRangeValueToFractional(aValue, _min, _max);
    }

    /**
     * Returns a string representation of MinMax suitable for archival purposes.
     */
    public String getStringRep()
    {
        String minStr = FormatUtils.formatNum(getMin());
        String maxStr = FormatUtils.formatNum(getMax());
        return "[" + minStr + ' ' + maxStr + "]";
    }

    /**
     * Returns a copy of this min max with new min.
     */
    public MinMax copyForMin(double aMin)
    {
        MinMax clone = clone();
        clone._min = aMin;
        return clone;
    }

    /**
     * Returns a copy of this min max with new max.
     */
    public MinMax copyForMax(double aMax)
    {
        MinMax clone = clone();
        clone._max = aMax;
        return clone;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    protected MinMax clone()
    {
        try { return (MinMax) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "MinMax { " + "Min=" + getMin() + ", Max=" + getMax() + " }";
    }

    /**
     * Returns a MinMax from given object (or null if impossible).
     */
    public static MinMax getMinMax(Object anObj)
    {
        // Handle MinMax
        if (anObj instanceof MinMax)
            return (MinMax) anObj;

        // Handle String
        if (anObj instanceof String) {
            String str = (String) anObj;
            String[] parts = str.split("\\s");
            if (parts.length < 2) {
                System.err.println("MinMax.getMinMax: Invalid string format: " + str);
                return null;
            }
            double min = Convert.doubleValue(parts[0]);
            double max = Convert.doubleValue(parts[1]);
            return new MinMax(min, max);
        }

        // Complain and return null
        System.err.println("MinMax.getMinMax: Can't get MinMax from: " + anObj);
        return null;
    }
}
