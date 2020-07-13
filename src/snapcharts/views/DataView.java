package snapcharts.views;
import java.util.*;

import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.app.Intervals;
import snapcharts.model.*;

/**
 * A view to display the actual contents of a chart.
 */
public abstract class DataView extends ParentView {
    
    // The ChartView that owns the area
    protected ChartView  _chartView;
    
    // The amount of the chart to show horizontally (0-1)
    private double  _reveal = 1;
    
    // Constants for properties
    public static String   Reveal_Prop = "Reveal";
    public static String   DataPoint_Prop = "DataPoint";

    // Constants for defaults
    private static Color  AXIS_LINES_COLOR = Color.LIGHTGRAY;

    /**
     * Creates a ChartArea.
     */
    public DataView()
    {
        setGrowWidth(true); setPrefSize(600,350);
        enableEvents(MouseMove, MouseRelease, MouseExit);
    }

    /**
     * Returns the type.
     */
    public abstract ChartType getType();

    /**
     * Sets the chart view.
     */
    protected void setChartView(ChartView aCV)  { _chartView = aCV; }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _chartView.getChart(); }

    /**
     * Returns the X axis view.
     */
    public AxisViewX getAxisX()  { return _chartView._axisX; }

    /**
     * Returns the Y axis view.
     */
    public AxisViewY getAxisY()  { return _chartView._axisY; }

    /**
     * Returns the data set list.
     */
    public DataSetList getDataSetList()  { return _chartView.getDataSetList(); }

    /**
     * Returns the actual list of datasets.
     */
    public List <DataSet> getDataSets()  { return getDataSetList().getDataSets(); }

    /**
     * Returns the number of datasets.
     */
    public int getDataSetCount()  { return getDataSetList().getDataSetCount(); }

    /**
     * Returns the individual dataset at given index.
     */
    public DataSet getDataSet(int anIndex)  { return getDataSetList().getDataSet(anIndex); }

    /**
     * Returns the DataSetList of active data sets.
     */
    public DataSetList getActiveDataSetList()  { return getDataSetList().getActiveDataSetList(); }

    /**
     * Returns the active dataset.
     */
    public List <DataSet> getActiveDataSets()  { return getDataSetList().getActiveDataSets(); }

    /**
     * Returns number of points in datasets.
     */
    public int getPointCount()  { return getDataSetList().getPointCount(); }

    /**
     * Returns the intervals.
     */
    public Intervals getActiveIntervals()
    {
        double height = getHeight() - getInsetsAll().getHeight();
        return getDataSetList().getActiveIntervals(height);
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
    public Point dataToView(double aX, double aY)
    {
        // Get insets
        Insets ins = getInsetsAll();

        // Convert X
        DataSetList dset = getDataSetList();
        int count = dset.getPointCount();
        double w = getWidth() - ins.getWidth();
        double dx = w/(count-1);
        double nx = ins.left + aX*dx;

        // Convert Y and return
        double axisMinVal = getActiveIntervals().getMin();
        double axisMaxVal = getActiveIntervals().getMax();
        double h = getHeight() - ins.getHeight();
        double ny = ins.top + h - (aY-axisMinVal)/(axisMaxVal-axisMinVal)*h;
        return new Point(nx, ny);
    }

    /**
     * Returns the given data point in local coords.
     */
    public Point dataPointInLocal(DataPoint aDP)
    {
        int index = aDP.getIndex(); double y = aDP.getValueX();
        return dataToView(index, y);
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
        int intervalCount = getActiveIntervals().getCount();
        double ih = aH/(intervalCount-1);

        // Draw y axis lines
        for (int i=0;i<intervalCount;i++) {
            double y = aY + i*ih; y = Math.round(y);
            aPntr.drawLine(0, y, aW, y);
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
            DataPoint dpnt = getDataPointAt(anEvent.getX(), anEvent.getY());
            _chartView.setTargDataPoint(dpnt);
        }

        // Handle MouseClick
        if (anEvent.isMouseClick()) {
            DataPoint dpnt = getDataPointAt(anEvent.getX(), anEvent.getY());
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
    protected DataPoint getDataPointAt(double aX, double aY)
    {
        // If point out of bounds, return null
        if (aX<0 || aX>getWidth() || aY<0 || aY>getWidth()) return null;

        // Iterate over active dataset to find dataset + value index closest to point
        DataPoint dataPoint = null; double dist = Float.MAX_VALUE;
        List <DataSet> dsets = getActiveDataSets();
        for (int i=0;i<dsets.size();i++) { DataSet dset = dsets.get(i);
            for (int j=0;j<getPointCount();j++) {
                Point pnt = dataToView(j,dset.getValueX(j));
                double d = Point.getDistance(aX, aY, pnt.x, pnt.y);
                if (d<dist) { dist = d;
                    dataPoint = dset.getPoint(j); }
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
        for (int i = 0; i< getDataSetCount(); i++) getDataSet(i).setDisabled(false);
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