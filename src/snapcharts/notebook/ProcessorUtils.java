/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snapcharts.data.DataSet;
import snapcharts.data.DataType;
import snapcharts.data.DoubleArray;
import snapcharts.doc.ChartArchiver;
import snapcharts.model.Chart;
import java.util.Arrays;

/**
 * This class provides some utility methods to help processor.
 */
public class ProcessorUtils {

    /**
     * Returns the value as a string.
     */
    public static String getStringForValue(Object aValue)
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
     * Returns a sample XY DataSet.
     */
    public static DataSet getSampleDataSetXY()
    {
        double[] dataX = new double[] { 1, 2, 3, 4 };
        double[] dataY = new double[] { 1, 4, 3, 6 };
        return DataSet.newDataSetForTypeAndValues(DataType.XY, dataX, dataY);
    }

    /**
     * Returns a sample XYZZ DataSet.
     */
    public static DataSet getSampleDataSetXYZZ()
    {
        double[] dataX = new double[] { 1, 2, 3, 4 };
        double[] dataY = new double[] { 1, 2, 3, 4 };
        double[] dataZ = new double[dataX.length * dataY.length];
        for (int i = 0; i < dataX.length; i++)
            for (int j = 0; j < dataY.length; j++)
                dataZ[i * dataY.length + j] = dataX[i] * dataY[j];
        return DataSet.newDataSetForTypeAndValues(DataType.XYZZ, dataX, dataY, dataZ);
    }
}
