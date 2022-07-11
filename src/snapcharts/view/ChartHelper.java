/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Point;
import snap.gfx.Painter;
import snap.util.ArrayUtils;
import snap.util.MathUtils;
import snap.props.PropChange;
import snap.view.View;
import snap.view.ViewEvent;
import snap.view.ViewUtils;
import snapcharts.data.DataSet;
import snapcharts.model.*;
import snapcharts.util.MinMax;
import snapcharts.viewx.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * A class to help customize ChartView for specific TraceType.
 */
public abstract class ChartHelper {

    // The ChartView
    protected ChartView  _chartView;

    // The ContentView
    protected ContentView  _contentView;

    // The Content
    private Content  _content;

    // The AxisTypes
    private AxisType[]  _axisTypes;

    // The AxisViews
    private Map<AxisType,AxisView>  _axisViews = new HashMap<>();

    // The AxisViews array
    private AxisView[]  _axisViewsArray;

    // The AxisView for X Axis
    private AxisViewX  _axisX;

    // The AxisView for Y Axis
    private AxisViewY  _axisY;

    // The AxisView for Z Axis
    private AxisViewZ  _axisZ;

    // The TraceViews
    private TraceView[] _traceViews;

    // A helper class to handle Pan/Zoom
    private ChartHelperPanZoom _panZoomer;

    /**
     * Constructor.
     */
    protected ChartHelper(ChartView aChartView)
    {
        _chartView = aChartView;

        // Init to empty array so views get added in resetView() + resetAxisViews()
        _axisTypes = new AxisType[0];

        // Create PanZoomer
        _panZoomer = new ChartHelperPanZoom(this);
    }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()  { return _chartView; }

