/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Painter;
import snapcharts.model.ChartType;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
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
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getDataSets();
        int dsetCount = dataSets.length;

        DataArea[] dataAreas = new DataArea[dsetCount];
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dataSets[i];
            dataAreas[i] = new XYDataArea(this, dset);
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
     * Paints chart border.
     */
    @Override
    public void paintAxesLinesAndTicks(Painter aPntr)
    {
        //paintAxesLinesAndTicksXY(this, aPntr);
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
        DataSetList dataSetList = aChartHelper.getDataSetList();
        Border border = dataSetList.getBorder();
        if (border == null)
            return;

        // Get view area
        DataView dataView = aChartHelper.getDataView();
        double areaX = 0;
        double areaY = 0;
        double areaW = dataView.getWidth();
        double areaH = dataView.getHeight();

        // Disable antialiasing to get crisp lines
        aPntr.setAntialiasing(false);

        // Paint Border
        aPntr.setColor(border.getColor());
        aPntr.setStroke(border.getStroke());
        aPntr.drawRect(areaX, areaY, areaW, areaH);

        // Enable antialiasing
        aPntr.setAntialiasing(true);
    }

    /**
     * Paints chart axis lines.
     */
    public static void paintAxesLinesAndTicksXY(ChartHelper aChartHelper, Painter aPntr)
    {
        // Get view area
        DataView dataView = aChartHelper.getDataView();
        double areaX = 0;
        double areaY = 0;
        double areaW = dataView.getWidth();
        double areaH = dataView.getHeight();

        // Disable antialiasing to get crisp lines
        aPntr.setAntialiasing(false);

        // Paint Axis lines
        Color axisColor = Color.GRAY.darker();
        aPntr.setColor(axisColor);
        aPntr.drawLine(areaX, areaY, areaX, areaY + areaH);
        aPntr.drawLine(areaX, areaY + areaH, areaX + areaW, areaY + areaH);

        // Enable antialiasing
        aPntr.setAntialiasing(true);
    }
}
