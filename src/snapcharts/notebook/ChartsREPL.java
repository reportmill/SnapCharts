/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snapcharts.data.*;
import snapcharts.model.Chart;
import snapcharts.model.Trace;
import snapcharts.model.TraceType;
import snapcharts.modelx.Contour3DTrace;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

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
        if (dataType == DataType.XYZ && dataArrays[0].length() != dataArrays[2].length())
            dataType = DataType.XYZZ;

        // Create/config DataSet
        DataSet dataSet = dataType == DataType.XYZZ ? new DataSetXYZZ() : new DataSetImpl();
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

        // Set title from DataSet.Name (or type)
        String dataSetName = dataSet.getName();
        if (dataSetName == null) dataSetName = dataSet.getDataType() + " Data";
        chart.getHeader().setTitle("Chart of " + dataSetName);

        // Return
        return chart;
    }

    /**
     * Creates and returns a Plot3D.
     */
    public static Chart chart3D(Object ... theObjects)
    {
        // Get DataSet from theObjects
        DataSet dataSet = dataSet(theObjects);

        // Create Trace for DataSet
        Trace trace = new Contour3DTrace();
        trace.setDataSet(dataSet);

        // Create Chart with Trace
        Chart chart = new Chart();
        chart.getAxisX().setTitle("X");
        chart.getAxisY().setTitle("Y");
        chart.getAxisZ().setTitle("Z");
        chart.addTrace(trace);

        // Set title from DataSet.Name (or type)
        String dataSetName = dataSet.getName();
        if (dataSetName == null) dataSetName = dataSet.getDataType() + " Data";
        chart.getHeader().setTitle("Chart of " + dataSetName);

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
     * Maps XY to Z.
     */
    public static DoubleArray mapXY(double[] x, double[] y, DoubleBinaryOperator mapper)
    {
        // Get Z double array
        double[] z = new double[x.length * y.length];

        // Iterate over X/Y and generate Z
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < y.length; j++) {
                z[i * y.length + j] = mapper.applyAsDouble(x[i], y[j]);
            }
        }

        // Return new double Array
        return new DoubleArray(z);
    }

    /**
     * Maps XY to Z.
     */
    public static DoubleArray mapXY(DoubleArray aX, DoubleArray aY, DoubleBinaryOperator mapper)
    {
        return mapXY(aX.doubleArray(), aY.doubleArray(), mapper);
    }

    /**
     * Conveniences.
     */
    public static Chart plot(Object ... theObjects)  { return chart(theObjects); }
    public static Chart plot3D(Object ... theObjects)  { return chart3D(theObjects); }
}