    /**
     * Returns the ContentView.
     */
    public ContentView getContentView()
    {
        if (_contentView != null) return _contentView;
        return _contentView = _chartView.getContentView();
    }

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _chartView.getChart(); }

    /**
     * Returns the TraceType.
     */
    public abstract TraceType getTraceType();

    /**
     * Returns the Content.
     */
    public Content getContent()
    {
        // If already set, just return
        if (_content !=null) return _content;

        // Get Content for Traces that are enabled
        return _content = getChart().getContent();
    }

    /**
     * Returns the layout for the chart.
     */
    public ChartViewLayout createLayout()
    {
        return new ChartViewLayout(_chartView);
    }

    /**
     * Returns the AxisTypes.
     */
    public AxisType[] getAxisTypes()
    {
        if (_axisTypes!=null) return _axisTypes;
        return _axisTypes = getAxisTypesImpl();
    }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        return getContent().getAxisTypes();
    }

    /**
     * Returns whether given axis type exists in chart.
     */
    public boolean isAxisType(AxisType anAxisType)
    {
        return ArrayUtils.contains(getAxisTypes(), anAxisType);
    }

    /**
     * Returns the X AxisView.
     */
    public AxisViewX getAxisViewX()
    {
        if (_axisX != null) return _axisX;
        return _axisX = (AxisViewX) getAxisView(AxisType.X);
    }

    /**
     * Returns the Y Axis.
     */
    public AxisViewY getAxisViewY()
    {
        if (_axisY != null) return _axisY;
        return _axisY = (AxisViewY) getAxisView(AxisType.Y);
    }

    /**
     * Returns the Z AxisView.
     */
    public AxisViewZ getAxisViewZ()
    {
        if (_axisZ != null) return _axisZ;
        return _axisZ = (AxisViewZ) getAxisView(AxisType.Z);
    }

    /**
     * Returns the AxisView for given type.
     */
    public AxisView getAxisView(AxisType anAxisType)
    {
        // If already set, just return
        AxisView axisView = _axisViews.get(anAxisType);
        if (axisView!=null)
            return axisView;

        // If not supported, return null
        if (!ArrayUtils.contains(getAxisTypes(), anAxisType))
            return null;

        // Create AxisView, init and add to map
        axisView = createAxisView(anAxisType);
        axisView._chartHelper = this;
        axisView._chartView = _chartView;
        axisView._contentView = _chartView.getContentView();
        axisView.addPropChangeListener(pc -> axisViewDidChange(pc));
        _axisViews.put(anAxisType, axisView);
        _axisViewsArray = null;
        return axisView;
    }

    /**
     * Returns the AxisViews.
     */
    public AxisView[] getAxisViews()
    {
        // If already set, just return
        if (_axisViewsArray != null) return _axisViewsArray;

        // Iterate over types to get cached AxisViews
        AxisType[] types = getAxisTypes();
        AxisView[] views = new AxisView[types.length];
        for (int i=0; i< types.length; i++)
            views[i] = getAxisView(types[i]);
        return _axisViewsArray = views;
    }

    /**
     * Creates an AxisView for given type.
     */
    protected AxisView createAxisView(AxisType anAxisType)
    {
        if (anAxisType == AxisType.X)
            return new AxisViewX();
        if (anAxisType.isAnyY())
            return new AxisViewY(anAxisType);
        if (anAxisType == AxisType.Z)
            return new AxisViewZ();
        throw new RuntimeException("ChartHelper.createAxisView: Unknown type: " + anAxisType);
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxes()
    {
        for (AxisView axisView : getAxisViews())
            axisView.resetAxes();
    }

    /**
     * Resets Axes to original bounds.
     */
    public void resetAxesAnimated()
    {
        // If axes don't need reset, do anim instead
        boolean isAxisOverrideSet = false;
        for (AxisView axisView : getAxisViews())
            isAxisOverrideSet |= axisView.isAxisMinOverrideSet() || axisView.isAxisMaxOverrideSet();
        if (!isAxisOverrideSet) {
            getChartView().animate();
            return;
        }

        for (AxisView axisView : getAxisViews())
            axisView.resetAxesAnimated();
    }

    /**
     * Returns the TraceViews.
     */
    public TraceView[] getTraceViews()
    {
        if (_traceViews !=null) return _traceViews;
        return _traceViews = createTraceViews();
    }

    /**
     * Creates the TraceViews.
     */
    protected abstract TraceView[] createTraceViews();

    /**
     * Returns the TraceView for trace.
     */
    public TraceView getTraceViewForTrace(Trace aTrace)
    {
        TraceView[] traceViews = getTraceViews();
        for (TraceView traceView : traceViews)
            if (traceView.getTrace() == aTrace)
                return traceView;

        // Some charts have only one shared TraceView (Bar, Pie)
        if (traceViews.length > 0)
            return traceViews[0];
        return null;
    }

    /**
     * Returns the first TraceView for axis type.
     */
    public TraceView getTraceViewForFirstAxisY()
    {
        AxisType[] axisTypes = getAxisTypes();
        for (AxisType axisType : axisTypes) {
            if (!axisType.isAnyY()) continue;
            TraceView traceView = getTraceViewForAxisTypeY(axisType);
            if (traceView != null)
                return traceView;
        }
        return null;
    }

    /**
     * Returns the first TraceView for axis type.
     */
    public TraceView getTraceViewForAxisTypeY(AxisType anAxisType)
    {
        for (TraceView traceView : getTraceViews())
            if (traceView.getAxisTypeY() == anAxisType)
                return traceView;
        return null;
    }

    /**
     * Returns the TraceViews for axis type.
     */
    public TraceView[] getTraceViewsForAxisType(AxisType anAxisType, boolean includeDisabled)
    {
        // Get TraceViews
        TraceView[] traceViews = getTraceViews();

        // Handle AxisType X
        if (anAxisType == AxisType.X) {
            if (!includeDisabled)
                traceViews = ArrayUtils.filter(traceViews, da -> !da.isTraceDisabled());
        }

        // Handle AxisType Y
        else if (anAxisType.isAnyY()) {
            Predicate<TraceView> filter = da -> da.getAxisTypeY() == anAxisType && (includeDisabled || !da.isTraceDisabled());
            traceViews = ArrayUtils.filter(traceViews, filter);
        }

        // Return TraceViews
        return traceViews;
    }

    /**
     * Returns the ChartPartView for given ChartPart.
     */
    public ChartPartView getChartPartViewForPart(ChartPart aChartPart)
    {
        // Handle Chart
        if (aChartPart instanceof Chart)
            return _chartView;

        // Handle Header
        if (aChartPart instanceof Header)
            return _chartView.getHeaderView();

        // Handle Axis
        if (aChartPart instanceof Axis) {
            Axis axis = (Axis) aChartPart;
            AxisType axisType = axis.getType();
            return _chartView.getChartHelper().getAxisView(axisType);
        }

        // Handle ContourAxis
        if (aChartPart instanceof ContourAxis)
            return _chartView.getContourAxisView();

        // Handle Legend
        if (aChartPart instanceof Legend)
            return _chartView.getLegendView();

        // Handle Marker
        if (aChartPart instanceof Marker) {
            for (MarkerView markerView : _chartView.getMarkerViews())
                if (markerView.getMarker() == aChartPart)
                    return markerView;
        }

        // Handle Trace
        if (aChartPart instanceof Content || aChartPart instanceof Trace)
            return _chartView.getContentView();

        // Handle unknown
        return null;
    }

    /**
     * Paints chart axis lines.
     */
    public void paintGridlines(Painter aPntr)  { }

    /**
     * Paints chart border.
     */
    public void paintBorder(Painter aPntr)  { }

    /**
     * Creates the axis intervals for active traces.
     */
    protected Intervals createIntervals(AxisView axisView)
    {
        // Handle Category axis special
        boolean isCategoryAxis = axisView.isCategoryAxis();
        if (isCategoryAxis)
            return createIntervalsForCategoryAxis();

        // Get axis min and max
        double min = getAxisMinForIntervalCalc(axisView);
        double max = getAxisMaxForIntervalCalc(axisView);

        // Get whether interval ends should be adjusted
        boolean minFixed = axisView.isAxisMinFixed();
        boolean maxFixed = axisView.isAxisMaxFixed();

        // Get axis length and suggested div length (if axis len is zero, reset to something reasonable)
        double axisLen = axisView.getAxisLen();
        double divLen = axisView.getDivLen();
        if (axisLen <= 0)
            axisLen = divLen * 10;

        // Handle Log: min/max are powers of 10, so we just want simple intervals from min to max by 1
        Axis axis = axisView.getAxis();
        if (axis.isLog())
            return Intervals.getIntervalsSimple(min, max, minFixed, maxFixed);

        // Calculate intervals
        Intervals intervals = Intervals.getIntervalsForMinMaxLen(min, max, axisLen, divLen, minFixed, maxFixed);

        // If user configured GridSpacing is defined (non-zero), change intervals to be based on GridSpacing/GridBase
        double gridSpacing = axis.getGridSpacing();
        if (gridSpacing != 0)
            intervals = Intervals.getIntervalsForSpacingAndBase(intervals, gridSpacing, axis.getGridBase(), minFixed, maxFixed);

        // Return intervals
        return intervals;
    }

    /**
     * Creates the axis intervals for discrete X axis, like bar or indexed value axis.
     */
    protected Intervals createIntervalsForCategoryAxis()
    {
        Content content = getContent();
        int pointCount = content.getPointCount();
        return Intervals.getCategoryIntervals(pointCount);
    }

    /**
     * Returns the axis min.
     */
    public double getAxisMinForIntervalCalc(AxisView axisView)
    {
        // If explicitly set, just return
        if (axisView._minOverride != AxisView.UNSET_DOUBLE)
            return axisView._minOverride;

        // Get axis info
        Axis axis = axisView.getAxis();
        AxisType axisType = axis.getType();
        AxisBound minBound = axis.getMinBound();

        // If Axis.MinBound is user configured use MinValue, Otherwise get from axis StagedData
        double min = axis.getMinValue();
        if (minBound != AxisBound.VALUE)
            min = getStagedDataMinMaxForAxis(axisType).getMinE();

        // If ZeroRequired and min greater than zero, reset min
        if (axis.isZeroRequired() && min > 0)
            min = 0;

        // Return min
        return min;
    }

    /**
     * Returns the axis max.
     */
    public double getAxisMaxForIntervalCalc(AxisView axisView)
    {
        // If explicitly set, just return
        if (axisView._maxOverride != AxisView.UNSET_DOUBLE)
            return axisView._maxOverride;

        // Get axis info
        Axis axis = axisView.getAxis();
        AxisType axisType = axis.getType();
        AxisBound maxBound = axis.getMaxBound();

        // If Axis.MaxBound is user configured use MaxValue, Otherwise get from axis StagedData
        double max = axis.getMaxValue();
        if (maxBound != AxisBound.VALUE)
            max = getStagedDataMinMaxForAxis(axisType).getMaxE();

        // If ZeroRequired and max less than zero, reset max
        if (axis.isZeroRequired() && max < 0)
            max = 0;

        // Return max
        return max;
    }

    /**
     * Returns the MinMax for given axis.
     */
    private MinMax getStagedDataMinMaxForAxis(AxisType anAxisType)
    {
        // Get active TraceViews for given AxisType (if none, just return silly MinMax)
        TraceView[] traceViews = getTraceViewsForAxisType(anAxisType, false);
        if (traceViews.length == 0)
            return new MinMax(0, 5);

        // Get Data channel
        boolean isX = anAxisType == AxisType.X;
        boolean isY = anAxisType.isAnyY();
        boolean isZ = anAxisType == AxisType.Z;
        if (!isX && !isY && !isZ)
            throw new RuntimeException("ChartHelper.getMinMaxForAxis: Unknown axis: " + anAxisType);

        // Get Min/Max
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (TraceView traceView : traceViews) {
            DataSet stagedData = traceView.getStagedData();
            double minVal = isX ? stagedData.getMinX() : isY ? stagedData.getMinY() : stagedData.getMinZ();
            double maxVal = isX ? stagedData.getMaxX() : isY ? stagedData.getMaxY() : stagedData.getMaxZ();
            min = Math.min(min, minVal);
            max = Math.max(max, maxVal);
        }

        // Return MinMax
        return new MinMax(min, max);
    }

    /**
     * Converts from data to view for given axis type.
     */
    public double dataToView(AxisType anAxisType, double dataXY)
    {
        AxisView axisView = getAxisView(anAxisType);
        return dataToView(axisView, dataXY);
    }

    /**
     * Converts from data to view for given axis type.
     */
    public double dataToView(AxisView axisView, double dataXY)
    {
        // Get AxisView and axis min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle X axis
        if (axisView.getAxisType() == AxisType.X) {
            double areaW = axisView.getWidth();
            double dispX = MathUtils.mapValueForRanges(dataXY, dataMin, dataMax, 0, areaW);
            return dispX;
        }

        // Handle Y axis
        double areaH = axisView.getHeight();
        double dispY = MathUtils.mapValueForRanges(dataXY, dataMin, dataMax, areaH, 0);
        return dispY;
    }

    /**
     * Converts a value from view coords to data coords.
     */
    public double viewToData(AxisType anAxisType, double dispXY)
    {
        AxisView axisView = getAxisView(anAxisType);
        return viewToData(axisView, dispXY);
    }

    /**
     * Converts a value from view coords to data coords.
     */
    public double viewToData(AxisView axisView, double dispXY)
    {
        // Get AxisView and axis min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle X axis
        if (axisView.getAxisType() == AxisType.X) {
            double areaW = axisView.getWidth();
            double dataX = MathUtils.mapValueForRanges(dispXY, 0, areaW, dataMin, dataMax);
            return dataX;
        }

        // Handle Y axis
        double areaH = axisView.getHeight();
        double dataY = MathUtils.mapValueForRanges(dispXY, 0, areaH, dataMax, dataMin);
        return dataY;
    }

    /**
     * Returns the data point for given View + X/Y.
     */
    public TracePoint getDataPointForViewXY(View aView, double aX, double aY)
    {
        // Local vars for data point closest to x/y
        TracePoint dataPoint = null;
        double dist = Float.MAX_VALUE;

        // Get View XY in ContentView coords
        ContentView contentView = getContentView();
        Point dispXY = contentView.isAncestor(aView) ? contentView.parentToLocal(aX, aY, aView) : new Point(aX, aY);
        double dispX = dispXY.x;
        double dispY = dispXY.y;

        // If point out of bounds, return null
        if (!contentView.contains(dispX, dispY))
            return null;

        // Iterate over TraceViews to find closest DataPoint
        TraceView[] traceViews = getTraceViews();
        for (TraceView traceView : traceViews) {
            TracePoint dpnt = traceView.getDataPointForLocalXY(dispX, dispY);
            if (dpnt != null) {
                Point dspXY = traceView.getLocalXYForDataPoint(dpnt);
                double dst = Point.getDistance(dispX, dispY, dspXY.x, dspXY.y);
                if (dst < dist) {
                    dataPoint = dpnt;
                    dist = dst;
                }
            }
        }

        // Return DataPoint
        return dataPoint;
    }

    /**
     * Returns the given data point as X/Y in given view coords.
     */
    public Point getViewXYForDataPoint(View aView, TracePoint aDP)
    {
        // Get TraceView for DataPoint
        Trace trace = aDP.getTrace();
        TraceView traceView = getTraceViewForTrace(trace);
        if (traceView == null) return null;

        // Get TraceView X/Y for DataPoint and return point converted to given view coords
        Point dispXY = traceView.getLocalXYForDataPoint(aDP);
        ContentView contentView = getContentView();
        if (contentView.isAncestor(aView))
            dispXY = contentView.localToParent(dispXY.x, dispXY.y, aView);
        return dispXY;
    }

    /**
     * Called after a chart area is installed in chart view.
     */
    public void activate()
    {
        // Enable all traces
        Content content = getContent();
        Trace[] traces = content.getTraces();
        for (Trace trace : traces)
            trace.setDisabled(false);
    }

    /**
     * Called before a chart area is removed from a chart view.
     */
    public void deactivate()  { }

    /**
     * Called when chart is reloaded.
     */
    public void resetView()
    {
        // If AxisTypes have changed, resetAxisViews
        if (!Objects.equals(getAxisTypes(), getContent().getAxisTypes()))
            resetAxisViews();

        // Reset Axes
        for (AxisView axisView : getAxisViews())
            axisView.resetView();

        // Reset TraceViews
        for (TraceView traceView : getTraceViews())
            if (traceView.isTraceEnabled())
                traceView.resetView();
    }

    /**
     * Resets the axis views.
     */
    protected void resetAxisViews()
    {
        // Remove old axis views
        removeAxisViews();

        // Reset cached values
        _axisTypes = null;
        _axisViewsArray = null;
        _axisX = null;
        _axisY = null;

        // Add new axis views
        for (AxisView axisView : getAxisViews()) {
            ViewUtils.addChild(_chartView, axisView);
            axisView.clearIntervals();
        }
    }

    /**
     * Removes AxisViews.
     */
    protected void removeAxisViews()
    {
        // Remove old axis views
        AxisView[] oldAxisViews = _chartView.getChildrenForClass(AxisView.class);
        for (AxisView axisView : oldAxisViews)
            ViewUtils.removeChild(_chartView, axisView);
    }

    /**
     * Resets TraceViews.
     */
    protected void resetTraceViews()
    {
        _traceViews = null;
        TraceView[] traceViews = getTraceViews();
        ContentView contentView = getContentView();
        contentView.setTraceViews(traceViews);
    }

    /**
     * Returns whether view is in ZoomSelectMode.
     */
    public boolean isZoomSelectMode()  { return _panZoomer.isZoomSelectMode(); }

    /**
     * Sets whether view is in ZoomSelectMode.
     */
    public void setZoomSelectMode(boolean aValue)
    {
        if (!getTraceType().isXYType()) return;
        _panZoomer.setZoomSelectMode(aValue);
    }

    /**
     * Called to process event for hook.
     */
    protected void processEventForChartPartView(ChartPartView aView, ViewEvent anEvent)
    {
        if (getTraceType().isXYType())
            _panZoomer.processEventForChartPartView(aView, anEvent);
    }

    /**
     * Override to forward to PanZoom.
     */
    protected void paintAboveForChartPartView(ChartPartView aView, Painter aPntr)
    {
        if (aView instanceof ContentView)
            _panZoomer.paintAbove(aPntr);
    }

    /**
     * Sets X/Y Axis min/max values for mouse drag points.
     */
    public void scaleAxesMinMaxForFactor(double aScale, boolean isAnimated)
    {
        if (getTraceType().isXYType())
            _panZoomer.scaleAxesMinMaxForFactor(aScale, isAnimated);
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        // Handle Content add/remove
        Object src = aPC.getSource();
        String propName = aPC.getPropName();
        if (src instanceof Content && propName == Content.Trace_Prop) {
            resetTraceViews();
            _chartView.resetLater();
        }

        // Forward to TraceViews
        for (TraceView traceView : getTraceViews())
            traceView.chartPartDidChange(aPC);

        // Forward to AxisViews
        for (AxisView axisView : getAxisViews())
            axisView.chartPartDidChange(aPC);
    }

    /**
     * Called when ContentView changes size.
     */
    protected void contentViewSizeDidChange()
    {
        // Forward to TraceViews
        for (TraceView traceView : getTraceViews())
            traceView.contentViewDidChangeSize();
    }

    /**
     * Called when AxisView changes properties.
     */
    protected void axisViewDidChange(PropChange aPC)
    {
        // Forward to TraceViews
        for (TraceView traceView : getTraceViews())
            traceView.axisViewDidChange(aPC);
    }

    /**
     * Creates a ChartHelper for given ChartView.
     */
    public static ChartHelper createChartHelper(ChartView aChartView)
    {
        TraceType traceType = aChartView.getTraceType();

        if (traceType == TraceType.Bar)
            return new BarChartHelper(aChartView);

        if (traceType == TraceType.Pie)
            return new PieChartHelper(aChartView);

        if (traceType == TraceType.Scatter)
            return new XYChartHelper(aChartView);

        if (traceType == TraceType.Contour)
            return new ContourChartHelper(aChartView);

        if (traceType == TraceType.Polar)
            return new PolarChartHelper(aChartView);

        if (traceType == TraceType.PolarContour)
            return new PolarContourChartHelper(aChartView);

        if (traceType == TraceType.Bar3D)
            return new Bar3DChartHelper(aChartView);

        if (traceType == TraceType.Pie3D)
            return new Pie3DChartHelper(aChartView);

        if (traceType == TraceType.Line3D)
            return new Line3DChartHelper(aChartView);

        if (traceType == TraceType.Contour3D)
            return new Contour3DChartHelper(aChartView);

        // Complain
        throw new RuntimeException("ChartHelper.createChartHelper: Unknown type: " + traceType);
    }
}