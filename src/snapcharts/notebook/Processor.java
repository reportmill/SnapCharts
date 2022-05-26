/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.KeyChain;
import snap.util.SnapUtils;
import snapcharts.data.DataSet;
import snapcharts.data.DataSetUtils;
import snapcharts.data.DataType;
import snapcharts.doc.ChartArchiver;
import snapcharts.model.Chart;
import snapcharts.model.ChartType;
import snapcharts.model.Trace;
import java.util.HashMap;
import java.util.Map;

/**
 * This class processes request expressions.
 */
public class Processor implements KeyChain.FunctionHandler {

    // A map holding values
    private Map<String,Double>  _variables = new HashMap<>();

    /**
     * Returns a value for given object and keychain.
     */
    public Object getValue(Object anObj, KeyChain aKC)
    {
        // Register as FunctionHandler
        KeyChain.setFunctionHandler(this);

        // Get value
        Object value = KeyChain.getValue(anObj, aKC);

        // Return
        return value;
    }

    /**
     * Returns the string value of a keychain.
     */
    public String getStringValue(Object anObj, KeyChain aKC)
    {
        Object value = getValue(anObj, aKC);
        String str = SnapUtils.stringValue(value);
        return str;
    }

    /**
     * Handles FunctionCall KeyChains.
     */
    @Override
    public Object getValueFunctionCall(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        String name = aKeyChain.getChildString(0);
        KeyChain argList = aKeyChain.getChildKeyChain(1);

        switch (name) {
            case "dataSet": return dataSet(anObj, argList);
            case "plot3D": return plot3D(anObj, argList);
            case "plot": return plot3D(anObj, argList);
            case "sin": return sinFunc(anObj, argList);
        }
        return null;
    }

    /**
     * Creates and returns a DataSet for (function, varArray1, varArray2).
     */
    public DataSet dataSet(Object anObj, KeyChain aKeyChain)
    {
        // Get expression to plot
        KeyChain expr = aKeyChain.getChildKeyChain(0);

        // Get arg 2 as value array
        KeyChain arg2 = aKeyChain.getChildKeyChain(1);
        String name1 = arg2.getChildString(0);
        KeyChain valueArray1KeyChain = arg2.getChildKeyChain(1);
        double[] valueArray1 = getValueAsDoubleArray(anObj, valueArray1KeyChain);
        int count1 = valueArray1.length;

        // Get arg 3 as value array
        KeyChain arg3 = aKeyChain.getChildKeyChain(2);
        String name2 = arg3.getChildString(0);
        KeyChain valueArray2KeyChain = arg3.getChildKeyChain(1);
        double[] valueArray2 = getValueAsDoubleArray(anObj, valueArray2KeyChain);
        int count2 = valueArray1.length;

        // Create valueArray for expression
        double[] valueArray3 = new double[count1 * count2];

        // Iterate over valueArray1
        for (int i = 0; i < count1; i++) {

            // Get/set valueArray1 value
            double valX = valueArray1[i];
            _variables.put(name1, valX);

            // Iterate over valueArray2
            for (int j = 0; j < count2; j++) {

                // Get/set valueArray2 value
                double valY = valueArray2[j];
                _variables.put(name2, valY);

                // Get/set valueArray3
                double val = KeyChain.getDoubleValue(_variables, expr);
                valueArray3[i * count1 + j] = val;
            }
        }

        // Create DataSet
        DataSet dataSet = DataSet.newDataSet();
        dataSet.setDataType(DataType.XYZZ);
        DataSetUtils.addDataPointsXYZZ(dataSet, valueArray1, valueArray2, valueArray3);

        // Return
        return dataSet;
    }

    /**
     * Creates and returns a Plot3D.
     */
    public String plot3D(Object anObj, KeyChain aKeyChain)
    {
        // Get DataSet
        DataSet dataSet = dataSet(anObj, aKeyChain);

        // Create Trace for DataSet
        Trace trace = new Trace();
        trace.setDataSet(dataSet);

        // Create Chart with Trace
        Chart chart = new Chart();
        chart.setType(ChartType.CONTOUR_3D);
        chart.addTrace(trace);

        // Write chart to string
        ChartArchiver chartArchiver = new ChartArchiver();
        String chartStr = chartArchiver.writeToXML(chart).toString();

        // Return
        return chartStr;
    }

    /**
     * Returns ArgList KeyChain as double array.
     */
    private double[] getValueAsDoubleArray(Object anObj, KeyChain aKeyChain)
    {
        // Get start value
        KeyChain startKC1 = aKeyChain.getChildKeyChain(0);
        double start = KeyChain.getDoubleValue(anObj, startKC1);

        // Get end value
        KeyChain endKC1 = aKeyChain.getChildKeyChain(1);
        double end = KeyChain.getDoubleValue(anObj, endKC1);

        // Get count value
        KeyChain arg3KC = aKeyChain.getChildCount() >= 3 ? aKeyChain.getChildKeyChain(2) : null;
        int count = arg3KC != null ? KeyChain.getIntValue(anObj, arg3KC) : 20;
        double incr = (end - start) / count;

        // Create/fill double array
        double[] valueArray = new double[count];
        for (int i = 0; i < count; i++)
            valueArray[i] = start + incr * i;

        // Return
        return valueArray;
    }

    /**
     * A Sin function takes KeyChain.
     */
    public Object sinFunc(Object anObj, KeyChain aKeyChain)
    {
        KeyChain arg0 = aKeyChain.getChildKeyChain(0);
        double value = KeyChain.getDoubleValue(anObj, arg0);
        double sinVal = Math.sin(value);
        return sinVal;
    }
}
