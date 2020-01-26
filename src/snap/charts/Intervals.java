/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.charts;
import java.util.*;

/**
 * This class represent a set of equally spaced interval values for a given min and max value and a height.
 * The intervals cover the range and should grow in increments of 10%, 20%, 25%, 40%, 50% or 100% and guarantee
 * that the height of the intervals is at least 45 pnts but no more than 75 pnts.
 */
public class Intervals {
    
    // The seed min value provided
    double            _minVal;
    
    // The seed max value provided
    double            _maxVal;
    
    // The seed height
    double            _height;
    
    // The change between intervals
    double            _delta;
    
    // The number of interval values
    int               _count;

    // The list of interval numbers
    List <Double>     _intervals;

/**
 * Return well-chosen intervals given a min value, a max value and a height. For instance, (1,4) would return
 *    (1,2,3,4,5), while (17,242) would return (50,100,150,200,250). Useful for graphing.
 */
public Intervals(double minValue, double maxValue, double aHeight)
{
    // Set seed value ivars
    _minVal = minValue; _maxVal = maxValue; _height = aHeight;
    
    // Calculate intervals and cache Delta, Count
    _intervals = getIntervalsFor(minValue, maxValue, aHeight, 40);
    _delta = _intervals.get(1) - _intervals.get(0);
    _count = _intervals.size();
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
 * Returns the seed height val.
 */
public double getSeedHeight()  { return _height; }

/**
 * Returns the number of intervals for this filled graph.
 */
public int getCount()  { return _count; }

/**
 * Returns the individual interval at a given index as a float value.
 */
public Double getInterval(int anIndex)  { return _intervals.get(anIndex); }

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
 * Returns well-chosen intervals from given min value to max value for given graph height and minimum step height.
 */
private static List <Double> getIntervalsFor(double aMinValue, double aMaxValue, double aHeight, double aMinHeight)
{
    // Find factor of 10 that is just below maxValue (10 ^ factor+1 is above)
    if(aMinValue==aMaxValue) aMaxValue++;
    double bigValue = Math.max(Math.abs(aMinValue), Math.abs(aMaxValue));
    int pow = -10; double factor = Math.pow(10, pow);
    while(true) { if(factor<=bigValue && factor*10>=bigValue) break; pow++; factor = Math.pow(10,pow); }
    
    // Declare array of pleasing increments (percents/100)
    double increments[] = { .2, .25, .40, .50, 1, 2, 2.5, 4, 5, 10 };
    double incr = factor, axisMin = aMinValue;
    int steps = 1;
    
    // Iterate over pleasing increments to find one that results in reasonable height for increment
    for(int i=0; i<increments.length; i++) { incr = increments[i]*factor;
        
        // Calculate number of steps to get from zero to maxValue with current increment
        steps = getStepsToValueWithIncrement(aMaxValue, incr);
        
        // If MinValue non zero, adjust Steps and AxisMin
        if(aMinValue!=0) {
            
            // Get steps to MinValue
            int steps2 = getStepsToValueWithIncrement(aMinValue, incr);
            
            // If both min/max greater than zero, subtract steps and reset AxisMin
            if(aMinValue>0 && aMaxValue>0) {
                steps2--; axisMin = steps2*incr; steps -= steps2; }
            
            // If both min/max less than zero
            else if(aMinValue<0 && aMaxValue<0) {
                axisMin = -steps2*incr; steps = steps2 - (steps - 1); }
            
            // If Min/Max are on opposite sides of zero
            else { axisMin = -steps2*incr; steps += steps2; }
        }
        
        // Otherwise, if MaxValue less than zero, reset AxisMin
        else if(aMaxValue<0)
            axisMin = -steps*incr;
            
        // Calculate AxisMax
        double axisMax = axisMin + steps*incr;
        
        // If min step height is zero, reset steps
        if(aMinHeight<=0) {
            incr = steps*incr; steps = 1; }
        
        // If more than 10 continue
        if(steps>10) continue;
        
        // If height per step out of bounds, continue
        double dh = aHeight/steps; if(dh<aMinHeight) continue;
        
        // If maxValue within 15 points of height, continue
        double maxValueHeight = (aMaxValue-axisMin)/(axisMax-axisMin)*aHeight;
        if(maxValueHeight+15>aHeight) continue;
        
        // Break since increment, steps and padding are sufficient
        break;
    }
    
    // If only one step, reset delta
    if(steps==1 && aMinHeight>0)
        return getIntervalsFor(aMinValue, aMaxValue, aHeight, 0);
    
    // Create intervals list and return
    List <Double> ivals = new ArrayList(); for(int i=0;i<=steps;i++) ivals.add(axisMin + incr*i);
    return ivals;
}

/**
 * Returns the number of steps it takes to get to a value (or beyond) with given increment.
 */
private static int getStepsToValueWithIncrement(double aValue, double anIncr)
{
    //int steps = 1; double axisMax = incr; while(axisMax<maxValue && steps<11) { axisMax += incr; steps++; }
    double val = Math.abs(aValue);
    int steps = (int)Math.ceil(val/anIncr);
    return steps;
}

}