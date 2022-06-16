/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import java.util.Arrays;

/**
 * This class manages an array of primitive data values (float, double, String).
 */
public class DataArrays {

    /**
     * Constructor to stop construction.
     */
    private DataArrays()  { }

    /**
     * This DataArray subclass uses doubles.
     */
    public static class Number extends DataArray {

        // The double array
        protected double[]  _doubleArray;

        // The float array
        protected float[]  _floatArray;

        /**
         * Constructor.
         */
        public Number()
        {
            _doubleArray = new double[10];
        }

        /**
         * Constructor.
         */
        public Number(double[] doubleArray)
        {
            _doubleArray = doubleArray.clone();
            _length = _doubleArray.length;
        }

        /**
         * Returns the Object value at index.
         */
        @Override
        public java.lang.Double getValue(int anIndex)
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
         * Override to return as this subclass.
         */
        @Override
        protected Number clone()
        {
            // Do normal version
            Number clone = (Number) super.clone();

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
        }
    }

    /**
     * This DataArray subclass uses Strings.
     */
    public static class String extends DataArray {

        // The String array
        protected java.lang.String[]  _stringArray;

        /**
         * Constructor.
         */
        public String()
        {
            _stringArray = new java.lang.String[10];
        }

        /**
         * Constructor.
         */
        public String(java.lang.String[] stringArray)
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
        protected String clone()
        {
            // Do normal version
            String clone = (String) super.clone();

            // Clone arrays
            if (_stringArray != null)
                clone._stringArray = _stringArray.clone();

            // Return
            return clone;
        }
    }
}
