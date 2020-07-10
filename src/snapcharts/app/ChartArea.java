package snapcharts.app;
import java.util.*;

import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Shape;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSeries;
import snapcharts.model.DataSet;

/**
 * A view to display the actual contents of a chart.
 */
public class ChartArea extends ParentView {
    
    // The ChartView that owns the area
    ChartView           _chartView;
    
    // The amount of the chart to show horizontally (0-1)
    double              _reveal = 1;
    
    // Constants for properties
    public static String   Reveal_Prop = "Reveal";
    public static String   DataPoint_Prop = "DataPoint";

    // Constants for defaults
    private static Color  AXIS_LINES_COLOR = Color.LIGHTGRAY;

/**
 * Creates a ChartArea.
 */
public ChartArea()
{
    setGrowWidth(true); setPrefSize(600,350);
    enableEvents(MouseMove, MouseRelease, MouseExit);
}

/**
 * Sets the chart view.
 */
protected void setChartView(ChartView aCV)  { _chartView = aCV; }

/**
 * Returns the chart.
 */
public Chart getChart()  { return _chartView.getChart(); }

/**
 * Returns the XAxis View.
 */
public ChartXAxis getXAxis()  { return _chartView._axisX; }

/**
 * Returns the YAxis View.
 */
public ChartYAxis getYAxis()  { return _chartView._axisY; }

/**
 * Returns the data set.
 */
public DataSet getDataSet()  { return _chartView.getDataSet(); }

/**
 * Returns the series.
 */
public List <DataSeries> getSeries()  { return getDataSet().getSeries(); }

/**
 * Returns the number of series.
 */
public int getSeriesCount()  { return getDataSet().getSeriesCount(); }

/**
 * Returns the individual series at given index.
 */
public DataSeries getSeries(int anIndex)  { return getDataSet().getSeries(anIndex); }

/**
 * Returns the active data set.
 */
public DataSet getActiveSet()  { return getDataSet().getActiveSet(); }

/**
 * Returns the active series.
 */
public List <DataSeries> getActiveSeries()  { return getDataSet().getActiveSeries(); }

/**
 * Returns the length of the series.
 */
public int getPointCount()  { return getDataSet().getPointCount(); }

/**
 * Returns the intervals.
 */
public Intervals getActiveIntervals()
{
    double height = getHeight() - getInsetsAll().getHeight();
    return getDataSet().getActiveIntervals(height);
}

/**
 * Returns the series color at index.
 */
public Color getColor(int anIndex)  { return getChart().getColor(anIndex); }

/**
 * Returns the series shape at index.
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
 * Converts a point from series to local.
 */
public Point seriesToLocal(double aX, double aY)
{
    // Get insets
    Insets ins = getInsetsAll();
    
    // Convert X
    DataSet dset = getDataSet();
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
    return seriesToLocal(index, y);
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
    double dashes[] = getYAxis().getGridLineDashArray();
    Stroke stroke = dashes==null && lineWidth==1? Stroke.Stroke1 : new Stroke(lineWidth, dashes, 0);
    aPntr.setStroke(stroke);
    
    // Have YAxisView paint lines
    if(_chartView.getAxisY().isVisible())
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
    for(int i=0;i<intervalCount;i++) {
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
    if(anEvent.isMouseMove()) {
        DataPoint dpnt = getDataPointAt(anEvent.getX(), anEvent.getY());
        _chartView.setTargDataPoint(dpnt);
    }
        
    // Handle MouseClick
    if(anEvent.isMouseClick()) {
        DataPoint dpnt = getDataPointAt(anEvent.getX(), anEvent.getY());
        if(dpnt==_chartView.getSelDataPoint()) dpnt = null;
        _chartView.setSelDataPoint(dpnt);
    }
        
    // Handle MouseExit
    if(anEvent.isMouseExit())
        _chartView.setTargDataPoint(null);
}

/**
 * Returns the data point best associated with given x/y (null if none).
 */
protected DataPoint getDataPointAt(double aX, double aY)
{
    // If point out of bounds, return null
    if(aX<0 || aX>getWidth() || aY<0 || aY>getWidth()) return null;
    
    // Iterate over active series to find series + value index closest to point
    DataPoint dataPoint = null; double dist = Float.MAX_VALUE;
    List <DataSeries> seriesList = getActiveSeries();
    for(int i=0;i<seriesList.size();i++) { DataSeries series = seriesList.get(i);
        for(int j=0;j<getPointCount();j++) {
            Point pnt = seriesToLocal(j,series.getValueX(j));
            double d = Point.getDistance(aX, aY, pnt.x, pnt.y);
            if(d<dist) { dist = d;
                dataPoint = series.getPoint(j); }
        }
    }
    
    // Return DataPoint for closest series+index
    return dataPoint;
}

/**
 * Called after a chart area is installed in chart view.
 */
public void activate()
{
    // Enable all series
    for(int i=0; i<getSeriesCount(); i++) getSeries(i).setDisabled(false);
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
    if(aPropName.equals(Reveal_Prop)) return getReveal();
    return super.getValue(aPropName);
}

/**
 * Sets the value for given key.
 */
public void setValue(String aPropName, Object aValue)
{
    if(aPropName.equals(Reveal_Prop)) setReveal(SnapUtils.doubleValue(aValue));
    else super.setValue(aPropName, aValue);
}

}