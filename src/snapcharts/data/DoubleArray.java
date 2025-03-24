/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.util.Convert;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.*;

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
    public DoubleArray clone()
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
    public static DoubleArray of(Object anObj)
    {
        // Handle DoubleArray
        if (anObj instanceof DoubleArray)
            return ((DoubleArray) anObj);

        // Handle double[]
        if (anObj instanceof double[])
            return new DoubleArray((double[]) anObj);

        // Handle Array (could be int[], float[], etc.)
        if (anObj.getClass().isArray()) {

            // Get double[] using Array get()
            int length = Array.getLength(anObj);
            double[] doubleArray = new double[length];
            for (int i = 0; i < length; i++) {
                Object val = Array.get(anObj, i);
                doubleArray[i] = Convert.doubleValue(val);
            }
            return new DoubleArray(doubleArray);
        }

        // Handle Stream
        if (anObj instanceof Stream) {
            Stream<?> stream = (Stream<?>) anObj;
            double[] doubleArray = stream.mapToDouble(obj -> Convert.doubleValue(obj)).toArray();
            return new DoubleArray(doubleArray);
        }

        // Handle BaseStream
        if (anObj instanceof BaseStream) {
            BaseStream<Object,?> baseStream = (BaseStream<Object, ?>) anObj;
            Iterator<Object> iterator = baseStream.iterator();
            Iterable<Object> iterable = () -> iterator;
            Stream<Object> stream = StreamSupport.stream(iterable.spliterator(), false);
            return of(stream);
        }

        // Just get double value of object and return as array
        double doubleValue = Convert.doubleValue(anObj);
        return new DoubleArray(new double[] { doubleValue });
    }

    /**
     * Returns DoubleArray from raw double values or array.
     */
    public static DoubleArray of(Object ... theDoubles)
    {
        // Iterate over values and convert to double
        double[] doubleArray = new double[theDoubles.length];
        for (int i = 0; i < theDoubles.length; i++)
            doubleArray[i] = Convert.doubleValue(theDoubles[i]);

        // Return
        return new DoubleArray(doubleArray);
    }

    /**
     * Returns DoubleArray for given range.
     */
    public static DoubleArray fromMinMax(double aMin, double aMax)
    {
        int count = snap.util.SnapEnv.isWebVM ? 16 : 80;
        return fromMinMaxCount(aMin, aMax, count);
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
