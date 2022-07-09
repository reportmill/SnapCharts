/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snap.props.PropChange;
import snapcharts.model.*;
import snapcharts.modelx.ContourStyle;
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

        // This is bogus
        Trace[] traces = aChartView.getContent().getTraces();
        for (Trace trace : traces) {
            TraceStyle traceStyle = trace.getTraceStyle();
            if (traceStyle instanceof ContourStyle) {
                _contourHelper = new ContourHelper(this, (ContourStyle) traceStyle);
                break;
            }
        }
        if (_contourHelper == null) {
            System.err.println("ContourChartHelper.init: No Contour Trace!");
        }
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
     * Creates the TraceViews.
     */
    @Override
    protected TraceView[] createTraceViews()
    {
        // Get Traces
        Content content = getContent();
        Trace[] traces = content.getTraces();
        int traceCount = traces.length;

        // Iterate over traces and create TraceViews
        TraceView[] traceViews = new TraceView[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            if (trace.getDataType().hasZ() )
                traceViews[i] = new ContourTraceView(this, trace);
            else traceViews[i] = new XYTraceView(this, trace);
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
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Trace/Content change
        Object src = aPC.getSource();
        if (src instanceof Trace || src instanceof Content || src instanceof TraceStyle || src instanceof ContourAxis) {
            _contourHelper.resetCachedValues();
        }
    }
}
