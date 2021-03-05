package snapcharts.views;
import java.util.*;
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

    // The ChartView that owns the area
    private ChartView  _chartView;

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
     * Returns the DataSet AxisType.
     */
    public AxisType getAxisTypeY()  { return _axisTypeY; }

    /**
     * Sets the DataView.
     */
    protected void setDataView(DataView aDataView)
    {
        _dataView = aDataView;
        _chartView = _dataView.getChartView();
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
    public Color getDataColor(int anIndex)  { return getChart().getColor(anIndex); }

    /**
     * Returns the Symbol shape at index.
     */
    public Shape getDataSymbolShape(int anIndex)
    {
        return getChart().getSymbolShape(anIndex);
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
        paintChart(aPntr);
        aPntr.restore();
    }

    /**
     * Paints chart content.
     */
    protected void paintChart(Painter aPntr)  { }

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
     * Returns the data point best associated with given x/y (null if none).
     */
    public DataPoint getDataPointForXY(double aX, double aY)
    {
        // If point out of bounds, return null
        if (aX<0 || aX>getWidth() || aY<0 || aY>getWidth()) return null;

        // Get data info
        DataSetList dsetList = getDataSetList();
        List<DataSet> dsets = dsetList.getDataSets();
        int pointCount = dsetList.getPointCount();

        // Iterate over active dataset to find dataset + value index closest to point
        DataPoint dataPoint = null;
        double dist = Float.MAX_VALUE;
        for (DataSet dset : dsets) {

            // Iterate over points
            for (int j=0; j<pointCount; j++) {
                double dataX = dset.getX(j);
                double dataY = dset.getY(j);
                double dispX = dataToViewX(dataX);
                double dispY = dataToViewY(dataY);
                double d = Point.getDistance(aX, aY, dispX, dispY);
                if (d<dist) {
                    dist = d;
                    dataPoint = dset.getPoint(j);
                }
            }
        }

        // Return DataPoint for closest dataset+index
        return dataPoint;
    }

    /**
     * Returns the given data point X/Y in this view coords.
     */
    public Point getDataPointXYLocal(DataPoint aDP)
    {
        return dataToView(aDP.getX(), aDP.getY());
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