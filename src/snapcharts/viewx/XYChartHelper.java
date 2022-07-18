/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Border;
import snap.gfx.Painter;
import snapcharts.model.Trace;
import snapcharts.model.Content;
import snapcharts.model.TraceType;
import snapcharts.view.*;

/**
 * A ChartHelper for TraceType Scatter.
 */
public class XYChartHelper extends ChartHelper {

    /**
     * Constructor.
     */
    public XYChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the trace type.
     */
    @Override
    public TraceType getTraceType()  { return TraceType.Scatter; }

    /**
     * Creates the TraceViews.
     */
    @Override
    protected TraceView[] createTraceViews()
    {
        Content content = getContent();
        Trace[] traces = content.getTraces();
        int traceCount = traces.length;

        TraceView[] traceViews = new TraceView[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            traceViews[i] = new ScatterTraceView(this, trace);
        }

        return traceViews;
    }

    /**
     * Paints chart axis lines.
     */
    @Override
    public void paintGridlines(Painter aPntr)
    {
        paintGridlinesXY(this, aPntr);
    }

    /**
     * Paints chart border.
     */
    @Override
    public void paintBorder(Painter aPntr)
    {
        paintBorderXY(this, aPntr);
    }

    /**
     * Paints chart axis lines.
     */
    public static void paintGridlinesXY(ChartHelper aChartHelper, Painter aPntr)
    {
        TraceView traceView = aChartHelper.getTraceViewForFirstAxisY(); if (traceView == null) return;
        XYGridPainter gridPainter = new XYGridPainter(aChartHelper);
        gridPainter.paintGridlines(aPntr, traceView);
    }

    /**
     * Paints chart border.
     */
    public static void paintBorderXY(ChartHelper aChartHelper, Painter aPntr)
    {
        // Get border
        Content content = aChartHelper.getContent();
        Border border = content.getBorder();
        if (border == null)
            return;

        // Get view area
        ContentView contentView = aChartHelper.getContentView();
        double areaX = 0;
        double areaY = 0;
        double areaW = contentView.getWidth();
        double areaH = contentView.getHeight();

        // Disable antialiasing to get crisp lines
        aPntr.setAntialiasing(false);

        // Paint Border
        aPntr.setColor(border.getColor());
        aPntr.setStroke(border.getStroke());
        aPntr.drawRect(areaX, areaY, areaW, areaH);

        // Enable antialiasing
        aPntr.setAntialiasing(true);
    }
}
