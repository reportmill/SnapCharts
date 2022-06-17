/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Point;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.props.PropChange;
import snapcharts.data.*;
import snapcharts.model.*;

/**
 * A view to display the actual contents of a chart.
 */
public abstract class DataArea extends ChartPartView<Trace> {

    // The ChartHelper
    protected ChartHelper  _chartHelper;

    // The DataView that holds this DataArea
    protected DataView  _dataView;

    // The Trace
    private Trace  _trace;

    // The AxisType for Y axis (might get coerced down to Y if chart type doesn't support it)
    private AxisType  _axisTypeY;

    // The Trace.ProcessedData possibly further processed for DataArea/Axes
    private DataSet  _stagedData;

    // The ProcessedData converted to DataArea display coords
    private DataSet  _dispData;

    /**
     * Constructor.
     */
    public DataArea(ChartHelper aChartHelper, Trace aTrace)
    {
        super();
        //setCursor(Cursor.MOVE);

        // Set ivars
        _chartHelper = aChartHelper;
        _trace = aTrace;

        // Get/set AxisTypeY for Trace. If chart type doesn't support it, just use standard Y
        _axisTypeY = _trace.getAxisTypeY();
        if (_axisTypeY != AxisType.Y && !_chartHelper.isAxisType(_axisTypeY))
            _axisTypeY = AxisType.Y;
    }

    /**
     * Returns the ChartPart.
     */
    @Override
    public Trace getChartPart()  { return _trace; }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()  { return _trace; }

    /**
     * Returns whether trace is enabled.
     */
    public boolean isTraceEnabled()  { return _trace.isEnabled(); }

    /**
     * Returns whether trace is disabled.
     */
    public boolean isTraceDisabled()  { return _trace.isDisabled(); }

    /**
     * Returns the trace AxisType.
     */
    public AxisType getAxisTypeY()  { return _axisTypeY; }

    /**
     * Sets the DataView.
     */
    protected void setDataView(DataView aDataView)
    {
        _dataView = aDataView;
    }

    /**
     * Returns the X axis view.
     */
    public AxisViewX getAxisViewX()
    {
        return _chartHelper.getAxisViewX();
    }

    /**
     * Returns the Y axis view.
     */
    public AxisViewY getAxisViewY()
    {
        AxisType axisType = getAxisTypeY();
        return (AxisViewY) _chartHelper.getAxisView(axisType);
    }

    /**
     * Returns the Z axis view.
     */
    public AxisViewZ getAxisViewZ()
    {
        return _chartHelper.getAxisViewZ();
    }

    /**
     * Returns the TraceList of active data sets.
     */
    public TraceList getTraceList()
    {
        return _chartHelper.getTraceList();
    }

    /**
     * Returns the trace color at index.
     */
    public Color getDataColor()
    {
        Trace trace = getTrace();
        return trace.getLineColor();
    }

    /**
     * Returns the trace color at index.
     */
    public Color getColorMapColor(int anIndex)
    {
        Trace trace = getTrace();
        return trace.getColorMapColor(anIndex);
    }

    /**
     * Returns the Symbol shape at index.
     */
    public Symbol getDataSymbol()
    {
        Trace trace = getTrace();
        PointStyle pointStyle = trace.getPointStyle();
        return pointStyle.getSymbol();
    }

    /**
     * Returns the Trace.ProcessedData possibly further processed for DataArea/Axes.
     * Conditions that cause further processing: Stacked, AxisWrap, Axis Log.
     */
    public DataSet getStagedData()
    {
        // If already set, just return
        if (_stagedData != null) return _stagedData;

        // Get Trace and ProcessedData
        Trace trace = getTrace();
        DataSet dataSet = trace.getProcessedData();

        // If Log, use Trace.LogData
        AxisViewX axisViewX = getAxisViewX();
        AxisViewY axisViewY = getAxisViewY();
        Axis axisX = axisViewX != null ? axisViewX.getAxis() : null;
        Axis axisY = axisViewY != null ? axisViewY.getAxis() : null;
        boolean isLogX = axisX != null && axisX.isLog();
        boolean isLogY = axisY != null && axisY.isLog();
        if (isLogX || isLogY)
            dataSet = trace.getLogData(isLogX, isLogY);

        // Handle stacked
        if (trace.isStacked()) {
            DataSet prevStackedData = getPreviousStackedData();
            if (prevStackedData != null)
                dataSet = DataSetUtils.addStackedData(dataSet, prevStackedData);
        }

        // If WrapAxis, wrap DataSet inside DataSetWrapper for wrap range and axis range
        if (axisX != null && axisX.isWrapAxis()) {

            // Get Wrap Min/Max values
            double wrapMin = axisX.getWrapMinMax().getMin();
            double wrapMax = axisX.getWrapMinMax().getMax();

            // Get Axis Min/Max values
            _stagedData = dataSet; // Is this lame? I think it might be
            double axisMin = axisViewX.getAxisMin();
            double axisMax = axisViewX.getAxisMax();
            dataSet = new DataSetWrapper(dataSet, wrapMin, wrapMax, axisMin, axisMax);
        }

        // Set/return
        return _stagedData = dataSet;
    }

