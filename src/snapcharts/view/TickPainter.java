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

    // Minor tick count
    protected int  minorTickCount;

    // Axis line X, Y
    protected double  axisLineX, axisLineY;

    // Tick line X, Y
    protected double  tickX, tickY;

    // Tick line MaxX, MaxY
    protected double  tickMaxX, tickMaxY;

    // Minor tick line X, Y
    protected double  tickMinorX, tickMinorY;

    // Minor tick line MaxX, MaxY
    protected double  tickMinorMaxX, tickMinorMaxY;

    // Polar stuff
    protected boolean isPolar;
    protected double polarShift;

    // Constants
    private static final Stroke DEFAULT_TICK_STROKE = Stroke.Stroke1;
    private static final Color DEFAULT_TICK_COLOR = Axis.DEFAULT_LINE_COLOR;

    /**
     * Constructor.
     */
    public TickPainter(ChartHelper aChartHelper)
    {
        _chartHelper = aChartHelper;

        // Handle polar stuff
        if (isPolar = _chartHelper.getChartType().isPolarType())
            polarShift = _chartHelper.getDataView().getWidth() / 2;
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
        minorTickCount = axis.getMinorTickCount();

        // TickPos support
        Axis.TickPos tickPos = axis.getTickPos();
        double tickMult = tickPos == Axis.TickPos.Outside ? 1 : tickPos == Axis.TickPos.Across ? .5 : 0;
        double tickShift = tickLength * tickMult;

        // Tick Minor
        double tickMinorLength = Math.round(tickLength / 2);
        double tickMinorShift = tickMinorLength * tickMult;

        // Update Axis line coords and tick coords for X Axis
        if (axisType == AxisType.X) {

            // Handle Top Axis
            if (axisSide == Side.TOP) {
                axisLineY = areaH;
                tickY = axisLineY - tickShift;
                tickMaxY = axisLineY + tickLength - tickShift;
                tickMinorY = axisLineY - tickMinorShift;
                tickMinorMaxY = axisLineY + tickMinorLength - tickMinorShift;
            }

            // Handle Bottom Axis
            else {
                axisLineY = 0;
                tickY = axisLineY - tickLength + tickShift;
                tickMaxY = axisLineY + tickShift;
                tickMinorY = axisLineY - tickMinorLength + tickMinorShift;
                tickMinorMaxY = axisLineY + tickMinorShift;
            }
        }

        // Update Axis line coords and tick coords for X Axis
        else if (axisType.isAnyY()) {

            // Handle Left Axis
            if (axis.getSide() == Side.LEFT) {
                axisLineX = areaMaxX;
                tickX = axisLineX - tickShift;
                tickMaxX = axisLineX + tickLength - tickShift;
                tickMinorX = axisLineX - tickMinorShift;
                tickMinorMaxX = axisLineX + tickMinorLength - tickMinorShift;
            }

            // Handle Right Axis
            else {
                axisLineX = areaX;
                tickX = axisLineX - tickLength + tickShift;
                tickMaxX = axisLineX + tickShift;
                tickMinorX = axisLineX - tickMinorLength + tickMinorShift;
                tickMinorMaxX = axisLineX + tickMinorShift;
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

        // If TickPos == Off, just return
        if (axis.getTickPos() == Axis.TickPos.Off)
            return;

        // Set Tick Color, Stroke
        aPntr.setColor(tickColor);
        aPntr.setStroke(tickStroke);

        // Iterate over intervals and paint lines
        Intervals intervals = axisView.getIntervals();
        for (int i = 0, iMax = intervals.getCount(); i < iMax; i++) {

            // Get interval in data coords, convert to display
            double dataX = intervals.getInterval(i);
            double dispX = (int) Math.round(_chartHelper.dataToView(axisView, dataX));
            if (isPolar)
                dispX -= polarShift;

            // Paint tick
            boolean isFullInterval = intervals.isFullInterval(i);
            if (isFullInterval)
                paintTickX(aPntr, dispX);

            // If Log, paint log minor grid
            if (axisIsLog)
                paintMinorLogTicksX(aPntr, dataX);

            // If MinorTickCount, paint minor ticks
            else if (minorTickCount > 0) {
                double datX = i != 0 ? dataX : intervals.getFullInterval(0);
                paintMinorTicksX(aPntr, datX);
            }
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

        // If TickPos == Off, just return
        if (axis.getTickPos() == Axis.TickPos.Off)
            return;

        // Set Tick Color, Stroke
        aPntr.setColor(tickColor);
        aPntr.setStroke(tickStroke);

        // Iterate over intervals and paint ticks
        Intervals intervals = axisView.getIntervals();
        for (int i = 0, iMax = intervals.getCount(); i < iMax; i++) {

            // Get interval in data coords, convert to display
            double dataY = intervals.getInterval(i);
            double dispY = (int) Math.round(_chartHelper.dataToView(axisView, dataY));

            // Paint tick
            boolean isFullInterval = intervals.isFullInterval(i);
            if (isFullInterval)
                paintTickY(aPntr, dispY);

            // If Log, paint log minor grid
            if (axisIsLog)
                paintMinorLogTicksY(aPntr, dataY);

            // If MinorTickCount, paint minor ticks
            else if (minorTickCount > 0) {
                double datY = i != 0 ? dataY : intervals.getFullInterval(0);
                paintMinorTicksY(aPntr, datY);
            }
        }
    }

    /**
     * Paints minor ticks for X axis.
     */
    protected void paintMinorTicksX(Painter aPntr, double dataX)
    {
        // Get start and increment for minor ticks
        Intervals intervals = axisView.getIntervals();
        double delta = intervals.getDelta();
        double incrX = delta / (minorTickCount + 1);
        double datX = dataX + incrX;

        // Iterate to MinorTickCount and paint
        for (int i = 0; i < minorTickCount; i++, datX += incrX) {

            // Get data value in display coords
            double dispX = (int) Math.round(_chartHelper.dataToView(axisView, datX));

            // Paint tick (skip/return if outside bounds)
            if (dispX < areaX)
                continue;
            if (dispX > areaMaxX)
                return;
            aPntr.drawLine(dispX, tickMinorY, dispX, tickMinorMaxY);
        }
    }

    /**
     * Paints minor ticks for Y axis.
     */
    protected void paintMinorTicksY(Painter aPntr, double dataY)
    {
        // Get start and increment for minor ticks
        Intervals intervals = axisView.getIntervals();
        double delta = intervals.getDelta();
        double incrY = delta / (minorTickCount + 1);
        double datY = dataY + incrY;

        // Iterate to MinorTickCount and paint
        for (int i = 0; i < minorTickCount; i++, datY += incrY) {

            // Get data value in display coords
            double dispY = (int) Math.round(_chartHelper.dataToView(axisView, datY));

            // Paint tick (skip/return if outside bounds)
            if (dispY > areaMaxY)
                continue;
            if (dispY < areaY)
                return;
            aPntr.drawLine(tickMinorX, dispY, tickMinorMaxX, dispY);
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
        for (int i = 1; i < 10; i++, datX += incrX) {

            // Get data val as log, convert to display coords (if polar, shift)
            double dataXLog = Math.log10(datX);
            double dispX = (int) Math.round(_chartHelper.dataToView(axisView, dataXLog));
            if (isPolar)
                dispX -= polarShift;

            // Paint tick (skip/return if outside bounds)
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
        for (int i = 1; i < 10; i++, datY += incrY) {

            // Get data val as log, convert to display coords
            double datYLog = Math.log10(datY);
            double dispY = (int) Math.round(_chartHelper.dataToView(axisView, datYLog));

            // Paint tick (skip/return if outside bounds)
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
