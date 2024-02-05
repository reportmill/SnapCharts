/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Point;
import snap.gfx.Painter;
import snap.view.ViewEvent;
import snap.view.ViewUtils;
import snapcharts.charts.*;
import java.util.Objects;

/**
 * A class to display Chart.Content (via child TraceView(s)).
 */
public class ContentView extends ChartPartView<Content> {

    // The ChartView
    private ChartView  _chartView;

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The TraceViews
    private TraceView[]  _traceViews;

    // Constants
    public static int DEFAULT_REVEAL_TIME = 2000;

    /**
     * Constructor.
     */
    public ContentView(ChartView aChartView)
    {
        _chartView = aChartView;

        // Config
        //setCursor(Cursor.MOVE);
        enableEvents(MousePress, MouseDrag, MouseRelease, Scroll, MouseMove, MouseExit);

        // Add event filter to clear/reset ChartView.TargPoint on MouseDrag/Release
        addEventFilter(e -> processFilterEvent(e), MouseDrag, MouseRelease);
    }

    /**
     * Returns the ChartPart.
     */
    public Content getChartPart()
    {
        return getContent();
    }

    /**
     * Returns the ChartView.
     */
    @Override
    public ChartView getChartView()  { return _chartView; }

    /**
     * Returns the TraceView.
     */
    public TraceView[] getTraceViews()  { return _traceViews; }

    /**
     * Sets the TraceViews.
     */
    protected void setTraceViews(TraceView[] theTraceViews)
    {
        _chartHelper = getChartHelper();

        // Remove old
        if (_traceViews !=null) {
            for (TraceView traceView : _traceViews)
                removeChild(traceView);
        }

        // Set new
        _traceViews = theTraceViews;

        // Add TraceViews as children
        for (TraceView traceView : _traceViews) {
            addChild(traceView);
            traceView.setContentView(this);
        }
    }

    /**
     * Return the ratio of the chart to show horizontally.
     */
    public double getReveal()
    {
        return _chartView!=null ? _chartView.getReveal() : 1;
    }

    /**
     * Sets the reation of the chart to show horizontally.
     */
    public void setReveal(double aValue)
    {
        TraceView[] traceViews = getTraceViews();
        for (TraceView traceView : traceViews)
            traceView.setReveal(aValue);
    }

    /**
     * Returns the time in milliseconds recommended for animation.
     */
    protected int getRevealTime()
    {
        TraceView[] traceViews = getTraceViews();
        int revealTime = 0;
        for (TraceView traceView : traceViews)
            revealTime = Math.max(revealTime, traceView.getRevealTime());
        return revealTime;
    }

    /**
     * Actual method to layout children.
     */
    protected void layoutImpl()
    {
        double viewW = getWidth();
        double viewH = getHeight();
        for (TraceView traceView : getTraceViews())
            traceView.setSize(viewW, viewH);
    }

    /**
     * Override to paint grid.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        // Paint Chart Gridlines
        _chartHelper.paintGridlines(aPntr);

        // Paint Chart Border
        _chartHelper.paintBorder(aPntr);
    }

    /**
     * Override to forward to PanZoom.
     */
    @Override
    protected void paintAbove(Painter aPntr)
    {
        // Paint Tags
        TraceView[] traceViews = getTraceViews();
        for (TraceView traceView : traceViews)
            traceView.paintTraceAbove(aPntr);

        // Paint Chart Markers
        paintMarkers(aPntr);

        // Forward to ChartHelper hook
        _chartHelper.paintAboveForChartPartView(this, aPntr);
    }

    /**
     * Paints Chart Markers (ChartView.MarkerViews).
     */
    private void paintMarkers(Painter aPntr)
    {
        // Get MarkerViews
        MarkerView[] markerViews = getChartView().getMarkerViews();
        if (markerViews.length == 0)
            return;

        // Paint markers
        for (MarkerView markerView : markerViews) {

            // Save GState
            aPntr.save();

            // Get Marker and X/Y axis (if either X/Y is in axis space)
            Marker marker = markerView.getMarker();
            AxisType axisTypeX = marker.getCoordSpaceX().getAxisType();
            AxisType axisTypeY = marker.getCoordSpaceY().getAxisType();

            // If either set of marker coords in Axis space, clip to ContentView bounds
            if (axisTypeX != null || axisTypeY != null) {
                double clipX = axisTypeX != null ? 0 : -getX();
                double clipY = axisTypeY != null ? 0 : -getY();
                double clipW = axisTypeX != null ? getWidth() : _chartView.getWidth();
                double clipH = axisTypeY != null ? getHeight() : _chartView.getHeight();
                aPntr.clipRect(clipX, clipY, clipW, clipH);
            }

            // Translate paint space to marker origin in ContentView coords
            double markerX = markerView.getX() - getX();
            double markerY = markerView.getY() - getY();
            aPntr.translate(markerX, markerY);

            // Paint MarkerView
            ViewUtils.paintAll(markerView, aPntr);

            // Restore graphics state
            aPntr.restore();
        }
    }

    /**
     * Override to forward to ChartHelper.
     */
    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseMove: Set ChartView.TargPoint
        if (anEvent.isMouseMove()) {
            Point point = localToParent(anEvent.getX(), anEvent.getY(), _chartView);
            _chartView.setTargPoint(point);
        }

        // Handle MouseExit: Clear ChartView.TargPoint
        else if (anEvent.isMouseExit())
            _chartView.setTargPoint(null);

        // Forward to ChartHelper
        _chartHelper.processEventForChartPartView(this, anEvent);

        // Handle MouseClick
        if (anEvent.isMouseClick() && anEvent.getClickCount() == 1) {
            TracePoint dataPoint = _chartHelper.getDataPointForViewXY(this, anEvent.getX(), anEvent.getY());
            if (Objects.equals(dataPoint, _chartView.getSelDataPoint()))
                dataPoint = null;
            _chartView.setSelDataPoint(dataPoint);
        }

        // Do normal version
        super.processEvent(anEvent);
    }

    /**
     * Override to clear/reset ChartView.TargPoint on MouseDrag/Release.
     */
    private void processFilterEvent(ViewEvent anEvent)
    {
        // If MouseDrag, clear ChartView.TargPoint
        if (anEvent.isMouseDrag())
            _chartView.setTargPoint(null);

        // If MouseRelease, reset ChartView.TargPoint
        else if (anEvent.isMouseRelease()) {
            Point point = localToParent(anEvent.getX(), anEvent.getY(), _chartView);
            _chartView.setTargPoint(point);
        }
    }

    /**
     * Override to notify ChartHelper.
     */
    @Override
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        _chartHelper.contentViewSizeDidChange();
    }

    /**
     * Override to notify ChartHelper.
     */
    @Override
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return;
        super.setHeight(aValue);
        _chartHelper.contentViewSizeDidChange();
    }

    /**
     * Override to suppress Border.
     */
    @Override
    protected void resetView()
    {
        ChartPart chartPart = getChartPart();
        setFont(chartPart.getFont());
        setFill(chartPart.getFill());
        setEffect(chartPart.getEffect());
    }
}
