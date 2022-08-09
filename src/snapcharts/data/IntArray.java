/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import java.util.Arrays;

/**
 * IntArray.
 */
public class IntArray {

    // The items
    private int[]  _intArray;

    // The number of items
    private int  _length;

    /**
     * Constructor.
     */
    public IntArray()
    {
        _intArray = new int[16];
        _length = 0;
    }

    /**
     * Returns the length.
     */
    public final int length()  { return _length; }

    /**
     * Returns the simple int array (trimmed to length).
     */
    public int[] intArray()
    {
        if (_length != _intArray.length)
            _intArray = Arrays.copyOf(_intArray, _length);
        return _intArray;
    }

    /**
     * Returns the double value at index.
     */
    public final int getInt(int anIndex)  { return _intArray[anIndex]; }

    /**
     * Add.
     */
    public void add(int aValue)
    {
        ensureCapacity(_length + 1);
        _intArray[_length++] = aValue;
    }

    /**
     * Remove.
     */
    public void removeIndex(int anIndex)
    {
        System.arraycopy(_intArray, anIndex+1, _intArray, anIndex, _length - anIndex - 1);
        _length--;
    }

    /**
     * Ensure capacity.
     */
    public void ensureCapacity(int aSize)
    {
        if (aSize > _intArray.length)
            _intArray = Arrays.copyOf(_intArray, aSize);
    }

    /**
     * Clear.
     */
    public void clear()  { _length = 0; }

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
        // Add Length
        StringBuffer sb = new StringBuffer();
        sb.append("Length=").append(length());

        // Append items
        String itemsStr = _intArray.length<20 ? Arrays.toString(_intArray) : Arrays.toString(Arrays.copyOf(_intArray,20)) + " ...";
        sb.append(", Items=").append(itemsStr);

        // Return
        return sb.toString();
    }
}
