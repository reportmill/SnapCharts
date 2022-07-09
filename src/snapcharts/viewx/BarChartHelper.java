/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.TraceView;

/**
 * A ChartHelper for ChartType BAR.
 */
public class BarChartHelper extends ChartHelper {

    /**
     * Constructor.
     */
    public BarChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.BAR; }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        return new AxisType[] { AxisType.X, AxisType.Y };
    }

    /**
     * Creates the TraceViews.
     */
    protected TraceView[] createTraceViews()
    {
        // Get traces
        Content content = getContent();
        Trace[] traces = content.getTraces();
        int traceCount = traces.length;

        // Create TraceView for each Trace
        TraceView[] traceViews = new TraceView[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            traceViews[i] = new BarTraceView(this, trace, i == 0);
        }

        // Return
        return traceViews;
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
     * Override for chart type.
     */
    public void resetView()
    {
        // Make sure all Trace.AxisTypeY are just Y
        Content content = getContent();
        Trace[] traces = content.getTraces(); if (traces.length == 0) return;
        for (Trace trace : traces)
            trace.setAxisTypeY(AxisType.Y);

        // Do normal version
        super.resetView();
    }
}
