/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.KeyChain;
import snap.util.KeyValue;
import snapcharts.data.DataSet;
import snapcharts.data.DataType;

/**
 * This class processes data requests.
 */
public class SubProcData extends SubProc {

    /**
     * Constructor.
     */
    public SubProcData(Processor aProcessor)
    {
        super(aProcessor);
    }

    /**
     * Handles processing functions supported by this class.
     */
    public Object getValueFunctionCall(Object aRoot, Object anObj, String functionName, KeyChain argListKC)
    {
        switch (functionName) {

            // Handle DataSet
            case "dataset": return dataSetOrSample(anObj, argListKC);
            case "dataset3d": return dataSetOrSample3D(anObj, argListKC);
        }

        return null;
    }

    /**
     * Creates and returns a DataSet for (function, x array, y array).
     */
    public DataSet dataSet(Object anObj, KeyChain aKeyChain)
    {
        // If no args, return LastDataSet
        if (aKeyChain.getChildCount() == 0) {
            Notebook notebook = getNotebook();
            return notebook.getLastResponseValueForClass(DataSet.class);
        }

        // Get function expression to plot
        KeyChain functExprKeyChain = aKeyChain.getChildKeyChain(0);
        int argCount = aKeyChain.getChildCount();
        int defaultDataLen = argCount == 2 ? 200 : 80;

        // Get X expression KeyChain (arg 2) and evaluate for dataX KeyValue<double[]>
        KeyChain exprX = aKeyChain.getChildKeyChain(1);
        KeyValue<double[]> dataXKeyVal = getValueAsDoubleArrayKeyValue(anObj, exprX, defaultDataLen);

        // If only two args, get XY DataSet and return
        if (aKeyChain.getChildCount() < 3)
            return getDataSetXY(functExprKeyChain, dataXKeyVal);

        // Get Y expression KeyChain (arg 3) and evaluate for dataX KeyValue<double[]>
        KeyChain exprY = aKeyChain.getChildKeyChain(2);
        KeyValue<double[]> dataYKeyVal = getValueAsDoubleArrayKeyValue(anObj, exprY, defaultDataLen);

        // Create DataSet and return
        return getDataSetXYZZ(functExprKeyChain, dataXKeyVal, dataYKeyVal);
    }

    /**
     * Creates and returns a XY DataSet for (function, x array).
     */
    public DataSet getDataSetXY(KeyChain exprKeyChain, KeyValue<double[]> dataXKeyVal)
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

        // Create DataSet, set name to expr and return
        DataSet dataSet = DataSet.newDataSetForTypeAndValues(DataType.XY, dataX, dataY);
        dataSet.setName(exprKeyChain.toString());
        return dataSet;
    }

    /**
     * Creates and returns a XY DataSet for (function, x array).
     */
    public DataSet getDataSetXYZZ(KeyChain exprKeyChain, KeyValue<double[]> dataXKeyVal, KeyValue<double[]> dataYKeyVal)
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
        DataSet dataSet = DataSet.newDataSetForTypeAndValues(DataType.XYZZ, dataX, dataY, dataZ);
        dataSet.setName(exprKeyChain.toString());

        // Return
        return dataSet;
    }

    /**
     * Same as dataSet(), but returns sample DataSet if can't create/find one.
     */
    public DataSet dataSetOrSample(Object anObj, KeyChain aKeyChain)
    {
        DataSet dataSet = dataSet(anObj, aKeyChain);
        if (dataSet == null)
            dataSet = ProcessorUtils.getSampleDataSetXY();
        return dataSet;
    }

    /**
     * Same as dataSet(), but returns sample DataSet if can't create/find one.
     */
    public DataSet dataSetOrSample3D(Object anObj, KeyChain aKeyChain)
    {
        DataSet dataSet = dataSet(anObj, aKeyChain);
        if (dataSet == null)
            dataSet = ProcessorUtils.getSampleDataSetXYZZ();
        return dataSet;
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
}