    /**
     * Returns the Trace points in display coords for this DataArea (cached).
     */
    public DataSet getDisplayData()
    {
        // If already set, just return
        if (_dispData != null) return _dispData;

        // Get display coords DataSet, set and return
        DataSet displayData = getDisplayDataImpl();
        return _dispData = displayData;
    }

    /**
     * Returns the Trace points in display coords for this DataArea.
     */
    protected DataSet getDisplayDataImpl()
    {
        // Get StagedData
        DataSet stagedData = getStagedData();
        int pointCount = stagedData.getPointCount();
        double[] dispX = new double[pointCount];
        double[] dispY = new double[pointCount];

        // Get ChartHelper and AxisViews
        ChartHelper chartHelper = getChartHelper();
        AxisView axisViewX = getAxisViewX();
        AxisView axisViewY = getAxisViewY();

        // Iterate over data points and convert to display coords
        for (int i = 0; i < pointCount; i++) {
            double dataX = stagedData.getX(i);
            double dataY = stagedData.getY(i);
            dispX[i] = chartHelper.dataToView(axisViewX, dataX);
            dispY[i] = chartHelper.dataToView(axisViewY, dataY);
        }

        // Create DataSet for points and return
        return new DataSetImpl(DataType.XY, dispX, dispY);
    }

    /**
     * Returns the start index for display data.
     */
    public int getDispDataStartIndex()
    {
        // Get DisplayData and PointCount
        DataSet dispData = getDisplayData();
        int pointCount = dispData.getPointCount();

        // Iterate over DispData to find first visible point index
        int startIndex = 0;
        while (startIndex < pointCount && dispData.getX(startIndex) < 0)
            startIndex++;

        // Return start index
        return startIndex;
    }

    /**
     * Returns the start index for display data.
     */
    public int getDispDataEndIndex()
    {
        // Get DisplayData and PointCount
        DataSet dispData = getDisplayData();
        int pointCount = dispData.getPointCount();

        // Iterate over DispData (back-to-front) to find last visible point index
        int endIndex = pointCount - 1;
        double areaMaxX = getWidth();
        while (endIndex > 0 && dispData.getX(endIndex) > areaMaxX)
            endIndex--;

        // Return end index
        return endIndex;
    }

    /**
     * Returns the start index for display data, starting with first point outside display range.
     */
    public int getDispDataStartOutsideIndex()
    {
        int startIndex = getDispDataStartIndex();
        if (startIndex > 0)
            startIndex--;
        return startIndex;
    }

    /**
     * Returns the end index for display data, ending with first point outside display range.
     */
    public int getDispDataEndOutsideIndex()
    {
        int endIndex = getDispDataEndIndex();
        int pointCount = getDisplayData().getPointCount();
        if (endIndex + 1 < pointCount)
            endIndex++;
        return endIndex;
    }

    /**
     * Returns the previous stacked DataArea.
     */
    public DataArea getPreviousStackedDataArea()
    {
        DataArea[] dataAreas = _chartHelper.getDataAreas();
        int index = ArrayUtils.indexOf(dataAreas, this);
        for (int i = index - 1; i >= 0; i--) {
            DataArea prevDataArea = dataAreas[i];
            Trace prevTrace = prevDataArea.getTrace();
            if (prevTrace.isStacked())
                return prevDataArea;
        }
        return null;
    }

    /**
     * Returns the previous stacked DataArea.StagedData.
     */
    public DataSet getPreviousStackedData()
    {
        DataArea prevDataArea = getPreviousStackedDataArea();
        return prevDataArea != null ? prevDataArea.getStagedData() : null;
    }

    /**
     * Returns whether trace is selected.
     */
    public boolean isSelected()
    {
        Trace trace = getTrace();
        TracePoint dataPoint = getChartView().getSelDataPoint();
        return dataPoint != null && dataPoint.getTrace() == trace;
    }

    /**
     * Returns whether trace is targeted.
     */
    public boolean isTargeted()
    {
        Trace trace = getTrace();
        TracePoint dataPoint = getChartView().getTargDataPoint();
        return dataPoint != null && dataPoint.getTrace() == trace;
    }

    /**
     * Returns whether trace is selected or targeted.
     */
    public boolean isSelectedOrTargeted()
    {
        return isSelected() || isTargeted();
    }

    /**
     * Returns selected data point if in Trace.
     */
    public TracePoint getSelDataPoint()
    {
        TracePoint selDataPoint = getChartView().getSelDataPoint();
        if (selDataPoint != null && selDataPoint.getTrace() == getTrace())
            return selDataPoint;
        return null;
    }

