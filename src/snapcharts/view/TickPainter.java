/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Side;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snapcharts.model.Axis;
import snapcharts.model.AxisType;
import snapcharts.model.Intervals;

/**
 * This class paints axis ticks for an AxisView.
 */
public class TickPainter {

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The AxisView
    protected AxisView<?>  axisView;

    // Area bounds
    protected double  areaX, areaY;
    protected double  areaW, areaH;
    protected double  areaMaxX, areaMaxY;

    // The axis
    protected Axis axis;

    // The AxisType
    protected AxisType  axisType;

    // The Side that the axis is on
    protected Side  axisSide;

    // Whether using log
    protected boolean  axisIsLog;

    // The Axis line color
    protected Color axisLineColor;

    // The Axis line stroke
    protected Stroke axisLineStroke;

    // Tick Color
    protected Color  tickColor;

    // Tick Stroke
    protected Stroke  tickStroke;

    // TickLength
    protected double  tickLength;

    // Axis line X, Y
    protected double  axisLineX, axisLineY;

    // Tick line X, Y
    protected double  tickX, tickY;

    // Tick line MaxX, MaxY
    protected double  tickMaxX, tickMaxY;

    // Constants
    private static final Stroke DEFAULT_TICK_STROKE = Stroke.Stroke1;
    private static final Color DEFAULT_TICK_COLOR = Color.GRAY;

    /**
     * Constructor.
     */
    public TickPainter(ChartHelper aChartHelper)
    {
        _chartHelper = aChartHelper;
    }

    /**
     * Updates tick painting properties for given AxisView.
     */
    protected void updatePaintPropsForAxisView(AxisView<?> anAxisView)
    {
        // Set AxisView
        axisView = anAxisView;

        // Get Area bounds
        areaW = axisView.getWidth();
        areaH = axisView.getHeight();
        areaMaxX = areaX + areaW;
        areaMaxY = areaY + areaH;

        // Get Axis info
        axis = axisView.getAxis();
        axisType = anAxisView.getAxisType();
        axisIsLog = axis.isLog();
        axisSide = axis.getSide();

        // Get axis line paint info
        axisLineColor = axis.getLineColor();
        axisLineStroke = axis.getLineStroke();

        // Get Tick paint info
        tickColor = DEFAULT_TICK_COLOR;
        tickStroke = DEFAULT_TICK_STROKE;
        tickLength = axis.getTickLength();

        // Update Axis line coords and tick coords for X Axis
        if (axisType == AxisType.X) {
            if (axisSide == Side.TOP) {
                axisLineY = areaH;
                tickY = axisLineY;
                tickMaxY = axisLineY + tickLength;
            }
            else {
                axisLineY = 0;
                tickY = axisLineY - tickLength;
                tickMaxY = axisLineY;
            }
        }

        // Update Axis line coords and tick coords for X Axis
        else if (axisType.isAnyY()) {
            if (axis.getSide() == Side.LEFT) {
                axisLineX = areaMaxX;
                tickX = axisLineX;
                tickMaxX = axisLineX + tickLength;
            }
            else {
                axisLineX = areaX;
                tickX = axisLineX - tickLength;
                tickMaxX = axisLineX;
            }
        }
    }

    /**
     * Paint ticks for given axis.
     */
    public void paintAxisLineAndTicks(Painter aPntr, AxisView<?> anAxisView)
    {
        // Update paint properties for given AxisView
        updatePaintPropsForAxisView(anAxisView);

        // If X Axis, paint X
        if (axisType == AxisType.X)
            paintAxisLineAndTicksX(aPntr);

            // If Y Axis, paint Y
        else paintAxisLineAndTicksY(aPntr);
    }

    /**
     * Paints chart X axis line and ticks.
     */
    protected void paintAxisLineAndTicksX(Painter aPntr)
    {
        // Paint Axis line
        aPntr.setColor(axisLineColor);
        aPntr.setStroke(axisLineStroke);
        aPntr.drawLine(areaX, axisLineY, areaMaxX, axisLineY);

        // Set Tick Color, Stroke
        aPntr.setColor(tickColor);
        aPntr.setStroke(tickStroke);

        // Iterate over intervals and paint lines
        Intervals ivals = axisView.getIntervals();
        for (int i = 0, iMax = ivals.getCount(); i < iMax; i++) {

            // Get interval in data coords, convert to display, and paint tick
            double dataX = ivals.getInterval(i);
            double dispX = (int) Math.round(_chartHelper.dataToView(axisView, dataX));
            paintTickX(aPntr, dispX);

            // If Log, paint log minor grid
            if (axisIsLog)
                paintMinorLogTicksX(aPntr, dataX);
        }
    }

    /**
     * Paints chart Y axis line and ticks.
     */
    protected void paintAxisLineAndTicksY(Painter aPntr)
    {
        // Paint Axis line
        aPntr.setColor(axisLineColor);
        aPntr.setStroke(axisLineStroke);
        aPntr.drawLine(axisLineX, areaY, axisLineX, areaMaxY);

        // Set Tick Color, Stroke
        aPntr.setColor(tickColor);
        aPntr.setStroke(tickStroke);

        // Iterate over intervals and paint ticks
        Intervals ivals = axisView.getIntervals();
        for (int i=0, iMax=ivals.getCount(); i<iMax; i++) {

            // Get interval in data coords, convert to display, and paint tick
            double dataY = ivals.getInterval(i);
            double dispY = (int) Math.round(_chartHelper.dataToView(axisView, dataY));
            paintTickY(aPntr, dispY);

            // If Log, paint log minor grid
            if (axisIsLog)
                paintMinorLogTicksY(aPntr, dataY);
        }
    }

    /**
     * Paints minor log ticks for X axis.
     */
    protected void paintMinorLogTicksX(Painter aPntr, double dataX)
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
            paintTickX(aPntr, dispX);
        }
    }

    /**
     * Paints minor log ticks for Y axis.
     */
    protected void paintMinorLogTicksY(Painter aPntr, double dataY)
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
            paintTickY(aPntr, dispY);
        }
    }

    /**
     * Paints X axis tick line at given X in display coords
     */
    private void paintTickX(Painter aPntr, double dispX)
    {
        aPntr.drawLine(dispX, tickY, dispX, tickMaxY);
    }

    /**
     * Paints Y axis tick line at given Y in display coords.
     */
    private void paintTickY(Painter aPntr, double dispY)
    {
        aPntr.drawLine(tickX, dispY, tickMaxX, dispY);
    }
}
