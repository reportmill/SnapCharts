/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;
import snap.util.ArrayUtils;
import snap.util.MathUtils;
import snapcharts.util.DataUtils;

/**
 * This class represent a set of equally spaced interval values for a given min and max value and an axis length.
 * The intervals cover the range and should grow in increments of 10%, 20%, 25%, 40%, 50% or 100% and guarantee
 * that the length of the intervals is at least 45 pnts but no more than 75 pnts.
 */
public class Intervals {
    
    // The given min for intervals
    private double  _minVal;
    
    // The given max for intervals
    private double  _maxVal;
    
    // The change between intervals
    private double  _delta;
    
    // The number of interval values
    private int  _count;

    // The array of interval division values
    private double[]  _divs;

    // An array of pleasing increments
    private static final double[] PLEASING_INCREMENTS = { .1, .2, .5, 1, 2, 5, 10 };

    /**
     * Constructor.
     */
    private Intervals()
    {
        super();
    }

   /**
     * Returns the seed min val.
     */
    public double getSeedMin()  { return _minVal; }

    /**
     * Returns the seed max val.
     */
    public double getSeedMax()  { return _maxVal; }

    /**
     * Returns the number of intervals for this filled graph.
     */
    public int getCount()  { return _count; }

    /**
     * Returns the individual interval at a given index as a float value.
     */
    public Double getInterval(int anIndex)  { return _divs[anIndex]; }

    /**
     * Returns the last interval as a double value.
     */
    public double getMin()  { return getInterval(0); }

    /**
     * Returns the last interval as a double value.
     */
    public double getMax()  { return getInterval(_count-1); }

    /**
     * Returns the interval change as a double value.
     */
    public double getDelta()  { return _delta; }

    /**
     * Returns whether given interval is full (has same delta as others).
     */
    public boolean isFullInterval(int anIndex)
    {
        // Inner intervals
        int count = getCount();
        if (anIndex > 0 && anIndex < count - 1)
            return true;

        int index2 = anIndex == 0 ? anIndex + 1 : anIndex - 1;
        double val1 = getInterval(anIndex);
        double val2 = getInterval(index2);
        double delta = Math.abs(val2 - val1);
        return withinPercentTolerance(delta, _delta, .0001);
    }

    /**
     * Returns whether given values are within given percent (as ratio) of each other.
     */
    private boolean withinPercentTolerance(double aVal1, double aVal2, double aTolerance)
    {
        // Get ratio of difference to max val
        double maxVal = Math.max(Math.abs(aVal1), Math.abs(aVal2));
        double diffVal = Math.abs(aVal2 - aVal1);
        double diffRatio = maxVal != 0 ? diffVal / maxVal : 0;

        // If difference greater than .1%, return false, otherwise true
        boolean withinTolerance = diffRatio < aTolerance;
        return withinTolerance;
    }

