/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Point;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.util.PropChange;
import snap.view.Cursor;
import snapcharts.model.Intervals;
import snapcharts.model.*;
import snapcharts.util.DataStoreUtils;

/**
 * A view to display the actual contents of a chart.
 */
public abstract class DataArea extends ChartPartView<DataSet> {

    // The ChartHelper
    protected ChartHelper  _chartHelper;

    // The DataView that holds this DataArea
    protected DataView  _dataView;

    // The DataSet
    private DataSet  _dataSet;

    // The AxisType for Y axis (might get coerced down to Y if chart type doesn't support it)
    private AxisType  _axisTypeY;

    // The DataSet.ProcessedData possibly further processed for DataArea/Axes
    private DataStore _stagedData;

    // The ProcessedData converted to DataArea display coords
    private DataStore  _dispData;

    /**
     * Constructor.
     */
    public DataArea(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super();
        setCursor(Cursor.MOVE);

        // Set ivars
        _chartHelper = aChartHelper;
        _dataSet = aDataSet;

        // Get/set DataSet.AxisTypeY. If chart type doesn't support it, coerce down to standard Y
        _axisTypeY = _dataSet.getAxisTypeY();
        if (_axisTypeY != AxisType.Y && !_chartHelper.isAxisType(_axisTypeY))
            _axisTypeY = AxisType.Y;
    }