    /**
     * Returns targeted data point if in Trace.
     */
    public TracePoint getTargDataPoint()
    {
        TracePoint targDataPoint = getChartView().getTargDataPoint();
        if (targDataPoint != null && targDataPoint.getTrace() == getTrace())
            return targDataPoint;
        return null;
    }

    /**
     * Return the ratio of the portion of chart to paint.
     */
    public double getReveal()  { return _dataView!=null ? _dataView.getReveal() : 1; }

    /**
     * Sets the ratio of the portion of chart to paint.
     */
    public void setReveal(double aValue)  { }

    /**
     * Returns the time in milliseconds recommended for animation.
     */
    protected int getRevealTime()  { return DataView.DEFAULT_REVEAL_TIME; }

    /**
     * Converts a point from data coords to view coords.
     */
    public Point dataToView(double dataX, double dataY)
    {
        double dispX = dataToViewX(dataX);
        double dispY = dataToViewY(dataY);
        return new Point(dispX, dispY);
    }

    /**
     * Converts a X coord from data coords to view coords.
     */
    public double dataToViewX(double dataX)
    {
        return _chartHelper.dataToView(AxisType.X, dataX);
    }

    /**
     * Converts a X coord from data coords to view coords.
     */
    public double dataToViewY(double dataY)
    {
        AxisType axisType = getAxisTypeY();
        return _chartHelper.dataToView(axisType, dataY);
    }

    /**
     * Paints chart axis lines.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get area bounds
        double areaW = getWidth();
        double areaH = getHeight();

        // Clip bounds
        aPntr.save();
        aPntr.clipRect(0, 0, areaW, areaH);

        // Paint chart
        Trace trace = getTrace();
        if (trace.isEnabled() || getParent().getChildCount() == 1)
            paintDataArea(aPntr);

        // Restore Graphics state
        aPntr.restore();
    }

    /**
     * Paints the DataArea (ChartType/Trace specific painting).
     */
    protected void paintDataArea(Painter aPntr)  { }

    /**
     * Paints the DataArea above all DataView.DataAreas.paintDataArea() painting.
     */
    protected void paintDataAreaAbove(Painter aPntr)
    {
        Trace trace = getTrace();
        if (trace.isShowTags())
            paintDataTags(aPntr);
    }

    /**
     * Paints tags for Trace.
     */
    protected void paintDataTags(Painter aPntr)  { }

    /**
     * Returns the data point closest to given x/y in local coords (null if none).
     */
    public TracePoint getDataPointForLocalXY(double aX, double aY)
    {
        // Constant for maximum display distance (in points)
        int MAX_SELECT_DISTANCE = 60;

        // Get data info
        DataSet stagedData = getStagedData();
        int pointCount = stagedData.getPointCount();
        TracePoint dataPoint = null;
        double dist = MAX_SELECT_DISTANCE;

        // Iterate over points and get closest DataPoint
        for (int i = 0; i < pointCount; i++) {
            double dataX = stagedData.getX(i);
            double dataY = stagedData.getY(i);
            double dispX = dataToViewX(dataX);
            double dispY = dataToViewY(dataY);
            double dst = Point.getDistance(aX, aY, dispX, dispY);
            if (dst < dist) {
                dist = dst;
                dataPoint = getTrace().getPoint(i);
            }
        }

        // Return DataPoint
        return dataPoint;
    }

    /**
     * Returns the given data point X/Y in this view coords.
     */
    public Point getLocalXYForDataPoint(TracePoint aDP)
    {
        DataSet stagedData = getStagedData();
        int index = aDP.getIndex();
        double dataX = stagedData.getX(index);
        double dataY = stagedData.getY(index);
        return dataToView(dataX, dataY);
    }

    /**
     * Called to reset view from updated Chart.
     */
    protected void resetView()
    {
        // Get/set Trace.AxisTypeY. If chart type doesn't support it, coerce down to standard Y
        _axisTypeY = _trace.getAxisTypeY();
        if (_axisTypeY != AxisType.Y && !_chartHelper.isAxisType(_axisTypeY))
            _axisTypeY = AxisType.Y;
    }

    /**
     * Clears the staged data.
     */
    protected void clearStagedData()
    {
        _stagedData = null;
        clearDisplayData();
    }

    /**
     * Clears the staged data.
     */
    protected void clearDisplayData()
    {
        _dispData = null;
        repaint();
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        // Clear XYPainter
        Object src = aPC.getSource();
        String propName = aPC.getPropName();
        Trace trace = getTrace();
        TraceStyle traceStyle = trace.getTraceStyle();
        if (src == trace || src == traceStyle || src instanceof Axis || propName == Trace.Stacked_Prop) {
            clearStagedData();
        }
    }

    /**
     * Called when DataView changes size.
     */
    protected void dataViewDidChangeSize()
    {
        clearDisplayData();
    }

    /**
     * Called when AxisView changes properties.
     */
    protected void axisViewDidChange(PropChange aPC)
    {
        clearDisplayData();
    }
}