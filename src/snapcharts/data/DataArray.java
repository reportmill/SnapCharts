/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class manages an array of primitive data values (float, double, String).
 */
public abstract class DataArray implements Cloneable {

    // The name
    private String  _name;

    // The array length
    protected int  _length;

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
     * Returns the length.
     */
    public final int length()  { return _length; }

    /**
     * Sets the length.
     */
    public void setLength(int aValue)
    {
        // Set length
        _length = aValue;

        // Clear caches
        clearCaches();
    }

    /**
     * Returns the Object value at index.
     */
    public abstract Object getValue(int anIndex);

    /**
     * Removes the float value at index.
     */
    public abstract void removeIndex(int anIndex);

    /**
     * Called to clear caches.
     */
    public void clearCaches()  { }

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

        // Return
        return clone;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toStringProps implementation.
     */
    public String toStringProps()
    {
        // Add Name
        StringBuffer sb = new StringBuffer();
        String name = getName();
        if (name != null)
            sb.append("Name=").append(getName()).append(", ");

        // Add Length
        sb.append("Length=").append(length());

        // Return
        return sb.toString();
    }

    /**
     * Creates a new DataArray for given value array.
     */
    public static DataArray newDataArrayForArray(Object anArray)
    {
        // Handle double
        if (anArray instanceof double[])
            return new DoubleArray((double[]) anArray);

        // Handle String
        if (anArray instanceof String[])
            return new StringArray((String[]) anArray);

        // Throw a fit
        throw new RuntimeException("DataArray.newDataArrayForArray: Unsupported array type: " + anArray.getClass());
    }

    /**
     * Returns an array of DataArray for given double/String arrays.
     */
    public static DataArray[] newDataArraysForArrays(Object ... theValues)
    {
        // Get DataArrays
        int arrayCount = 0;
        DataArray[] dataArrays = new DataArray[theValues.length];
        for (Object valueArray : theValues) {
            if (valueArray == null)
                continue;
            DataArray dataArray = DataArray.newDataArrayForArray(valueArray);
            dataArrays[arrayCount++] = dataArray;
        }

        // Trim to size
        if (arrayCount != dataArrays.length)
            dataArrays = Arrays.copyOf(dataArrays, arrayCount);

        // Return
        return dataArrays;
    }

    /**
     * Creates a new DataArray for given value array.
     */
    public static DataArray[] newDataArraysForDataType(DataType dataType)
    {
        // Get channels and DataArrays
        DataChan[] channels = dataType.getChannelsXY();
        int channelCount = channels.length;
        DataArray[] dataArrays = new DataArray[channelCount];

        // Add DataArrays
        for (int i = 0; i < channelCount; i++) {
            DataChan chan = channels[i];
            switch (chan) {
                case X:
                case Y:
                case Z: dataArrays[i] = new DoubleArray(new double[0]); break;
                case C: dataArrays[i] = new StringArray(new String[0]); break;
                default: break;
            }
        }

        // Return
        return dataArrays;
    }
}
