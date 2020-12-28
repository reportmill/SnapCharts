package snapcharts.views;
import java.util.*;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.*;
import snapcharts.model.Intervals;
import snapcharts.model.*;

/**
 * A view to display the actual contents of a chart.
 */
public abstract class DataArea<T extends DataSet> extends ChartPartView<T> {

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The ChartView that owns the area
    private ChartView  _chartView;

    // The DataView that holds this DataArea
    protected DataView _dataView;

    // The DataSet
    private T  _dataSet;
    
    // Constants for defaults
    protected static Color  BORDER_COLOR = Color.GRAY;
    protected static Color GRID_LINE_COLOR = Color.get("#E6");
    protected static double GRID_LINE_WIDTH = 1;
    protected static Color TICK_LINE_COLOR = Color.GRAY;
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
    public AxisViewX getAxisX()  { return _dataView.getAxisX(); }

    /**
     * Returns the Y axis view.
     */
    public AxisViewY getAxisY()  { return _dataView.getAxisY(); }

    /**
     * Returns the data set list.
     */
    public DataSetList getDataSetListAll()  { return _chartView.getDataSetList(); }

    /**
     * Returns the DataSetList of active data sets.
     */
    public DataSetList getDataSetList()  { return getDataSetListAll().getActiveList(); }

    /**
     * Returns number of points in datasets.
     */
    public int getPointCount()
    {
        DataSetList dsetList = getDataSetList();
        return dsetList.getPointCount();
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
        return getAxisY().getIntervals();
    }

    /**
     * Converts a point from dataset coords to view coords.
     */
    public Point dataToView(double dataX, double dataY)  { return _dataView.dataToView(dataX, dataY); }

    /**
     * Converts a X coord from data coords to view coords.
     */
    public double dataToViewX(double dataX)  { return _dataView.dataToViewX(dataX); }

    /**
     * Converts a X coord from data coords to view coords.
     */
    public double dataToViewY(double dataY)  { return _dataView.dataToViewY(dataY); }

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
        AxisViewX axisView = getAxisX(); if (axisView==null) return;
        AxisX axis = axisView.getAxis();

        // Set Grid Color/Stroke
        aPntr.setColor(GRID_LINE_COLOR);
        double dashes[] = axisView.getGridLineDashArray();
        Stroke stroke = dashes==null ? Stroke.Stroke1 : new Stroke(GRID_LINE_WIDTH, dashes, 0);
        aPntr.setStroke(stroke);

        // Iterate over intervals and paint lines
        double areaY = 0;
        double areaH = getHeight();
        double tickLen = axis.getTickLength();
        Intervals ivals = axisView.getIntervals();
        for (int i = 0, iMax = ivals.getCount(); i < iMax; i++) {
            double dataX = ivals.getInterval(i);
            double dispX = (int) Math.round(dataToViewX(dataX));
            aPntr.setColor(GRID_LINE_COLOR);
            aPntr.drawLine(dispX, areaY, dispX, areaH);
            aPntr.setColor(TICK_LINE_COLOR);
            aPntr.drawLine(dispX, areaY + areaH - tickLen, dispX, areaY + areaH);
        }
    }

    /**
     * Paints chart Y axis lines.
     */
    protected void paintGridlinesY(Painter aPntr)
    {
        // Get info
        AxisViewY axisView = getAxisY(); if (axisView==null || !axisView.isVisible()) return;
        AxisY axis = axisView.getAxis();

        // Set Grid Color/Stroke
        aPntr.setColor(GRID_LINE_COLOR);
        double dashes[] = axisView.getGridLineDashArray();
        Stroke stroke = dashes==null ? Stroke.Stroke1 : new Stroke(GRID_LINE_WIDTH, dashes, 0);
        aPntr.setStroke(stroke);

        // Iterate over intervals and paint lines
        double areaX = 0;
        double areaW = getWidth();
        double tickLen = axis.getTickLength();
        Intervals ivals = getIntervalsY();
        for (int i=0, iMax=ivals.getCount(); i<iMax; i++) {
            double dataY = ivals.getInterval(i);
            double dispY = (int) Math.round(dataToViewY(dataY));
            aPntr.setColor(GRID_LINE_COLOR);
            aPntr.drawLine(areaX, dispY, areaW, dispY);
            aPntr.setColor(TICK_LINE_COLOR);
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
     * Call to clear any cached data.
     */
    protected void clearCache()  { }

    /**
     * Override to clear section/bar cache.
     */
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        clearCache();
    }

    /**
     * Override to clear section/bar cache.
     */
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return;
        super.setHeight(aValue);
        clearCache();
    }
}