    /**
     * Returns the divs.
     */
    public double[] getDivs()  { return _divs; }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String divStr = DataUtils.getStringForDoubleArray(_divs);
        return "Intervals { MinVal=" + _minVal + ", MaxVal=" + _maxVal +
                ", Count=" + _count + ", Delta=" + _delta + ", Divs=" + divStr + " }";
    }

    /**
     * Return simple intervals from given min/max int value.
     */
    public static Intervals getIntervalsSimple(int aMin, int aMax)
    {
        Intervals ivals = new Intervals();
        ivals._minVal = aMin;
        ivals._maxVal = aMax;
        int len = aMax - aMin + 1;
        ivals._divs = new double[len];
        for (int i=0; i<len; i++) ivals._divs[i] = aMin + i;
        ivals._delta = 1;
        ivals._count = len;
        return ivals;
    }

    /**
     * Just returns intervals of whole numbers from min (floor) to max (ceil), incremented by 1.
     */
    public static Intervals getIntervalsSimple(double aMin, double aMax, boolean minFixed, boolean maxFixed)
    {
        // Get min/max as ints
        double minRound = MathUtils.floor(aMin, .1);
        double maxRound = MathUtils.ceil(aMax, .1);
        int min = (int) Math.floor(minRound);
        int max = (int) Math.ceil(maxRound);

        // Create/fill divs from min to max
        int len = max - min + 1;
        double[] divs = new double[len];
        for (int i=0; i<len; i++)
            divs[i] = min + i;

        // Update ends
        divs[0] = minRound;
        if (minFixed)
            divs[0] = aMin;
        divs[len - 1] = maxRound;
        if (maxFixed)
            divs[len - 1] = aMax;

        Intervals ivals = new Intervals();
        ivals._minVal = 0;
        ivals._maxVal = aMax;
        ivals._divs = divs;
        ivals._delta = 1;
        ivals._count = divs.length;
        return ivals;
    }

    /**
     * Return the factor of 10 below given value.
     */
    private static double minFactor(double aValue)
    {
        // Start at max factor and shrink by factor of 10
        double factor = 10000;
        while (true) {
            if (factor < aValue || factor<.1)
                return factor;
            factor /= 10;
        }
    }

    /**
     * Return the factor of 10 above given value.
     */
    private static double maxFactor(double aValue)
    {
        // Start at min factor and group by factor of 10
        double factor = 1;
        while (true) {
            if (factor >= aValue)
                return factor;
            factor *= 10;
        }
    }

    /**
     * Returns pleasing intervals given a min value, a max value and an axis length.
     * For instance, (1,4) returns (1,2,3,4,5), while (17,242) would return (50,100,150,200,250). Useful for graphing.
     */
    public static Intervals getIntervalsForMinMaxLen(double aMin, double aMax, double axisLen, double divLen, boolean minFixed, boolean maxFixed)
    {
        // Set seed value ivars
        Intervals ivals = new Intervals();
        ivals._minVal = aMin;
        ivals._maxVal = aMax;

        // Calculate intervals and cache Count
        double[] divs = getDivsFor(aMin, aMax, axisLen, divLen, minFixed, maxFixed);
        int count = divs.length;
        ivals._divs = divs;
        ivals._count = count;

        // Bogus - get rid of this when you figure it out
        if (count < 2) {
            getDivsFor(aMin, aMax, axisLen, divLen, minFixed, maxFixed);
            System.err.println("Intervals: getIntevalsForMinMaxLen: How can this happen?");
            ivals._divs = divs = new double[] { aMin, aMax };
            ivals._count = count = divs.length;
        }

        // Cache delta: If plenty of divs, use difference of second set, otherwise use max of first/last sets
        ivals._delta = count>=4 ? divs[2] - divs[1] : Math.max(divs[1] - divs[0], divs[count-1] - divs[count-2]);

        // Return
        return ivals;
    }

    /**
     * Returns intervals for given GridSpacing and GridBase.
     */
    public static Intervals getIntervalsForSpacingAndBase(Intervals theIntervals, double aSpacing, double aBase, boolean minFixed, boolean maxFixed)
    {
        // Get Spacing (if less than zero, treat it as interval count (negated))
        double spacing = aSpacing;
        double min = theIntervals.getMin();
        double max = theIntervals.getMax();
        double rangeLength = max - min;
        if (spacing < 0) {
            spacing = rangeLength / -spacing;
            aBase = Axis.GRID_BASE_DATA_MIN;
        }

        // If more than 100 divs, cap it at 100
        double divCount = rangeLength / spacing;
        if (divCount > 100) {
            System.err.println("Intervals.getIntervalsForSpacingAndBase: Too many intervals (" + divCount + ")");
            spacing = rangeLength / 100;
        }

        // Create divs
        double[] divs = new double[0];

        // If GridBase is DATA_MIN, add divs
        if (aBase <= Axis.GRID_BASE_DATA_MIN) {
            double div = min;
            while (div <= max) {
                divs = ArrayUtils.add(divs, div);
                div += spacing;
            }
            if (MathUtils.equals(div, max, .001))
                divs = ArrayUtils.add(divs, div);
        }

        // If GridBase is DATA_MAX, add divs
        else if (aBase >= Axis.GRID_BASE_DATA_MAX) {
            double div = max;
            while (div >= min) {
                divs = ArrayUtils.add(divs, div, 0);
                div -= spacing;
            }
            if (MathUtils.equals(div, min, .001))
                divs = ArrayUtils.add(divs, div, 0);
        }

        // Otherwise, get simple divs from min to max by spacing
        else {

            // Get simple divs
            double min2 = min - aBase;
            double max2 = max - aBase;
            divs = getDivsForMinMaxIncr(min2, max2, spacing, 0, minFixed, maxFixed);

            // If Base is set, reset divs by Base amount
            if (aBase != 0) {
                for (int i = 0; i < divs.length; i++)
                    divs[i] += aBase;
            }
        }

        // Update/return intervals
        theIntervals._divs = divs;
        theIntervals._count = divs.length;
        theIntervals._delta = spacing;
        return theIntervals;
    }

    /**
     * Returns pleasing intervals for given min/max, axis length and min step size (in points).
     * Returned intervals are separated by constant increment and thus will usually be around (not on) Min/Max vals.
     */
    private static double[] getDivsFor(double aMin, double aMax, double axisLen, double divLen, boolean minFixed, boolean maxFixed)
    {
        // Fix some edge cases
        if (aMin>0 && aMin<1 && MathUtils.equals(aMin/(aMax - aMin), 0))
            aMin = 0;
        if (Double.isInfinite(aMin))
            aMin = -Double.MIN_VALUE;
        if (Double.isInfinite(aMax))
            aMax = Double.MAX_VALUE;

        // Find largest factor of 10 that is below range (must be a better way to do this)
        if (aMin==aMax) aMax++;
        double rangeLen = aMax - aMin;
        int pow = (int) Math.round(Math.log10(rangeLen));
        double factor = Math.pow(10, pow);
        while (factor*1.1>rangeLen) factor /= 10;

        // Make sure axisLen is reasonable
        if (axisLen<divLen*2)
            axisLen = divLen*1.5;

        // Iterate over increments to find one that results in reasonable length for increment
        for (double val : PLEASING_INCREMENTS)
        {
            // Get candidate increment from nice list times factor
            double incr = val*factor;

            // Get step just before MinVal and step after MaxVal
            double stepMin = minFixed ? getStepsToValueWithIncrement(aMin, incr) :
                    getStepBeforeValueForBaseAndIncrement(aMin, 0, incr);
            double stepMax = maxFixed ? getStepsToValueWithIncrement(aMax, incr) :
                    getStepAfterValueForBaseAndIncrement(aMax, 0, incr);
            double steps = stepMax - stepMin;

            // If step size greater than or equal MinSize, return simple intervals for increment
            double stepSize = axisLen/steps;
            if (steps<15 && MathUtils.gte(stepSize, divLen))
            {
                // Get simple intervals for increment
                double[] ivals = getDivsForMinMaxIncr(aMin, aMax, incr, 0, minFixed, maxFixed);

                // If only 3 intervals and StepSize is less than half original MinSize, use ends instead
                if (ivals.length==3 && stepSize<divLen/2)
                {
                    ivals = new double[] { ivals[0], ivals[ivals.length - 1] };
                }

                // Return intervals
                return ivals;
            }
        }

        // Try again with reduced min size - should be rare
        int minSize2 = (int) Math.floor(divLen * .9);
        return getDivsFor(aMin, aMax, axisLen, minSize2, minFixed, maxFixed);
    }

    /**
     * Returns the number of steps it takes to get to a value (or beyond) with given increment.
     */
    private static double getStepsToValueWithIncrement(double aValue, double anIncr)
    {
        double steps = aValue/anIncr;
        if (MathUtils.equals(steps, (int) steps))
            steps = (int) steps;
        return steps;
    }

    /**
     * Returns number of steps to get before given value (or to, if on step) from given base with given increment.
     */
    private static int getStepBeforeValueForBaseAndIncrement(double aValue, double aBase, double anIncr)
    {
        double range = aValue - aBase;
        int steps = (int) Math.floor(range/anIncr + .0000001);
        return steps;
    }

    /**
     * Returns number of steps to get beyond given value (or to, if on step) from given base with given increment.
     */
    private static int getStepAfterValueForBaseAndIncrement(double aValue, double aBase, double anIncr)
    {
        double range = aValue - aBase;
        int steps = (int) Math.ceil(range/anIncr - .0000001);
        return steps;
    }

    /**
     * Returns simple intervals double array from given min, max and increment.
     */
    private static double[] getDivsForMinMaxIncr(double aMin, double aMax, double anIncr, double aBase, boolean minFixed, boolean maxFixed)
    {
        // Get steps to ends and step count
        int stepMin = getStepBeforeValueForBaseAndIncrement(aMin, aBase, anIncr);
        int stepMax = getStepAfterValueForBaseAndIncrement(aMax, aBase, anIncr);
        int stepCount = stepMax - stepMin + 1;

        // Create array, fill with intervals
        double[] divs = new double[stepCount];
        for (int i = 0,step = stepMin; i < stepCount; i++, step++)
            divs[i] = step * anIncr;

        // Adjust edges if Min/Max Fixed
        if (minFixed)
            divs[0] = aMin;
        if (maxFixed)
            divs[divs.length - 1] = aMax;

        // Return divs
        return divs;
    }
}