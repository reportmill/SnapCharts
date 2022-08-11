/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.util.SnapUtils;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

/**
 * This NumberArray subclass stores values in a double array.
 */
public class DoubleArray extends NumberArray {

    /**
     * Constructor.
     */
    public DoubleArray()
    {
        _doubleArray = new double[0];
        _length = 0;
    }

    /**
     * Constructor.
     */
    public DoubleArray(double[] doubleArray)
    {
        _doubleArray = doubleArray.clone();
        _length = _doubleArray.length;
    }

    /**
     * Sets the length.
     */
    @Override
    public void setLength(int aValue)
    {
        // Expand components array if needed
        if (aValue >= _length)
            _doubleArray = Arrays.copyOf(_doubleArray, aValue);

        // Do normal version
        super.setLength(aValue);
    }

    /**
     * Returns the simple double array (trimmed to length).
     */
    public double[] doubleArray()
    {
        if (_length != _doubleArray.length)
            _doubleArray = Arrays.copyOf(_doubleArray, _length);
        return _doubleArray;
    }

    /**
     * Sets the double array.
     */
    public void setDoubleArray(double[] doubleArray)
    {
        _doubleArray = doubleArray;
        _length = _doubleArray.length;
        clearCaches();
    }

    /**
     * Returns the double value at index.
     */
    @Override
    public final double getDouble(int anIndex)
    {
        return _doubleArray[anIndex];
    }

    /**
     * Sets the double value at index.
     */
    @Override
    public final void setDouble(double aValue, int anIndex)
    {
        // Set value
        _doubleArray[anIndex] = aValue;

        // Clear caches
        clearCaches();
    }

    /**
     * Adds the double value at index.
     */
    public void addDouble(double aValue, int anIndex)
    {
        // Expand components array if needed
        if (_length == _doubleArray.length)
            _doubleArray = Arrays.copyOf(_doubleArray, Math.max(_doubleArray.length * 2, 20));

        // If index is inside current length, shift existing elements over
        if (anIndex < _length)
            System.arraycopy(_doubleArray, anIndex, _doubleArray, anIndex + 1, _length - anIndex);

        // Set value and increment length
        _doubleArray[anIndex] = aValue;
        _length++;

        // Clear caches
        clearCaches();
    }

    /**
     * Removes the value at index.
     */
    public void removeIndex(int anIndex)
    {
        // Shift remaining elements in
        System.arraycopy(_doubleArray, anIndex + 1, _doubleArray, anIndex, _length - anIndex - 1);
        _length--;

        // Clear caches
        clearCaches();
    }

    /**
     * Override to customize.
     */
    public void clearCaches()
    {
        super.clearCaches();
        _floatArray = null;
    }

    /**
     * Returns a DoubleArray consisting of the results of applying the given function to the elements of this DoubleArray.
     */
    public DoubleArray map(DoubleUnaryOperator mapper)
    {
        double[] doubleArray = doubleArray();
        DoubleStream doubleStream = DoubleStream.of(doubleArray);
        DoubleStream doubleStreamMapped = doubleStream.map(mapper);
        double[] doubleArrayMapped = doubleStreamMapped.toArray();
        return new DoubleArray(doubleArrayMapped);
    }

    /**
     * Returns a DoubleArray consisting of the elements of this DoubleArray that match the given predicate.
     */
    public DoubleArray filter(DoublePredicate predicate)
    {
        double[] doubleArray = doubleArray();
        DoubleStream doubleStream = DoubleStream.of(doubleArray);
        DoubleStream doubleStreamFiltered = doubleStream.filter(predicate);
        double[] doubleArrayFiltered = doubleStreamFiltered.toArray();
        return new DoubleArray(doubleArrayFiltered);
    }

    /**
     * Returns raw double array.
     */
    public double[] toArray()
    {
        return doubleArray();
    }

    /**
     * Returns a DoubleStream for DoubleArray.
     */
    public DoubleStream stream()
    {
        return DoubleStream.of(doubleArray());
    }

    /**
     * Override to return as this subclass.
     */
    @Override
    protected DoubleArray clone()
    {
        // Do normal version
        DoubleArray clone = (DoubleArray) super.clone();

        // Clone arrays
        if (_doubleArray != null)
            clone._doubleArray = _doubleArray.clone();

        // Return
        return clone;
    }

    /**
     * Returns DoubleArray from raw double values or array.
     */
    public static DoubleArray of(Object ... theDoubles)
    {
        // Handle empty array
        if (theDoubles.length == 0)
            return new DoubleArray(new double[0]);

        // Handle DoubleArray
        Object obj0 = theDoubles[0];
        if (obj0 instanceof DoubleArray)
            return ((DoubleArray) obj0);

        // Handle double[]
        if (obj0 instanceof double[])
            return new DoubleArray((double[]) obj0);

        // Handle Array (could be int[], float[], etc.)
        if (obj0.getClass().isArray()) {

            // Get double[] using Array get()
            int length = Array.getLength(obj0);
            double[] doubleArray = new double[length];
            for (int i = 0; i < length; i++) {
                Object val = Array.get(obj0, i);
                doubleArray[i] = SnapUtils.doubleValue(val);
            }

            // Return
            return new DoubleArray(doubleArray);
        }

        // Iterate over values and convert to double
        double[] doubleArray = new double[theDoubles.length];
        for (int i = 0; i < theDoubles.length; i++)
            doubleArray[i] = SnapUtils.doubleValue(theDoubles[i]);

        // Return
        return new DoubleArray(doubleArray);
    }

    /**
     * Returns DoubleArray for given range.
     */
    public static DoubleArray fromMinMax(double aMin, double aMax)
    {
        return fromMinMaxCount(aMin, aMax, 80);
    }

    /**
     * Returns DoubleArray for given range.
     */
    public static DoubleArray fromMinMaxCount(double aMin, double aMax, int aCount)
    {
        // Fix illegal min/max
        if (aMin > aMax) { double tmp = aMin; aMin = aMax; aMax = tmp; }

        // Create double array
        double incr = (aMax - aMin) / aCount;
        double[] doubleArray = new double[aCount];
        for (int i = 0; i < aCount; i++)
            doubleArray[i] = aMin + incr * i;

        // Return
        return new DoubleArray(doubleArray);
    }
}
