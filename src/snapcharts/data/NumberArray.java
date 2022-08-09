/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.util.ArrayUtils;

/**
 * This DataArray subclass uses doubles.
 */
public abstract class NumberArray extends DataArray {

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
        super();
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
     * Returns the double value at index.
     */
    public double getDouble(int anIndex)
    {
        return getFloat(anIndex);
    }

    /**
     * Sets the double value at index.
     */
    public void setDouble(double aValue, int anIndex)
    {
        setFloat(anIndex, anIndex);
    }

    /**
     * Adds the double value at end.
     */
    public void addDouble(double aValue)
    {
        addDouble(aValue, length());
    }

    /**
     * Adds the double value at index.
     */
    public abstract void addDouble(double aValue, int anIndex);

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
     * Removes the value at index.
     */
    public abstract void removeIndex(int anIndex);

    /**
     * Returns the simple double array (trimmed to length).
     */
    public double[] doubleArray()
    {
        // If already set, just return
        if (_doubleArray != null) return _doubleArray;

        // Create, set, return
        float[] floatArray = floatArray();
        double[] doubleArray = ArrayUtils.doubleArray(floatArray);
        return _doubleArray = doubleArray;
    }

    /**
     * Returns the float array.
     */
    public float[] floatArray()
    {
        // If already set, just return
        if (_floatArray != null) return _floatArray;

        // Create, set, return
        double[] doubleArray = doubleArray();
        float[] floatArray = ArrayUtils.floatArray(doubleArray);
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
        int length = length();
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
     * Called to clear caches.
     */
    public void clearCaches()
    {
        super.clearCaches();
        _minMax = null;
    }

    /**
     * Override to return as this subclass.
     */
    @Override
    protected NumberArray clone()
    {
        // Do normal version
        NumberArray clone = (NumberArray) super.clone();

        // Return
        return clone;
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
