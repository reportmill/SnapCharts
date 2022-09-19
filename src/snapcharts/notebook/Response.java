/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snapcharts.data.DataSet;
import snapcharts.data.DoubleArray;
import snapcharts.doc.ChartArchiver;
import snapcharts.model.Chart;
import java.util.Arrays;

/**
 * This class represents the output of a JavaEntry.
 */
public class Response {

    // The JavaEntry for this response
    private JavaEntry  _javaEntry;

    // The notebook that contains this entry
    private Notebook  _notebook;

    // The response as a value
    private Object  _value;

    // The text
    protected String  _text;

    /**
     * Constructor.
     */
    public Response(JavaEntry aJavaEntry)
    {
        _javaEntry = aJavaEntry;
        _notebook = aJavaEntry.getNotebook();
    }

    /**
     * Returns the entry.
     */
    public JavaEntry getJavaEntry()  { return _javaEntry; }

    /**
     * Returns the notebook that contains this entry.
     */
    public Notebook getNotebook()  { return _notebook; }

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
    }

    /**
     * Returns the text.
     */
    public String getText()
    {
        // If already set, just return
        if (_text != null) return _text;

        // Get text from value
        Object value = getValue();
        String text = getStringForValue(value);

        // Set and return
        return _text = text;
    }

    /**
     * Returns the value as a string.
     */
    private String getStringForValue(Object aValue)
    {
        // Handle null
        if (aValue == null)
            return "null";

        // Handle String
        if (aValue instanceof String)
            return (String) aValue;

        // Handle double[], DoubleArray
        if (aValue instanceof double[])
            return Arrays.toString((double[]) aValue);
        if (aValue instanceof DoubleArray) {
            double[] doubleArray = ((DoubleArray) aValue).doubleArray();
            return getStringForValue(doubleArray);
        }

        // Handle Chart
        if (aValue instanceof Chart) {
            Chart chart = (Chart) aValue;
            ChartArchiver chartArchiver = new ChartArchiver();
            String chartStr = chartArchiver.writeToXML(chart).toString();
            return chartStr;
        }

        // Handle DataSet
        if (aValue instanceof DataSet) {
            DataSet dataSet = (DataSet) aValue;
            ChartArchiver chartArchiver = new ChartArchiver();
            String chartStr = chartArchiver.writeToXML(dataSet).toString();
            return chartStr;
        }

        // Handle exception
        if (aValue instanceof Exception) {
            Exception exception = (Exception) aValue;
            Throwable rootCause = exception;
            while (rootCause.getCause() != null) rootCause = rootCause.getCause();
            return rootCause.toString();
        }

        // Handle anything
        return aValue.toString();
    }

    /**
     * Returns the index in Notebook request/response list.
     */
    public int getIndex()  { return _javaEntry.getIndex(); }

    /**
     * Standard toString method.
     */
    @Override
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + " { " + propStrings + " }";
    }

    /**
     * Standard toString method.
     */
    protected String toStringProps()
    {
        String textProp = "Text:" + getText();
        return textProp;
    }
}
