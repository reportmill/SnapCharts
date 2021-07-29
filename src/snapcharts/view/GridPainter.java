package snapcharts.view;

import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snapcharts.model.Axis;
import snapcharts.model.AxisType;
import snapcharts.model.Intervals;

/**
 * This class handles grid painting for DataArea.
 */
public class GridPainter {

    // The DataArea
    private DataArea  _dataArea;

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // Area bounds
    protected double  areaX, areaY;
    protected double  areaW, areaH;
    protected double  areaMaxX, areaMaxY;

    // AxisView
    protected AxisView  axisView;

    // The axis
    protected Axis  axis;

    // Whether using log
    protected boolean  axisIsLog;

    // Whether to show grid
    protected boolean  isShowGrid;

    // Grid Color
    protected Color  gridColor;

    // Grid Stroke
    protected Stroke  gridStroke;

    // TickLine Color
    protected Color  tickLineColor;

    // TickLength
    protected double  tickLength;

    /**
     * Constructor.
     */
    public GridPainter(DataArea aDataArea)
    {
        // Set DataArea, ChartHelper
        _dataArea = aDataArea;
        _chartHelper = aDataArea.getChartHelper();

        // Get Area bounds
        areaW = _dataArea.getWidth();
        areaH = _dataArea.getHeight();
        areaMaxX = areaX + areaW;
        areaMaxY = areaY + areaH;
    }

    /**
     * Updates values for axis.
     */
    protected void updateForAxis(AxisType anAxisType)
    {
        axisView = anAxisType == AxisType.X ? _dataArea.getAxisViewX() : _dataArea.getAxisViewY();
        axis = axisView.getAxis();
        axisIsLog = axis.isLog();
        isShowGrid = axis.isShowGrid();
        gridColor = axis.getGridColor();
        gridStroke = axis.getGridStroke();
        tickLineColor = AxisView.TICK_LINE_COLOR;
        tickLength = axis.getTickLength();
    }

    /**
     * Paints chart axis lines.
     */
    public void paintGridlines(Painter aPntr)
    {
        // Paint AxisX gridlines
        updateForAxis(AxisType.X);
        if (axisView != null);
            paintGridlinesX(aPntr);

        // Paint AxisY gridlines
        updateForAxis(AxisType.Y);
        if (axisView != null & axisView.isVisible())
            paintGridlinesY(aPntr);
    }

    /**
     * Paints chart X axis lines.
     */
    protected void paintGridlinesX(Painter aPntr)
    {
        // Set Grid Stroke
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
        // Set Grid Stroke
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
        double dataXFloor = Math.floor(dataX);
        double incrX = Math.pow(10, dataXFloor);
        double datX = incrX + incrX;
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
        double dataYFloor = Math.floor(dataY);
        double incrY = Math.pow(10, dataYFloor);
        double datY = incrY + incrY;
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
     * Paints X axis grid line and/or tick at given X in display coords
     */
    protected void paintGridlineX(Painter aPntr, double dispX)
    {
        // Paint line
        if (isShowGrid) {
            aPntr.setColor(gridColor);
            aPntr.drawLine(dispX, areaY, dispX, areaMaxY);
        }

        // Paint tick
        aPntr.setColor(tickLineColor);
        aPntr.drawLine(dispX, areaMaxY - tickLength, dispX, areaMaxY);
    }

    /**
     * Paints Y axis grid line and/or tick at given Y in display coords.
     */
    protected void paintGridlineY(Painter aPntr, double dispY)
    {
        // Paint line
        if (isShowGrid) {
            aPntr.setColor(gridColor);
            aPntr.drawLine(areaX, dispY, areaMaxX, dispY);
        }

        // Paint tick
        aPntr.setColor(tickLineColor);
        aPntr.drawLine(areaX, dispY, areaX + tickLength, dispY);
    }
}
