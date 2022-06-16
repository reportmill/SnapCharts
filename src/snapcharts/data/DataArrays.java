/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.data;
import snap.util.SnapUtils;
import java.util.Arrays;

/**
 * This class manages an array of primitive data values (float, double, String).
 */
public abstract class DataArrays implements Cloneable {

    /**
     * This DataArray subclass uses doubles.
     */
    public static class Number extends DataArray {

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
         * Returns the component type.
         */
        @Override
        public Class getComponentType()  { return double.class; }

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
            _floatArray = null;
            _stringArray = null;
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
            _floatArray = null;
            _stringArray = null;
        }

        /**
         * Adds the double value at index.
         */
        @Override
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
            _floatArray = null;
            _stringArray = null;
        }

        /**
         * Sets the String value at index.
         */
        @Override
        public void setString(java.lang.String aValue, int anIndex)
        {
            double value = SnapUtils.doubleValue(aValue);
            setDouble(value, anIndex);
        }

        /**
         * Adds the String value at index.
         */
        @Override
        public void addString(java.lang.String aValue, int anIndex)
        {
            double value = SnapUtils.doubleValue(aValue);
            addDouble(value, anIndex);
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
            _floatArray = null;
            _stringArray = null;
        }

        /**
         * Override to trim array.
         */
        @Override
        public double[] getDoubleArray()
        {
            if (_length != _doubleArray.length)
                _doubleArray = Arrays.copyOf(_doubleArray, _length);
            return _doubleArray;
        }

        /**
         * Override to return as this subclass.
         */
        @Override
        protected Number clone()
        {
            return (Number) super.clone();
        }
    }

    /**
     * This DataArray subclass uses Strings.
     */
    public static class String extends DataArray {

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
         * Returns the component type.
         */
        @Override
        public Class getComponentType()  { return String.class; }

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
            _floatArray = null;
            _doubleArray = null;
        }

        /**
         * Returns the double value at index.
         */
        @Override
        public final double getDouble(int anIndex)
        {
            java.lang.String str = getString(anIndex);
            return SnapUtils.doubleValue(str);
        }

        /**
         * Sets the double value at index.
         */
        @Override
        public final void setDouble(double aValue, int anIndex)
        {
            java.lang.String str = SnapUtils.stringValue(aValue);
            setString(str, anIndex);
        }

        /**
         * Adds the double value at index.
         */
        @Override
        public void addDouble(double aValue, int anIndex)
        {
            java.lang.String str = SnapUtils.stringValue(aValue);
            addString(str, anIndex);
        }

        /**
         * Sets the String value at index.
         */
        @Override
        public void setString(java.lang.String aValue, int anIndex)
        {
            // Set value
            _stringArray[anIndex] = aValue;

            // Clear caches
            _floatArray = null;
            _doubleArray = null;
        }

        /**
         * Adds the String value at index.
         */
        @Override
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
            _floatArray = null;
            _doubleArray = null;
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
            _floatArray = null;
            _doubleArray = null;
        }

        /**
         * Override to trim array.
         */
        @Override
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
            return (String) super.clone();
        }
    }
}
