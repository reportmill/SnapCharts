package snapcharts.view;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.Intervals;
import snapcharts.model.*;

/**
 * A view to display the actual contents of a chart.
 */
public abstract class DataArea<T extends DataSet> extends ChartPartView<T> {

    // The ChartHelper
    protected ChartHelper  _chartHelper;

    // The DataView that holds this DataArea
    protected DataView _dataView;

    // The DataSet
    private T  _dataSet;

    // The AxisType for Y axis (might get coerced down to Y if chart type doesn't support it)
    private AxisType  _axisTypeY;
    
    // Constants for defaults
    protected static Color  BORDER_COLOR = Color.GRAY;
    protected static Color AXIS_LINE_COLOR = Color.DARKGRAY;

    /**
     * Constructor.
     */
    public DataArea(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super();

        // Set ivars
        _chartHelper = aChartHelper;
        _dataSet = (T) aDataSet;

        // Get/set DataSet.AxisTypeY. If chart type doesn't support it, coerce down to standard Y
        _axisTypeY = _dataSet.getAxisTypeY();
        if (_axisTypeY != AxisType.Y && !_chartHelper.isAxisType(_axisTypeY))
            _axisTypeY = AxisType.Y;
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return _dataSet; }

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
        return dataStyle.getSymbol();
    }

    /**
     * Returns the Symbol shape at index.
     */
    public Shape getDataSymbolShape()
    {
        Symbol symbol = getDataSymbol();
        return symbol.getShape();
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
     * Paints chart axis lines.
     */
    public void paintBorder(Painter aPntr)
    {
        // Get view area
        double areaX = 0;
        double areaY = 0;
        double areaW = getWidth();
        double areaH = getHeight();

        // Paint Border
        aPntr.setColor(DataArea.BORDER_COLOR);
        aPntr.setStroke(Stroke.Stroke1);
        aPntr.drawRect(areaX, areaY, areaW, areaH);

        // Paint Axis lines
        aPntr.setColor(DataArea.AXIS_LINE_COLOR);
        aPntr.drawLine(areaX, areaY, areaX, areaY + areaH);
        aPntr.drawLine(areaX, areaY + areaH, areaX + areaW, areaY + areaH);
    }

    /**
     * Paints chart axis lines.
     */
    public void paintGridlines(Painter aPntr)
    {
        paintGridlinesX(aPntr);
        paintGridlinesY(aPntr);
    }

    /**
     * Paints chart X axis lines.
     */
    protected void paintGridlinesX(Painter aPntr)
    {
        // Get info
        AxisViewX axisView = getAxisViewX(); if (axisView==null) return;
        AxisX axis = axisView.getAxis();
        Color gridColor = axisView.getGridColor();
        Color tickLineColor = AxisView.TICK_LINE_COLOR;
        double tickLen = axis.getTickLength();

        // Set Grid Color/Stroke
        aPntr.setColor(gridColor);
        aPntr.setStroke(axisView.getGridStroke());

        // Iterate over intervals and paint lines
        double areaY = 0;
        double areaH = getHeight();
        Intervals ivals = axisView.getIntervals();
        for (int i = 0, iMax = ivals.getCount(); i < iMax; i++) {
            double dataX = ivals.getInterval(i);
            double dispX = (int) Math.round(_chartHelper.dataToView(axisView, dataX));
            aPntr.setColor(gridColor);
            aPntr.drawLine(dispX, areaY, dispX, areaH);
            aPntr.setColor(tickLineColor);
            aPntr.drawLine(dispX, areaY + areaH - tickLen, dispX, areaY + areaH);
        }
    }

    /**
     * Paints chart Y axis lines.
     */
    protected void paintGridlinesY(Painter aPntr)
    {
        // Get info
        AxisViewY axisView = getAxisViewY(); if (axisView==null || !axisView.isVisible()) return;
        AxisY axis = axisView.getAxis();
        Color gridColor = axisView.getGridColor();
        Color tickLineColor = AxisView.TICK_LINE_COLOR;
        double tickLen = axis.getTickLength();

        // Set Grid Color/Stroke
        aPntr.setColor(gridColor);
        aPntr.setStroke(axisView.getGridStroke());

        // Iterate over intervals and paint lines
        double areaX = 0;
        double areaW = getWidth();
        Intervals ivals = axisView.getIntervals();
        for (int i=0, iMax=ivals.getCount(); i<iMax; i++) {
            double dataY = ivals.getInterval(i);
            double dispY = (int) Math.round(_chartHelper.dataToView(axisView, dataY));
            aPntr.setColor(gridColor);
            aPntr.drawLine(areaX, dispY, areaW, dispY);
            aPntr.setColor(tickLineColor);
            aPntr.drawLine(areaX, dispY, areaX + tickLen, dispY);
        }
    }

    /**
     * Returns the data point closest to given x/y in local coords (null if none).
     */
    public DataPoint getDataPointForLocalXY(double aX, double aY)
    {
        // Constant for maximum display distance (in points)
        int MAX_SELECT_DISTANCE = 60;

        // Get data info
        DataSet dataSet = getDataSet();
        int pointCount = dataSet.getPointCount();
        DataPoint dataPoint = null;
        double dist = MAX_SELECT_DISTANCE;

        // Iterate over points and get closest DataPoint
        for (int j=0; j<pointCount; j++) {
            double dataX = dataSet.getX(j);
            double dataY = dataSet.getY(j);
            double dispX = dataToViewX(dataX);
            double dispY = dataToViewY(dataY);
            double dst = Point.getDistance(aX, aY, dispX, dispY);
            if (dst < dist) {
                dist = dst;
                dataPoint = dataSet.getPoint(j);
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
        double dataX = aDP.getX();
        double dataY = aDP.getY();
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
    protected void chartPartDidChange(PropChange aPC)  { }

    /**
     * Called when DataView changes size.
     */
    protected void dataViewDidChangeSize()  { }

    /**
     * Called when AxisView changes properties.
     */
    protected void axisViewDidChange(PropChange aPC)  { }
}