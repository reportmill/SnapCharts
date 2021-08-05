/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snapcharts.model.Intervals;
import snapcharts.view.*;

/**
 * This class handles grid painting for XY Charts.
 */
public class XYGridPainter extends GridPainter {

    /**
     * Constructor.
     */
    public XYGridPainter(ChartHelper aChartHelper)
    {
        super(aChartHelper);
    }

    /**
     * Paints chart axis lines.
     */
    public void paintGridlines(Painter aPntr, DataArea aDataArea)
    {
        // Paint AxisX gridlines
        AxisView axisViewX = aDataArea.getAxisViewX();
        if (axisViewX != null && axisViewX.getAxis().isShowGrid()) {
            updateForAxisView(axisViewX);
            paintGridlinesX(aPntr);
        }

        // Paint AxisY gridlines
        AxisViewY axisViewY = aDataArea.getAxisViewY();
        if (axisViewY != null && axisViewY.isVisible() && axisViewY.getAxis().isShowGrid()) {
            updateForAxisView(axisViewY);
            paintGridlinesY(aPntr);
        }
    }

    /**
     * Paints chart X axis lines.
     */
    protected void paintGridlinesX(Painter aPntr)
    {
        // Set Grid Stroke
        aPntr.setColor(gridColor);
        aPntr.setStroke(gridStroke);

        // Iterate over intervals and paint lines
        Intervals ivals = axisView.getIntervals();
        for (int i = 0, iMax = ivals.getCount(); i < iMax; i++) {

            // Get interval X and paint gridline
            double dataX = ivals.getInterval(i);
            double dispX = (int) Math.round(_chartHelper.dataToView(axisView, dataX));
            paintGridlineX(aPntr, dispX);

            // If Log, paint log minor grid
            if (axisIsLog)
                paintMinorLogGridlinesX(aPntr, dataX);
        }
    }

    /**
     * Paints chart Y axis lines.
     */
    protected void paintGridlinesY(Painter aPntr)
    {
        // Set Grid Color, Stroke
        aPntr.setColor(gridColor);
        aPntr.setStroke(gridStroke);

        // Iterate over intervals and paint lines
        Intervals ivals = axisView.getIntervals();
        for (int i=0, iMax=ivals.getCount(); i<iMax; i++) {

            // Get interval X and paint gridline
            double dataY = ivals.getInterval(i);
            double dispX = (int) Math.round(_chartHelper.dataToView(axisView, dataY));
            paintGridlineY(aPntr, dispX);

            // If Log, paint log minor grid
            if (axisIsLog)
                paintMinorLogGridlinesY(aPntr, dataY);
        }
    }

    /**
     * Paints minor log gridlines for X axis.
     */
    protected void paintMinorLogGridlinesX(Painter aPntr, double dataX)
    {
        // Get start and increment for log ticks
        double dataXFloor = Math.floor(dataX);
        double incrX = Math.pow(10, dataXFloor);
        double datX = incrX + incrX;

        // Iterate over extra 9 log ticks and paint
        for (int i=1; i<10; i++, datX+=incrX) {
            double dataXLog = Math.log10(datX);
            double dispX = (int) Math.round(_chartHelper.dataToView(axisView, dataXLog));
            if (dispX < areaX)
                continue;
            if (dispX > areaMaxX)
                return;
            paintGridlineX(aPntr, dispX);
        }
    }

    /**
     * Paints minor log gridlines for Y axis.
     */
    protected void paintMinorLogGridlinesY(Painter aPntr, double dataY)
    {
        // Get start and increment for log ticks
        double dataYFloor = Math.floor(dataY);
        double incrY = Math.pow(10, dataYFloor);
        double datY = incrY + incrY;

        // Iterate over extra 9 log ticks and paint
        for (int i=1; i<10; i++, datY+=incrY) {
            double datYLog = Math.log10(datY);
            double dispY = (int) Math.round(_chartHelper.dataToView(axisView, datYLog));
            if (dispY > areaMaxY)
                continue;
            if (dispY < areaY)
                return;
            paintGridlineY(aPntr, dispY);
        }
    }

    /**
     * Paints X axis grid line at given X in display coords
     */
    private final void paintGridlineX(Painter aPntr, double dispX)
    {
        aPntr.drawLine(dispX, areaY, dispX, areaMaxY);
    }

    /**
     * Paints Y axis grid line at given Y in display coords.
     */
    private final void paintGridlineY(Painter aPntr, double dispY)
    {
        aPntr.drawLine(areaX, dispY, areaMaxX, dispY);
    }
}
