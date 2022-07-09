/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Border;
import snap.gfx.Painter;
import snapcharts.model.ChartType;
import snapcharts.model.Trace;
import snapcharts.model.Content;
import snapcharts.view.*;

/**
 * A ChartHelper for common XY ChartTypes: LINE, AREA, SCATTER.
 */
public class XYChartHelper extends ChartHelper {

    // The ChartType
    private ChartType  _chartType;

    /**
     * Constructor.
     */
    public XYChartHelper(ChartView aChartView, ChartType aChartType)
    {
        super(aChartView);
        _chartType = aChartType;
    }

    /**
     * Returns the type.
     */
    @Override
    public ChartType getChartType()  { return _chartType; }

    /**
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        Content content = getContent();
        Trace[] traces = content.getTraces();
        int traceCount = traces.length;

        DataArea[] dataAreas = new DataArea[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            dataAreas[i] = new XYDataArea(this, trace);
        }

        return dataAreas;
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
        DataArea dataArea = aChartHelper.getDataAreaForFirstAxisY(); if (dataArea == null) return;
        XYGridPainter gridPainter = new XYGridPainter(aChartHelper);
        gridPainter.paintGridlines(aPntr, dataArea);
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
