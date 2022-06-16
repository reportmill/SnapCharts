/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.util.SnapUtils;
import java.util.Objects;

/**
 * This class manages an array of primitive data values (float, double, String).
 */
public abstract class DataArray implements Cloneable {

    // The name
    private String  _name;

    // The array length
    protected int  _length;

    // The float array
    protected float[]  _floatArray;

    // The double array
    protected double[]  _doubleArray;

    // The String array
    protected String[]  _stringArray;

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        if (Objects.equals(aName, _name)) return;
        _name = aName;
    }

    /**
     * Returns the component type.
     */
    public abstract Class getComponentType();

    /**
     * Returns the Object value at index.
     */
    public abstract Object getValue(int anIndex);

    /**
     * Returns the length.
     */
    public final int getLength()  { return _length; }

    /**
     * Sets the length.
     */
    public abstract void setLength(int aValue);

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
     * Returns the double value at index.
     */
    public abstract double getDouble(int anIndex);

    /**
     * Sets the double value at index.
     */
    public abstract void setDouble(double aValue, int anIndex);

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
    public abstract void addDouble(double aValue, int anIndex);

    /**
     * Returns the String value at index.
     */
    public String getString(int anIndex)
    {
        Object value = getValue(anIndex);
        return SnapUtils.stringValue(value);
    }

    /**
     * Sets the String value at index.
     */
    public abstract void setString(String aValue, int anIndex);

    /**
     * Adds the String value at end.
     */
    public void addString(String aValue)
    {
        addString(aValue, getLength());
    }

    /**
     * Adds the String value at index.
     */
    public abstract void addString(String aValue, int anIndex);

    /**
     * Removes the float value at index.
     */
    public abstract void removeIndex(int anIndex);

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
     * Returns the double array.
     */
    public double[] getDoubleArray()
    {
        // If already set, just return
        if (_doubleArray != null) return _doubleArray;

        // Create/load array
        float[] floatArray = getFloatArray();
        int length = floatArray.length;
        double[] doubleArray = new double[length];
        for (int i = 0; i < length; i++)
            doubleArray[i] = floatArray[i];

        // Set and return
        return _doubleArray = doubleArray;
    }

    /**
     * Returns the String array.
     */
    public String[] getStringArray()
    {
        // If already set, just return
        if (_stringArray != null) return _stringArray;

        // Create/load array
        int length = getLength();
        String[] stringArray = new String[length];
        for (int i = 0; i < length; i++)
            stringArray[i] = getString(i);

        // Set and return
        return _stringArray = stringArray;
    }

    /**
     * Standard clone implementation.
     */
    @Override
    protected DataArray clone()
    {
        // Do normal version
        DataArray clone;
        try { clone = (DataArray) super.clone(); }
        catch (Exception e) { throw new RuntimeException(e); }

        // Clone arrays
        if (_floatArray != null) clone._floatArray = _floatArray.clone();
        if (_doubleArray != null) clone._doubleArray = _doubleArray.clone();
        if (_stringArray != null) clone._stringArray = _stringArray.clone();

        // Return
        return clone;
    }

    /**
     * Creates a new DataArray for given value array.
     */
    public static DataArray newDataArrayForArray(Object anArray)
    {
        // Handle double
        if (anArray instanceof double[])
            return new DataArrays.Double((double[]) anArray);

        // Handle float
        if (anArray instanceof float[])
            return new DataArrays.Float((float[]) anArray);

        // Handle String
        if (anArray instanceof String[])
            return new DataArrays.String((String[]) anArray);

        // Throw a fit
        throw new RuntimeException("DataArray.newDataArrayForArray: Unsupported array type: " + anArray.getClass());
    }
}
