/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snapcharts.util.MinMax;

import java.util.Arrays;

/**
 * This DataArray subclass uses doubles.
 */
public class NumberArray extends DataArray {

    // The units
    private DataUnit  _unit;

    // The double array
    protected double[]  _doubleArray;

    // The float array
    protected float[]  _floatArray;

    // Min/Max for values
    private MinMax  _minMax;

    /**
     * Constructor.
     */
    public NumberArray()
    {
        _doubleArray = new double[10];
    }

    /**
     * Constructor.
     */
    public NumberArray(double[] doubleArray)
    {
        _doubleArray = doubleArray.clone();
        _length = _doubleArray.length;
    }

    /**
     * Returns the unit for data values.
     */
    public DataUnit getUnit()  { return _unit; }

    /**
     * Sets the unit for data values.
     */
    public void setUnit(DataUnit aValue)
    {
        _unit = aValue;
    }

    /**
     * Returns the Object value at index.
     */
    @Override
    public Double getValue(int anIndex)
    {
        return _doubleArray[anIndex];
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

        // Set length
        _length = aValue;

        // Clear caches
        clearCaches();
    }

    /**
     * Returns the double value at index.
     */
    public final double getDouble(int anIndex)
    {
        return _doubleArray[anIndex];
    }

    /**
     * Sets the double value at index.
     */
    public final void setDouble(double aValue, int anIndex)
    {
        // Set value
        _doubleArray[anIndex] = aValue;

        // Clear caches
        clearCaches();
    }

    /**
     * Adds the double value at end.
     */
    public void addDouble(double aValue)
    {
        addDouble(aValue, getLength());
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
     * Returns the float value at index.
     */
    public float getFloat(int anIndex)
    {
        return (float) getDouble(anIndex);
    }

    /**
     * Sets the float value at index.
     */
    public void setFloat(float aValue, int anIndex)
    {
        setDouble(aValue, anIndex);
    }

    /**
     * Adds the float value at end.
     */
    public void addFloat(float aValue)
    {
        addFloat(aValue, getLength());
    }

    /**
     * Adds the float value at index.
     */
    public void addFloat(float aValue, int anIndex)
    {
        addDouble(aValue, anIndex);
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
     * Returns the simple double array (trimmed to length).
     */
    public double[] getDoubleArray()
    {
        if (_length != _doubleArray.length)
            _doubleArray = Arrays.copyOf(_doubleArray, _length);
        return _doubleArray;
    }

    /**
     * Returns the float array.
     */
    public float[] getFloatArray()
    {
        // If already set, just return
        if (_floatArray != null) return _floatArray;

        // Create/load array
        double[] doubleArray = getDoubleArray();
        int length = doubleArray.length;
        float[] floatArray = new float[length];
        for (int i = 0; i < length; i++)
            floatArray[i] = (float) doubleArray[i];

        // Set and return
        return _floatArray = floatArray;
    }

    /**
     * Returns the minimum X value in this dataset.
     */
    public MinMax getMinMax()
    {
        // If already set, just return
        if (_minMax != null) return _minMax;

        // If no points, just return 0,0
        int length = getLength();
        if (length == 0)
            return _minMax = new MinMax(0, 0);

        // Iterate over values to get min/max
        double min = Float.MAX_VALUE;
        double max = -Float.MAX_VALUE;
        for (int i = 0; i < length; i++) {
            double value = getDouble(i);
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        // Return MinMax
        return _minMax = new MinMax(min, max);
    }

    /**
     * Override to return as this subclass.
     */
    @Override
    protected NumberArray clone()
    {
        // Do normal version
        NumberArray clone = (NumberArray) super.clone();

        // Clone arrays
        if (_doubleArray != null)
            clone._doubleArray = _doubleArray.clone();

        // Return
        return clone;
    }

    /**
     * Called to clear caches.
     */
    public void clearCaches()
    {
        super.clearCaches();
        _floatArray = null;
        _minMax = null;
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        StringBuffer sb = new StringBuffer(super.toStringProps());
        MinMax minMax = getMinMax();
        sb.append(", Min=").append(minMax.getMin());
        sb.append(", Max=").append(minMax.getMax());
        return sb.toString();
    }
}
