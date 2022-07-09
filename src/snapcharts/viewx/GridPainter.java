/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Side;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snapcharts.model.Axis;
import snapcharts.view.AxisView;
import snapcharts.view.ChartHelper;
import snapcharts.view.DataArea;
import snapcharts.view.ContentView;

/**
 * This class handles grid painting for ContentView.
 */
public class GridPainter {

    // The ChartHelper
    protected ChartHelper _chartHelper;

    // The ContentView
    protected ContentView  _contentView;

    // Area bounds
    protected double  areaX, areaY;
    protected double  areaW, areaH;
    protected double  areaMaxX, areaMaxY;

    // AxisView
    protected AxisView axisView;

    // The axis
    protected Axis  axis;

    // Whether using log
    protected boolean  axisIsLog;

    // The Axis line color
    protected Color  axisLineColor;

    // The Axis line stroke
    protected Stroke  axisLineStroke;

    // Whether to show grid
    protected boolean  isShowGrid;

    // Grid Color
    protected Color  gridColor;

    // Grid Stroke
    protected Stroke  gridStroke;

    // Tick Color
    protected Color  tickColor;

    // TickLength
    protected double  tickLength;

    // Axis line X, Y
    protected double  axisLineX, axisLineY;

    // Tick line X, Y
    protected double  tickX, tickY;

    // Tick line MaxX, MaxY
    protected double  tickMaxX, tickMaxY;

    /**
     * Constructor.
     */
    public GridPainter(ChartHelper aChartHelper)
    {
        // Set ContentView, ChartHelper, DataArea
        _chartHelper = aChartHelper;
        _contentView = aChartHelper.getContentView();

        // Get Area bounds
        areaW = _contentView.getWidth();
        areaH = _contentView.getHeight();
        areaMaxX = areaX + areaW;
        areaMaxY = areaY + areaH;
    }

    /**
     * Updates values for axis.
     */
    protected void updateForAxisView(AxisView anAxisView)
    {
        axisView = anAxisView;
        axis = axisView.getAxis();
        axisIsLog = axis.isLog();
        axisLineColor = Axis.DEFAULT_AXIS_LINE_COLOR;
        axisLineStroke = Axis.DEFAULT_AXIS_LINE_STROKE;
        isShowGrid = axis.isShowGrid();
        gridColor = axis.getGridColor();
        gridStroke = axis.getGridStroke();
        tickColor = AxisView.TICK_LINE_COLOR;
        tickLength = axis.getTickLength();

        // Update AxisLineX
        if (axis.getType().isAnyY()) {
            if (axis.getSide() == Side.LEFT) {
                axisLineX = axisView.getMaxX() - _contentView.getX();
                tickX = axisLineX;
                tickMaxX = axisLineX + tickLength;
            }
            else {
                axisLineX = axisView.getX() - _contentView.getX();
                tickX = axisLineX - tickLength;
                tickMaxX = axisLineX;
            }
        }
    }

    /**
     * Paints chart axis lines.
     */
    public void paintGridlines(Painter aPntr, DataArea aDataArea)  { }
}
