/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snap.util.PropChange;
import snapcharts.model.*;
import snapcharts.view.*;

/**
 * A ChartHelper for common Contour types.
 */
public class ContourChartHelper extends ChartHelper {

    // An object to help with Contours
    protected ContourHelper  _contourHelper;

    /**
     * Constructor.
     */
    public ContourChartHelper(ChartView aChartView)
    {
        super(aChartView);
        _contourHelper = new ContourHelper(this);
    }

    /**
     * Returns the ChartType.
     */
    @Override
    public ChartType getChartType()  { return ChartType.CONTOUR; }

    /**
     * Returns the ContourHelper.
     */
    public ContourHelper getContourHelper()  { return _contourHelper; }

    /**
     * Returns the ContourAxisView.
     */
    public ContourAxisView getContourAxisView()
    {
        return _chartView.getContourAxisView();
    }

    /**
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        // Get Traces
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces();
        int traceCount = traces.length;

        // Iterate over traces and create DataAreas
        DataArea[] dataAreas = new DataArea[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            if (trace.getDataType().hasZ() )
                dataAreas[i] = new ContourDataArea(this, trace);
            else dataAreas[i] = new XYDataArea(this, trace);
        }

        // Return DataAreas
        return dataAreas;
    }

    /**
     * Paints chart axis lines.
     */
    @Override
    public void paintGridlines(Painter aPntr)
    {
        XYChartHelper.paintGridlinesXY(this, aPntr);
    }

    /**
     * Paints chart border.
     */
    @Override
    public void paintBorder(Painter aPntr)
    {
        XYChartHelper.paintBorderXY(this, aPntr);
    }

    /**
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Trace/TraceList change
        Object src = aPC.getSource();
        if (src instanceof Trace || src instanceof TraceList || src instanceof TraceStyle || src instanceof ContourAxis) {
            _contourHelper.resetCachedValues();
        }
    }
}
