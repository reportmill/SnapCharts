/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Rect;
import snap.gfx.Painter;
import snap.util.MathUtils;
import snap.util.PropChange;
import snapcharts.data.*;
import snapcharts.model.*;
import snapcharts.util.MinMax;
import snapcharts.view.*;

/**
 * A ChartHelper for Polar charts.
 */
public class PolarChartHelper extends ChartHelper {

    // The rect around the polar grid
    private Rect  _polarBounds;

    // The MinMax for Chart Radius data
    private MinMax  _radiusMinMax;

    /**
     * Constructor.
     */
    public PolarChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.POLAR; }

    /**
     * Override to return ChartViewLayoutPolar.
     */
    @Override
    public ChartViewLayout createLayout()
    {
        return new PolarChartViewLayout(_chartView);
    }

    /**
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces();
        int traceCount = traces.length;

        DataArea[] dataAreas = new DataArea[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            dataAreas[i] = new PolarDataArea(this, trace);
        }

        return dataAreas;
    }

    /**
     * Returns the polar bounds.
     */
    public Rect getPolarBounds()
    {
        // If already set, just return
        if (_polarBounds!=null) return _polarBounds;

        // Calc polar rect
        DataView dataView = getDataView();
        double viewW = dataView.getWidth();
        double viewH = dataView.getHeight();
        double areaX = 0;
        double areaY = 0;
        double areaW = viewW;
        double areaH = viewH;
        if (areaW < areaH) {
            areaH = areaW;
            areaY = areaY + Math.round((viewH - areaH)/2);
        }
        else {
            areaW = areaH;
            areaX = areaX + Math.round((viewW - areaW)/2);
        }

        // Set/return
        return _polarBounds = new Rect(areaX, areaY, areaW, areaH);
    }

    /**
     * Paints chart axis lines.
     */
    @Override
    public void paintGridlines(Painter aPntr)
    {
        DataArea dataArea = getDataAreaForFirstAxisY();
        PolarGridPainter gridPainter = new PolarGridPainter(this);
        gridPainter.paintGridlines(aPntr, dataArea);
    }

    /**
     * Override to handle Polar special.
     */
    protected Intervals createIntervals(AxisView axisView)
    {
        // If Y Axis, substitute X axis intervals
        if (axisView.getAxisType().isAnyY())
            return getAxisViewX().getIntervals();

        // Do normal version
        return super.createIntervals(axisView);
    }

    /**
     * Override for polar.
     */
    public double getAxisMinForIntervalCalc(AxisView axisView)
    {
        // If explicitly set, just return
        if (axisView.isAxisMinOverrideSet())
            return axisView.getAxisMinOverride();

        // Return Min for radius
        return getMinMaxForRadius().getMinE();
    }

    /**
     * Returns the axis max.
     */
    public double getAxisMaxForIntervalCalc(AxisView axisView)
    {
        // If explicitly set, just return
        if (axisView.isAxisMaxOverrideSet())
            return axisView.getAxisMaxOverride();

        // Return Min for radius
        return getMinMaxForRadius().getMaxE();
    }

    /**
     * Converts a polar theta/radius point to coord on axis in display coords.
     */
    public double polarDataToView(AxisType anAxisType, double aTheta, double aRadius)
    {
        AxisView axisView = getAxisView(anAxisType);
        return polarDataToView(axisView, aTheta, aRadius);
    }

    /**
     * Converts a polar theta/radius point to coord on axis in display coords.
     */
    public double polarDataToView(AxisView axisView, double aTheta, double aRadius)
    {
        // Convert from polar to XY
        double dataXY = polarDataToXY(axisView, aTheta, aRadius);

        // Do normal XY data to view conversion
        return dataToView(axisView, dataXY);
    }

    /**
     * Converts a polar theta/radius point to X/Y in data coords.
     */
    private final double polarDataToXY(AxisView axisView, double aTheta, double aRadius)
    {
        // Get data min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double radius = aRadius - dataMin;

        // Handle X axis
        if (axisView.getAxisType() == AxisType.X)
            return dataMin + Math.cos(aTheta) * radius;

        // Handle Y axis
        return dataMin + Math.sin(aTheta) * radius;
    }

    /**
     * Override to handle Polar special.
     */
    public double dataToView(AxisView axisView, double dataXY)
    {
        // Get PolarBounds
        Rect polarBounds = getPolarBounds();

        // Get data min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle X axis
        if (axisView.getAxisType() == AxisType.X) {
            double areaX = polarBounds.getMidX();
            double areaW = polarBounds.width / 2;
            double dispX = MathUtils.mapValueForRanges(dataXY, dataMin, dataMax, areaX, areaX + areaW);
            return dispX;
        }

        // Handle Y axis
        double areaY = polarBounds.y;
        double areaH = polarBounds.height / 2;
        double dispY = MathUtils.mapValueForRanges(dataXY, dataMin, dataMax, areaY + areaH, areaY);
        return dispY;
    }

    /**
     * Override to handle Polar special.
     */
    public double viewToData(AxisView axisView, double dispXY)
    {
        // Get PolarBounds
        Rect polarBounds = getPolarBounds();

        // Get data min/max (data coords)
        Intervals intervals = axisView.getIntervals();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();

        // Handle X axis
        if (axisView.getAxisType() == AxisType.X) {
            double areaX = polarBounds.getMidX();
            double areaW = polarBounds.width / 2;
            double dataX = MathUtils.mapValueForRanges(dispXY, areaX, areaX + areaW, dataMin, dataMax);
            return dataX;
        }

        // Handle Y axis
        double areaY = polarBounds.y;
        double areaH = polarBounds.height / 2;
        double dataY = MathUtils.mapValueForRanges(dispXY, areaY, areaY + areaH, dataMax, dataMin);
        return dataY;
    }

    /**
     * Returns the MinMax for radius.
     */
    private MinMax getMinMaxForRadius()
    {
        // If already set, just return
        if (_radiusMinMax != null) return _radiusMinMax;

        // Get TraceList (if empty, just return silly range)
        TraceList traceList = getTraceList();
        if (traceList.getTraceCount()==0 || traceList.getPointCount()==0)
            return new MinMax(0, 5);

        // Get Radius MinMax for all traces
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        Trace[] traces = traceList.getEnabledTraces();
        for (Trace trace : traces) {
            DataSet dataSet = trace.getProcessedData();
            NumberArray dataArrayR = dataSet.getNumberArrayForChannel(DataChan.R);
            MinMax minMax = dataArrayR != null ? dataArrayR.getMinMax() : new MinMax(0, 0);
            min = Math.min(min, minMax.getMin());
            max = Math.max(max, minMax.getMax());
        }

        // Check Axis.ZeroRequired
        AxisView axisViewX = getAxisViewX();
        Axis axisX = axisViewX.getAxis();
        if (axisX.isZeroRequired()) {
            if (min > 0)
                min = 0;
            else if (max < 0)
                max = 0;
        }

        // Set/return min/max
        return _radiusMinMax = new MinMax(min, max);
    }

    /**
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Trace/TraceList change
        Object src = aPC.getSource();
        if (src instanceof Trace || src instanceof TraceList || src instanceof Axis) {
            _radiusMinMax = null;
        }
    }

    /**
     * Override to clear PolarBounds.
     */
    @Override
    protected void dataViewSizeDidChange()
    {
        // Do normal version
        super.dataViewSizeDidChange();

        // Clear PolarBounds
        _polarBounds = null;
    }
}