    /**
     * Returns the ChartPart.
     */
    @Override
    public DataSet getChartPart()  { return _dataSet; }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _dataSet; }

    /**
     * Returns the DataStyle.
     */
    public DataStyle getDataStyle()  { return _dataSet.getDataStyle(); }

    /**
     * Returns whether dataset is enabled.
     */
    public boolean isDataSetEnabled()  { return _dataSet.isEnabled(); }

    /**
     * Returns whether dataset is disabled.
     */
    public boolean isDataSetDisabled()  { return _dataSet.isDisabled(); }

    /**
     * Returns the DataSet AxisType.
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
     * Returns the DataSetList of active data sets.
     */
    public DataSetList getDataSetList()
    {
        return _chartHelper.getDataSetList();
    }

    /**
     * Returns the dataset color at index.
     */
    public Color getDataColor()
    {
        DataStyle dataStyle = getDataStyle();
        return dataStyle.getLineColor();
    }

    /**
     * Returns the dataset color at index.
     */
    public Color getColorMapColor(int anIndex)
    {
        DataStyle dataStyle = getDataStyle();
        return dataStyle.getColorMapColor(anIndex);
    }

    /**
     * Returns the Symbol shape at index.
     */
    public Symbol getDataSymbol()
    {
        DataSet dataSet = getDataSet();
        DataStyle dataStyle = dataSet.getDataStyle();
        SymbolStyle symbolStyle = dataStyle.getSymbolStyle();
        return symbolStyle.getSymbol();
    }

    /**
     * Returns the DataSet.ProcessedData possibly further processed for DataArea/Axes.
     * Conditions that cause further processing: Stacked, AxisWrap, Axis Log.
     */
    public DataStore getStagedData()
    {
        // If already set, just return
        if (_stagedData != null) return _stagedData;

        // Get DataSet and ProcessedData
        DataSet dataSet = getDataSet();
        DataStore dataStore = dataSet.getProcessedData();

        // If Log, use DataSet.LogData
        AxisViewX axisViewX = getAxisViewX();
        AxisViewY axisViewY = getAxisViewY();
        Axis axisX = axisViewX != null ? axisViewX.getAxis() : null;
        Axis axisY = axisViewY != null ? axisViewY.getAxis() : null;
        boolean isLogX = axisX != null && axisX.isLog();
        boolean isLogY = axisY != null && axisY.isLog();
        if (isLogX || isLogY)
            dataStore = dataSet.getLogData(isLogX, isLogY);

        // Handle stacked
        if (dataSet.isStacked()) {
            DataStore prevStackedData = getPreviousStackedData();
            if (prevStackedData != null)
                dataStore = DataStoreUtils.addStackedData(dataStore, prevStackedData);
        }

        // If WrapAxis, wrap DataStore inside DataStoreWrapper for wrap range and axis range
        if (axisX != null && axisX.isWrapAxis()) {

            // Get Wrap Min/Max values
            double wrapMin = axisX.getWrapMinMax().getMin();
            double wrapMax = axisX.getWrapMinMax().getMax();

            // Get Axis Min/Max values
            _stagedData = dataStore; // Is this lame? I think it might be
            double axisMin = axisViewX.getAxisMin();
            double axisMax = axisViewX.getAxisMax();
            dataStore = new DataStoreWrapper(dataStore, wrapMin, wrapMax, axisMin, axisMax);
        }

        // Set/return
        return _stagedData = dataStore;
    }

    /**
     * Returns the DataSet.ProcessedData as PureData in DataArea display coords.
     */
    public DataStore getDispData()
    {
        // If already set, just return
        if (_dispData != null) return _dispData;

        // Get StagedData
        DataStore dataStore = getStagedData();
        int pointCount = dataStore.getPointCount();
        double[] dispX = new double[pointCount];
        double[] dispY = new double[pointCount];

        // Get ChartHelper and AxisViews
        ChartHelper chartHelper = getChartHelper();
        AxisView axisViewX = getAxisViewX();
        AxisView axisViewY = getAxisViewY();

        // Iterate over data points
        for (int i = 0; i < pointCount; i++) {

            // Get data X/Y and disp X/Y
            double dataX = dataStore.getX(i);
            double dataY = dataStore.getY(i);
            dispX[i] = chartHelper.dataToView(axisViewX, dataX);
            dispY[i] = chartHelper.dataToView(axisViewY, dataY);
        }

        // Create PureData, set, return
        DataStore dispData = new DataStoreImpl(DataType.XY, dispX, dispY);
        return _dispData = dispData;
    }

    /**
     * Returns the start index for display data.
     */
    public int getDispDataStartIndex()
    {
        // Get DisplayData and PointCount
        DataStore dispData = getDispData();
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
        DataStore dispData = getDispData();
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
        int pointCount = getDispData().getPointCount();
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
        for (int i=index-1; i>=0; i--) {
            DataArea prevDataArea = dataAreas[i];
            DataSet prevDataSet = prevDataArea.getDataSet();
            if (prevDataSet.isStacked())
                return prevDataArea;
        }
        return null;
    }

    /**
     * Returns the previous stacked DataArea.StagedData.
     */
    public DataStore getPreviousStackedData()
    {
        DataArea prevDataArea = getPreviousStackedDataArea();
        return prevDataArea != null ? prevDataArea.getStagedData() : null;
    }

    /**
     * Returns whether dataset is selected.
     */
    public boolean isSelected()
    {
        DataSet dataSet = getDataSet();
        DataPoint dataPoint = getChartView().getSelDataPoint();
        return dataPoint != null && dataPoint.getDataSet() == dataSet;
    }

    /**
     * Returns whether dataset is targeted.
     */
    public boolean isTargeted()
    {
        DataSet dataSet = getDataSet();
        DataPoint dataPoint = getChartView().getTargDataPoint();
        return dataPoint != null && dataPoint.getDataSet() == dataSet;
    }

    /**
     * Returns whether dataset is selected or targeted.
     */
    public boolean isSelectedOrTargeted()
    {
        return isSelected() || isTargeted();
    }

    /**
     * Returns selected data point if in DataSet.
     */
    public DataPoint getSelDataPoint()
    {
        DataPoint selDataPoint = getChartView().getSelDataPoint();
        if (selDataPoint != null && selDataPoint.getDataSet() == getDataSet())
            return selDataPoint;
        return null;
    }

    /**
     * Returns targeted data point if in DataSet.
     */
    public DataPoint getTargDataPoint()
    {
        DataPoint targDataPoint = getChartView().getTargDataPoint();
        if (targDataPoint != null && targDataPoint.getDataSet() == getDataSet())
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
     * Returns the Y axis intervals for active datasets.
     */
    public Intervals getIntervalsY()
    {
        return getAxisViewY().getIntervals();
    }

    /**
     * Converts a point from dataset coords to view coords.
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
        DataSet dataSet = getDataSet();
        if (dataSet.isEnabled() || getParent().getChildCount()==1)
            paintDataArea(aPntr);

        // Restore Graphics state
        aPntr.restore();
    }

    /**
     * Paints the DataArea (ChartType/DataSet specific painting).
     */
    protected void paintDataArea(Painter aPntr)  { }

    /**
     * Paints the DataArea above all DataView.DataAreas.paintDataArea() painting.
     */
    protected void paintDataAreaAbove(Painter aPntr)
    {
        DataStyle dataStyle = getDataStyle();
        if (dataStyle.isShowTags())
            paintDataTags(aPntr);
    }

    /**
     * Paints tags for DataSet.
     */
    protected void paintDataTags(Painter aPntr)  { }

    /**
     * Returns the data point closest to given x/y in local coords (null if none).
     */
    public DataPoint getDataPointForLocalXY(double aX, double aY)
    {
        // Constant for maximum display distance (in points)
        int MAX_SELECT_DISTANCE = 60;

        // Get data info
        DataStore stagedData = getStagedData();
        int pointCount = stagedData.getPointCount();
        DataPoint dataPoint = null;
        double dist = MAX_SELECT_DISTANCE;

        // Iterate over points and get closest DataPoint
        for (int j=0; j<pointCount; j++) {
            double dataX = stagedData.getX(j);
            double dataY = stagedData.getY(j);
            double dispX = dataToViewX(dataX);
            double dispY = dataToViewY(dataY);
            double dst = Point.getDistance(aX, aY, dispX, dispY);
            if (dst < dist) {
                dist = dst;
                dataPoint = getDataSet().getPoint(j);
            }
        }

        // Return DataPoint
        return dataPoint;
    }

    /**
     * Returns the given data point X/Y in this view coords.
     */
    public Point getLocalXYForDataPoint(DataPoint aDP)
    {
        DataStore stagedData = getStagedData();
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
        // Get/set DataSet.AxisTypeY. If chart type doesn't support it, coerce down to standard Y
        _axisTypeY = _dataSet.getAxisTypeY();
        if (_axisTypeY != AxisType.Y && !_chartHelper.isAxisType(_axisTypeY))
            _axisTypeY = AxisType.Y;
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        // Clear XYPainter
        Object src = aPC.getSource();
        String propName = aPC.getPropName();
        if (src == getDataSet() || src instanceof Axis || propName == DataSet.Stacked_Prop) {
            _stagedData = null;
            _dispData = null;
        }
    }

    /**
     * Called when DataView changes size.
     */
    protected void dataViewDidChangeSize()
    {
        _dispData = null;
    }

    /**
     * Called when AxisView changes properties.
     */
    protected void axisViewDidChange(PropChange aPC)
    {
        _dispData = null;
    }
}