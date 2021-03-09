package snapcharts.util;

import snap.util.FormatUtils;
import snap.util.SnapUtils;

/**
 * A class to hold a min and max value.
 */
public class MinMax implements Cloneable {

    // The min value
    private double  _min;

    // The max value
    private double  _max;

    /**
     * Constructor.
     */
    public MinMax(double aMin, double aMax)
    {
        if (aMax <= aMin) {
            if (aMax == aMin) {
                System.err.println("MinMax.new: Equal min/max: I'm going to let it slide this time.");
                aMin = aMin - 1; aMax = aMax + 1;
            }
            else {
                System.err.println("MinMax.new: Invalid min/max: I'm going to let it slide this time.");
                double tmp = aMin; aMin = aMax; aMax = tmp;
            }
        }
        _min = aMin;
        _max = aMax;
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
     * Returns the range length.
     */
    public double getRangeLength()
    {
        return _max - _min;
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
            double min = SnapUtils.doubleValue(parts[0]);
            double max = SnapUtils.doubleValue(parts[1]);
            return new MinMax(min, max);
        }

        // Complain and return null
        System.err.println("MinMax.getMinMax: Can't get MinMax from: " + anObj);
        return null;
    }
}
