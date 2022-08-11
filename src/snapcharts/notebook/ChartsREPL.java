/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snapcharts.data.*;
import snapcharts.model.Chart;
import snapcharts.model.Trace;
import snapcharts.model.TraceType;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a REPL base class specific for charts.
 */
public class ChartsREPL {

    /**
     * Creates and return a DataArray.
     */
    public static DoubleArray doubleArray(Object ... theDoubles)
    {
        return DoubleArray.of(theDoubles);
    }

    /**
     * Creates and return a DataArray.
     */
    public static DataArray dataArray(Object anObj)
    {
        // Handle null
        if (anObj == null) return null;

        // Handle DataArray
        if (anObj instanceof DataArray)
            return (DataArray) anObj;

        // Handle array of anything
        if (anObj.getClass().isArray())
            return doubleArray(anObj);

        // Return not handled
        return null;
    }

    /**
     * Creates and returns a Dataset.
     */
    public static DataSet dataSet(Object ... theObjects)
    {
        // Handle DataSet
        if (theObjects.length > 0 && theObjects[0] instanceof DataSet)
            return (DataSet) theObjects[0];

        // Get objects as DataArray array
        List<DataArray> dataArraysList = new ArrayList<>();
        for (Object obj : theObjects) {
            DataArray dataArray = dataArray(obj);
            if (dataArray != null)
                dataArraysList.add(dataArray);
        }

        // Return if empty
        if (dataArraysList.size() < 2)
            throw new IllegalArgumentException("ChartsREPL.dataSet: Not enough data arrays");

        // Create DataArrays and get DataType
        DataArray[] dataArrays = dataArraysList.toArray(new DataArray[0]);
        DataType dataType = dataArrays.length < 3 ? DataType.XY : DataType.XYZ;

        // Create/config DataSet
        DataSet dataSet = new DataSetImpl();
        dataSet.setDataType(dataType);
        dataSet.setDataArrays(dataArrays);

        // Return
        return dataSet;
    }

    /**
     * Creates and returns a Chart.
     */
    public static Chart chart(Object ... theObjects)
    {
        // Get DataSet from theObjects
        DataSet dataSet = dataSet(theObjects);

        // Get TraceType
        TraceType traceType = TraceType.Scatter;
        if (dataSet.getDataType().hasZ())
            traceType = TraceType.Contour;

        // Create Trace for DataSet
        Trace trace = Trace.newTraceForClass(traceType.getTraceClass());
        trace.setDataSet(dataSet);

        // Create Chart with Trace
        Chart chart = new Chart();
        chart.getAxisX().setTitle("X");
        chart.getAxisY().setTitle("Y");
        chart.addTrace(trace);

        // Set title from LastDataSetTitle
        chart.getHeader().setTitle("Plot of " + dataSet.getName());

        // Return
        return chart;
    }

    /**
     * Creates a double array of min/max.
     */
    public static DoubleArray minMaxArray(double aMin, double aMax)
    {
        return DoubleArray.fromMinMax(aMin, aMax);
    }

    /**
     * Creates a double array of min/max/count.
     */
    public static DoubleArray minMaxArray(double aMin, double aMax, int aCount)
    {
        return DoubleArray.fromMinMaxCount(aMin, aMax, aCount);
    }

    /**
     * Conveniences.
     */
    public static Chart plot(Object ... theObjects)  { return chart(theObjects); }
    public static DoubleArray doublearray(Object ... theDoubles)  { return doubleArray(theDoubles); }
    public static DataArray dataarray(Object anObj)  { return dataArray(anObj); }
    public static DataSet dataset(Object ... theObjects)  { return dataSet(theObjects); }
    public static DoubleArray minmax(double aMin, double aMax)  { return minMaxArray(aMin, aMax); }
}
