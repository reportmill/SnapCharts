/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.KeyChain;
import snap.util.KeyValue;
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
        // Get value
        Object value;
        try { value = getValue(anObj, aKC); }

        // If exception, set as response
        catch (Exception e) {
            value = e.toString();
        }

        // Convert to string and return
        String str = value != null ? SnapUtils.stringValue(value) : "null";
        return str;
    }

    /**
     * Handles FunctionCall KeyChains.
     */
    @Override
    public Object getValueFunctionCall(Object aRoot, Object anObj, KeyChain aKeyChain)
    {
        // Get function name (lowercase) and arg list
        String functionName = aKeyChain.getChildString(0).toLowerCase();
        KeyChain argList = aKeyChain.getChildKeyChain(1);

        // Dispatch for function name
        switch (functionName) {

            // Handle DataSet
            case "dataset": return dataSet(anObj, argList);

            // Handle Plot
            case "plot": return plot(anObj, argList);

            // Handle Plot3D
            case "plot3d": return plot3D(anObj, argList);

            // Handle Sin, Cos
            case "sin": return sinFunc(anObj, argList);
            case "cos": return cosFunc(anObj, argList);
        }

        // Complain and return null since function not found
        System.err.println("Processor.getValueFunctionCall: Unknown function: " + functionName);
        return null;
    }

    /**
     * Creates and returns a DataSet for (function, x array, y array).
     */
    public DataSet dataSet(Object anObj, KeyChain aKeyChain)
    {
        // Get function expression to plot
        KeyChain functExprKeyChain = aKeyChain.getChildKeyChain(0);
        int argCount = aKeyChain.getChildCount();
        int defaultDataLen = argCount == 2 ? 200 : 80;

        // Get X expression KeyChain (arg 2) and evaluate for dataX KeyValue<double[]>
        KeyChain exprX = aKeyChain.getChildKeyChain(1);
        KeyValue<double[]> dataXKeyVal = getValueAsDoubleArrayKeyValue(anObj, exprX, defaultDataLen);

        // If only two args, get XY DataSet and return
        if (aKeyChain.getChildCount() < 3) {
            DataSet dataSet = getDataSetXY(functExprKeyChain, dataXKeyVal);
            return dataSet;
        }

        // Get Y expression KeyChain (arg 3) and evaluate for dataX KeyValue<double[]>
        KeyChain exprY = aKeyChain.getChildKeyChain(2);
        KeyValue<double[]> dataYKeyVal = getValueAsDoubleArrayKeyValue(anObj, exprY, defaultDataLen);

        // Create DataSet
        DataSet dataSet = getDataSetXYZZ(functExprKeyChain, dataXKeyVal, dataYKeyVal);
        return dataSet;
    }

    /**
     * Creates and returns a XY DataSet for (function, x array).
     */
    private DataSet getDataSetXY(KeyChain exprKeyChain, KeyValue<double[]> dataXKeyVal)
    {
        // Get X key (probably 'x') and data array
        String keyX = dataXKeyVal.getKey();
        double[] dataX = dataXKeyVal.getValue();
        int countX = dataX.length;

        // Create dataY for expression values
        double[] dataY = new double[countX];

        // Iterate over dataX values
        for (int i = 0; i < countX; i++) {

            // Get/set dataX value
            double valX = dataX[i];
            _variables.put(keyX, valX);

            // Get/set dataY
            double val = KeyChain.getDoubleValue(_variables, exprKeyChain);
            dataY[i] = val;
        }

        // Create DataSet, return
        DataSet dataSet = DataSet.newDataSetForTypeAndValues(DataType.XY, dataX, dataY);
        return dataSet;
    }

    /**
     * Creates and returns a XY DataSet for (function, x array).
     */
    private DataSet getDataSetXYZZ(KeyChain exprKeyChain, KeyValue<double[]> dataXKeyVal, KeyValue<double[]> dataYKeyVal)
    {
        // Get X key (probably 'x') and data array
        String keyX = dataXKeyVal.getKey();
        double[] dataX = dataXKeyVal.getValue();
        int countX = dataX.length;

        // Get Y key (probably 'x') and data array
        String keyY = dataYKeyVal.getKey();
        double[] dataY = dataYKeyVal.getValue();
        int countY = dataY.length;

        // Create valueArray for expression
        double[] dataZ = new double[countX * countY];

        // Iterate over dataX values
        for (int i = 0; i < countX; i++) {

            // Get/set dataX value
            double valX = dataX[i];
            _variables.put(keyX, valX);

            // Iterate over dataY values
            for (int j = 0; j < countY; j++) {

                // Get/set dataY value
                double valY = dataY[j];
                _variables.put(keyY, valY);

                // Get/set dataZ
                double val = KeyChain.getDoubleValue(_variables, exprKeyChain);
                dataZ[i * countY + j] = val;
            }
        }

        // Create DataSet
        DataSet dataSet = DataSet.newDataSet();
        dataSet.setDataType(DataType.XYZZ);
        DataSetUtils.addDataPointsXYZZ(dataSet, dataX, dataY, dataZ);

        // Return
        return dataSet;
    }

    /**
     * Creates and returns a Plot.
     */
    public String plot(Object anObj, KeyChain aKeyChain)
    {
        // Get DataSet
        DataSet dataSet = dataSet(anObj, aKeyChain);

        // Create Trace for DataSet
        Trace trace = new Trace();
        trace.setDataSet(dataSet);

        // Get ChartType
        ChartType chartType = ChartType.SCATTER;
        if (dataSet.getDataType().hasZ())
            chartType = ChartType.CONTOUR;

        // Create Chart with Trace
        Chart chart = new Chart();
        chart.setType(chartType);
        chart.getAxisX().setTitle("X");
        chart.getAxisY().setTitle("Y");
        chart.addTrace(trace);

        // Write chart to string
        ChartArchiver chartArchiver = new ChartArchiver();
        String chartStr = chartArchiver.writeToXML(chart).toString();

        // Return
        return chartStr;
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
        chart.getAxisX().setTitle("X");
        chart.getAxisY().setTitle("Y");
        chart.getAxisZ().setTitle("Z");
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
    private KeyValue<double[]> getValueAsDoubleArrayKeyValue(Object anObj, KeyChain aKeyChain, int defaultDataLen)
    {
        // Get key string
        String key = aKeyChain.getChildString(0);

        // Get array keyChain and array
        KeyChain valueArrayKeyChain = aKeyChain.getChildKeyChain(1);
        double[] valueArray = getValueAsDoubleArray(anObj, valueArrayKeyChain, defaultDataLen);

        // Return Key/Value
        return new KeyValue<>(key, valueArray);
    }

    /**
     * Returns ArgList KeyChain as double array.
     */
    private double[] getValueAsDoubleArray(Object anObj, KeyChain aKeyChain, int defaultDataLen)
    {
        // Get start value
        KeyChain startKC1 = aKeyChain.getChildKeyChain(0);
        double start = KeyChain.getDoubleValue(anObj, startKC1);

        // Get end value
        KeyChain endKC1 = aKeyChain.getChildKeyChain(1);
        double end = KeyChain.getDoubleValue(anObj, endKC1);

        // Get count value
        KeyChain arg3KC = aKeyChain.getChildCount() >= 3 ? aKeyChain.getChildKeyChain(2) : null;
        int count = arg3KC != null ? KeyChain.getIntValue(anObj, arg3KC) : defaultDataLen;
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

    /**
     * A Sin function takes KeyChain.
     */
    public Object cosFunc(Object anObj, KeyChain aKeyChain)
    {
        KeyChain arg0 = aKeyChain.getChildKeyChain(0);
        double value = KeyChain.getDoubleValue(anObj, arg0);
        double sinVal = Math.cos(value);
        return sinVal;
    }
}
