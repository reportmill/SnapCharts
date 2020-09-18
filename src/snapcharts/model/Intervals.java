/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

import java.util.Arrays;

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
    
    // The axis length for the intervals
    private double  _axisLen;
    
    // The change between intervals
    private double  _delta;
    
    // The number of interval values
    private int  _count;

    // The list of interval numbers
    private double  _intervals[];

    /**
     * Constructor.
     */
    private Intervals()
    {
        super();
    }

    /**
     * Return well-chosen intervals given a min value, a max value and an axis length. For instance, (1,4) would return
     *    (1,2,3,4,5), while (17,242) would return (50,100,150,200,250). Useful for graphing.
     */
    public static Intervals getIntervalsForMinMaxLen(double minValue, double maxValue, double anAxisLen)
    {
        // Set seed value ivars
        Intervals ivals = new Intervals();
        ivals._minVal = minValue;
        ivals._maxVal = maxValue;
        ivals._axisLen = anAxisLen;

        // Calculate intervals and cache Delta, Count
        double vals[] = getIntervalsFor(minValue, maxValue, anAxisLen, 40);
        ivals._intervals = vals;
        ivals._delta = vals[1] - vals[0];
        ivals._count = vals.length;
        return ivals;
    }

    /**
     * Return well-chosen intervals given a min value, a max value and an axis length. For instance, (1,4) would return
     *    (1,2,3,4,5), while (17,242) would return (50,100,150,200,250). Useful for graphing.
     */
    public static Intervals getSimpleIntervals(int minValue, int maxValue)
    {
        Intervals ivals = new Intervals();
        ivals._minVal = minValue;
        ivals._maxVal = maxValue;
        int len = maxValue - minValue + 1;
        ivals._axisLen = len;
        ivals._intervals = new double[len];
        for (int i=0; i<len; i++)
            ivals._intervals[i] = minValue + i;
        ivals._delta = 1;
        ivals._count = len;
        return ivals;
    }

    /**
     * Returns the seed min val.
     */
    public double getSeedValueMin()  { return _minVal; }

    /**
     * Returns the seed max val.
     */
    public double getSeedValueMax()  { return _maxVal; }

    /**
     * Returns the given axis length.
     */
    public double getAxisLength()  { return _axisLen; }

    /**
     * Returns the number of intervals for this filled graph.
     */
    public int getCount()  { return _count; }

    /**
     * Returns the individual interval at a given index as a float value.
     */
    public Double getInterval(int anIndex)  { return _intervals[anIndex]; }

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
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "Intervals { MinVal=" + _minVal + ", MaxVal=" + _maxVal + ", AxisLen=" + _axisLen +
            " Count=" + _count + ", Delta=" + _delta +
            ", Intervals=" + Arrays.toString(_intervals) + '}';
    }

    /**
     * Returns well-chosen intervals from given min value to max value for given axis length and minimum step length.
     */
    private static double[] getIntervalsFor(double aMinValue, double aMaxValue, double anAxisLen, double aStepLen)
    {
        // Find factor of 10 that is just below maxValue (10 ^ factor+1 is above)
        if (aMinValue==aMaxValue) aMaxValue++;
        double bigValue = Math.max(Math.abs(aMinValue), Math.abs(aMaxValue));
        int pow = -10; double factor = Math.pow(10, pow);
        while (true) {
            if (factor<=bigValue && factor*10>=bigValue)
                break;
            pow++;
            factor = Math.pow(10,pow);
        }

        // Declare array of pleasing increments (percents/100)
        double increments[] = { .2, .25, .40, .50, 1, 2, 2.5, 4, 5, 10 };
        double incr = factor, axisMin = aMinValue;
        int steps = 1;

        // Iterate over pleasing increments to find one that results in reasonable length for increment
        for (int i=0; i<increments.length; i++) { incr = increments[i]*factor;

            // Calculate number of steps to get from zero to maxValue with current increment
            steps = getStepsToValueWithIncrement(aMaxValue, incr);

            // If MinValue non zero, adjust Steps and AxisMin
            if (aMinValue!=0) {

                // Get steps to MinValue
                int steps2 = getStepsToValueWithIncrement(aMinValue, incr);

                // If both min/max greater than zero, subtract steps and reset AxisMin
                if (aMinValue>0 && aMaxValue>0) {
                    steps2--; axisMin = steps2*incr; steps -= steps2; }

                // If both min/max less than zero
                else if (aMinValue<0 && aMaxValue<0) {
                    axisMin = -steps2*incr; steps = steps2 - (steps - 1); }

                // If Min/Max are on opposite sides of zero
                else { axisMin = -steps2*incr; steps += steps2; }
            }

            // Otherwise, if MaxValue less than zero, reset AxisMin
            else if (aMaxValue<0)
                axisMin = -steps*incr;

            // Calculate AxisMax
            double axisMax = axisMin + steps*incr;

            // If min step length is zero, reset steps
            if (aStepLen<=0) {
                incr = steps*incr;
                steps = 1;
            }

            // If more than 10 continue
            if (steps>10) continue;

            // If length per step out of bounds, continue
            double dh = anAxisLen/steps; if (dh<aStepLen) continue;

            // If maxValue within 15 points of length, continue
            double maxValueLen = (aMaxValue-axisMin)/(axisMax-axisMin)*anAxisLen;
            if (maxValueLen+15>anAxisLen) continue;

            // Break since increment, steps and padding are sufficient
            break;
        }

        // If only one step, reset delta
        if (steps==1 && aStepLen>0)
            return getIntervalsFor(aMinValue, aMaxValue, anAxisLen, 0);

        // Create intervals list and return
        double ivals[] = new double[steps+1];
        for (int i=0;i<=steps;i++)
            ivals[i] = axisMin + incr*i;
        return ivals;
    }

    /**
     * Returns the number of steps it takes to get to a value (or beyond) with given increment.
     */
    private static int getStepsToValueWithIncrement(double aValue, double anIncr)
    {
        //int steps = 1; double axisMax = incr; while (axisMax<maxValue && steps<11) { axisMax += incr; steps++; }
        double val = Math.abs(aValue);
        int steps = (int)Math.ceil(val/anIncr);
        return steps;
    }
}