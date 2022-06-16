/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import java.util.Arrays;

/**
 * This DataArray subclass uses Strings.
 */
public class StringArray extends DataArray {

    // The String array
    protected java.lang.String[]  _stringArray;

    /**
     * Constructor.
     */
    public StringArray()
    {
        _stringArray = new java.lang.String[10];
    }

    /**
     * Constructor.
     */
    public StringArray(java.lang.String[] stringArray)
    {
        _stringArray = stringArray.clone();
        _length = _stringArray.length;
    }

    /**
     * Returns the Object value at index.
     */
    @Override
    public java.lang.String getValue(int anIndex)
    {
        return _stringArray[anIndex];
    }

    /**
     * Sets the length.
     */
    @Override
    public void setLength(int aValue)
    {
        // Expand components array if needed
        if (aValue >= _length)
            _stringArray = Arrays.copyOf(_stringArray, aValue);

        // Set length
        _length = aValue;

        // Clear caches
        clearCaches();
    }

    /**
     * Returns the String value at index.
     */
    public java.lang.String getString(int anIndex)
    {
        return _stringArray[anIndex];
    }

    /**
     * Sets the String value at index.
     */
    public void setString(java.lang.String aValue, int anIndex)
    {
        // Set value
        _stringArray[anIndex] = aValue;

        // Clear caches
        clearCaches();
    }

    /**
     * Adds the String value at index.
     */
    public void addString(java.lang.String aValue)
    {
        addString(aValue, getLength());
    }

    /**
     * Adds the String value at index.
     */
    public void addString(java.lang.String aValue, int anIndex)
    {
        // Expand components array if needed
        if (_length == _stringArray.length)
            _stringArray = Arrays.copyOf(_stringArray, Math.max(_stringArray.length * 2, 20));

        // If index is inside current length, shift existing elements over
        if (anIndex < _length)
            System.arraycopy(_stringArray, anIndex, _stringArray, anIndex + 1, _length - anIndex);

        // Set value and increment length
        _stringArray[anIndex] = aValue;
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
        System.arraycopy(_stringArray, anIndex + 1, _stringArray, anIndex, _length - anIndex - 1);
        _length--;

        // Clear caches
        clearCaches();
    }

    /**
     * Returns the simple String array (trimmed to length).
     */
    public java.lang.String[] getStringArray()
    {
        if (_length != _stringArray.length)
            _stringArray = Arrays.copyOf(_stringArray, _length);
        return _stringArray;
    }

    /**
     * Override to return as this subclass.
     */
    @Override
    protected StringArray clone()
    {
        // Do normal version
        StringArray clone = (StringArray) super.clone();

        // Clone arrays
        if (_stringArray != null)
            clone._stringArray = _stringArray.clone();

        // Return
        return clone;
    }
}
