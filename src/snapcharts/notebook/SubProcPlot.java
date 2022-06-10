/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.util.KeyChain;
import snapcharts.data.DataSet;
import snapcharts.model.Chart;
import snapcharts.model.ChartType;
import snapcharts.model.Trace;

/**
 * This class processes plot requests.
 */
public class SubProcPlot extends SubProc {

    // The Data Processor
    protected SubProcData  _dataProcessor;

    /**
     * Constructor.
     */
    public SubProcPlot(Processor aProcessor)
    {
        super(aProcessor);
        _dataProcessor = aProcessor._dataProcessor;
    }

    /**
     * Handles processing functions supported by this class.
     */
    public Object getValueFunctionCall(Object aRoot, Object anObj, String functionName, KeyChain argListKC)
    {
        switch (functionName) {

            // Handle Plot
            case "plot": return plot(anObj, argListKC);

            // Handle Plot3D
            case "plot3d": return plot3D(anObj, argListKC);
        }

        return null;
    }

    /**
     * Creates and returns a Plot.
     */
    public Chart plot(Object anObj, KeyChain aKeyChain)
    {
        // Get DataSet
        DataSet dataSet = _dataProcessor.dataSetOrSample(anObj, aKeyChain);

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

        // Set title from LastDataSetTitle
        chart.getHeader().setTitle("Plot of " + dataSet.getName());

        // Return
        return chart;
    }

    /**
     * Creates and returns a Plot3D.
     */
    public Chart plot3D(Object anObj, KeyChain aKeyChain)
    {
        // Get DataSet
        DataSet dataSet = _dataProcessor.dataSetOrSample3D(anObj, aKeyChain);

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

        // Set title from LastDataSetTitle
        chart.getHeader().setTitle("Plot of " + dataSet.getName());

        // Return
        return chart;
    }
}
