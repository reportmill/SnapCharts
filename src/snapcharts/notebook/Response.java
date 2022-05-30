/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;

/**
 * This Entry subclass represents the output of a processed Request entry.
 */
public class Response extends Entry {

    // The response as a value
    private Object  _value;

    // The response as text
    private String  _valueStr;

    /**
     * Returns the response as a value.
     */
    public Object getValue()  { return _value; }

    /**
     * Sets the response as a value.
     */
    public void setValue(Object aValue)
    {
        _value = aValue;

        if (aValue instanceof String)
            setText((String) aValue);
    }

    /**
     * Returns the response as a string.
     */
    public String getValueString()
    {
        if (_valueStr != null) return _valueStr;

        Object value = getValue();
        String valueStr = ""; //getStringForValue(value);
        return _valueStr = valueStr;
    }
}
