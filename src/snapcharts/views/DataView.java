package snapcharts.views;
import java.util.*;
import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.Intervals;
import snapcharts.model.*;

/**
 * A view to display the actual contents of a chart.
 */
public abstract class DataView extends ChartPartView {
    
    // The ChartView that owns the area
    protected ChartView  _chartView;
    
    // The amount of the chart to show horizontally (0-1)
    private double  _reveal = 1;
    
    // Constants for properties
    public static String   Reveal_Prop = "Reveal";

    // Constants for defaults
    private static Color  AXIS_LINES_COLOR = Color.LIGHTGRAY;

    /**
     * Creates a ChartArea.
     */
    public DataView()
    {
        setGrowWidth(true);
        setPrefSize(600,350);
        enableEvents(MouseMove, MouseRelease, MouseExit);
    }

    /**
     * Returns the ChartType.
     */
    public abstract ChartType getChartType();

    /**
     * Sets the chart view.
     */
    protected void setChartView(ChartView aCV)  { _chartView = aCV; }

    /**
     * Returns the X axis view.
     */
    public AxisViewX getAxisX()  { return _chartView.getAxisX(); }

    /**
     * Returns the Y axis view.
     */
    public AxisViewY getAxisY()  { return _chartView.getAxisY(); }

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
     * Returns the X axis intervals for active datasets.
     */
    public Intervals getIntervalsX()
    {
        // Get DataSetList and Width
        DataSetList dsetList = getDataSetList();
        double width = getWidth() - getInsetsAll().getWidth();

        // If Bar, reset width to -1 to use index as X
        ChartType chartType = getChartType();
        if (chartType==ChartType.BAR || chartType==ChartType.BAR_3D)
            width = -1;

        // Return intervals
        return dsetList.getIntervalsX(width);
    }

    /**
     * Returns the Y axis intervals for active datasets.
     */
    public Intervals getIntervalsY()
    {
        DataSetList dsetList = getDataSetList();
        double height = getHeight() - getInsetsAll().getHeight();
        return dsetList.getIntervalsY(height);
    }

    /**
     * Returns the dataset color at index.
     */
    public Color getColor(int anIndex)  { return getChart().getColor(anIndex); }

    /**
     * Returns the dataset shape at index.
     */
    public Shape getMarkerShape(int anIndex)  { return getChart().getMarkerShape(anIndex); }

    /**
     * Return the ratio of the chart to show horizontally.
     */
    public double getReveal()  { return _reveal; }

    /**
     * Sets the reation of the chart to show horizontally.
     */
    public void setReveal(double aValue)
    {
        _reveal = aValue;
        repaint();
    }

    /**
     * Registers for animation.
     */
    public void animate()
    {
        setReveal(0);
        getAnimCleared(1000).setValue(Reveal_Prop,1).setLinear().play();
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
        Insets ins = getInsetsAll();
        double dataMin = getIntervalsX().getMin();
        double dataMax = getIntervalsX().getMax();
        double width = getWidth() - ins.getWidth();
        return ins.left + (dataX - dataMin)/(dataMax - dataMin)*width;
    }

    /**
     * Converts a X coord from data coords to view coords.
     */
    public double dataToViewY(double dataY)
    {
        Insets ins = getInsetsAll();
        Intervals intervals = getIntervalsY();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        double height = getHeight() - ins.getHeight();
        return ins.top + height - (dataY - dataMin)/(dataMax - dataMin)*height;
    }

    /**
     * Paints chart axis lines.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get insets and chart content width/height (minus insets)
        Insets ins = getInsetsAll();
        double pw = getWidth(), ph = getHeight();
        double w = pw - ins.getWidth();
        double h = ph - ins.getHeight();

        // Set axis line color and stroke
        aPntr.setColor(AXIS_LINES_COLOR);
        double lineWidth = 1;
        double dashes[] = getAxisY().getGridLineDashArray();
        Stroke stroke = dashes==null && lineWidth==1? Stroke.Stroke1 : new Stroke(lineWidth, dashes, 0);
        aPntr.setStroke(stroke);

        // Have YAxisView paint lines
        if (_chartView.getAxisY().isVisible())
            paintAxisY(aPntr, 0, ins.top, pw, h);

        // Paint chart
        paintChart(aPntr, ins.left, ins.top, w, h);
    }

    /**
     * Paints chart axis lines.
     */
    protected void paintAxisY(Painter aPntr, double aX, double aY, double aW, double aH)
    {
        // Get number of interval lines and interval height
        int intervalCount = getIntervalsY().getCount();
        double intervalLen = aH/(intervalCount-1);

        // Draw y axis lines
        for (int i=0;i<intervalCount;i++) {
            double dispY = Math.round(aY + i*intervalLen);
            aPntr.drawLine(0, dispY, aW, dispY);
        }

        aPntr.setStroke(Stroke.Stroke1);
    }

    /**
     * Paints chart content.
     */
    protected void paintChart(Painter aPntr, double aX, double aY, double aW, double aH)  { }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseMove
        if (anEvent.isMouseMove()) {
            DataPoint dpnt = getDataPointForXY(anEvent.getX(), anEvent.getY());
            _chartView.setTargDataPoint(dpnt);
        }

        // Handle MouseClick
        if (anEvent.isMouseClick()) {
            DataPoint dpnt = getDataPointForXY(anEvent.getX(), anEvent.getY());
            if (dpnt==_chartView.getSelDataPoint()) dpnt = null;
            _chartView.setSelDataPoint(dpnt);
        }

        // Handle MouseExit
        if (anEvent.isMouseExit())
            _chartView.setTargDataPoint(null);
    }

    /**
     * Returns the data point best associated with given x/y (null if none).
     */
    protected DataPoint getDataPointForXY(double aX, double aY)
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
     * Called after a chart area is installed in chart view.
     */
    public void activate()
    {
        // Enable all datasets
        DataSetList dataSetList = getDataSetListAll();
        List<DataSet> dsets = dataSetList.getDataSets();
        for (DataSet dset : dsets)
            dset.setDisabled(false);
    }

    /**
     * Called before a chart area is removed from a chart view.
     */
    public void deactivate()  { }

    /**
     * Called when chart is reloaded.
     */
    public void reactivate()  { }

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

    /**
     * Returns the value for given key.
     */
    public Object getValue(String aPropName)
    {
        if (aPropName.equals(Reveal_Prop)) return getReveal();
        return super.getValue(aPropName);
    }

    /**
     * Sets the value for given key.
     */
    public void setValue(String aPropName, Object aValue)
    {
        if (aPropName.equals(Reveal_Prop)) setReveal(SnapUtils.doubleValue(aValue));
        else super.setValue(aPropName, aValue);
    }

    /**
     * Creates a DataView for given type.
     */
    public static DataView createDataViewForType(ChartType aType)
    {
        switch (aType)
        {
            case BAR: return new DataViewBar();
            case BAR_3D: return new DataViewBar3D();
            case LINE: return new DataViewLine();
            case PIE: return new DataViewPie();
            default: throw new RuntimeException("DataView.createDataViewForType: Unknown type: " + aType);
        }
    }
}