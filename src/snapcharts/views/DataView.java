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
    
    // The ChartArea that holds this DataView
    protected ChartArea  _chartArea;
    
    // The ChartView that owns the area
    protected ChartView  _chartView;

    // The amount of the chart to show horizontally (0-1)
    private double  _reveal = 1;

    // Constants for properties
    public static String   Reveal_Prop = "Reveal";

    // Constants for defaults
    protected static Color  BORDER_COLOR = Color.GRAY;
    protected static Color  GRID_LINES_COLOR = Color.get("#E6");
    protected static Color  TICK_LINES_COLOR = Color.GRAY;
    protected static Color  AXIS_LINES_COLOR = Color.DARKGRAY;

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
     * Returns whether this is bar chart.
     */
    public boolean isChartTypeBar()
    {
        ChartType chartType = getChartType();
        return chartType==ChartType.BAR || chartType==ChartType.BAR_3D;
    }

    /**
     * Sets the ChartArea.
     */
    protected void setChartArea(ChartArea aChartArea)
    {
        _chartArea = aChartArea;
        _chartView = _chartArea.getChartView();
    }

    /**
     * Returns the X axis view.
     */
    public AxisViewX getAxisX()  { return _chartArea.getAxisX(); }

    /**
     * Returns the Y axis view.
     */
    public AxisViewY getAxisY()  { return _chartArea.getAxisY(); }

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
        return getAxisX().getIntervals();
    }

    /**
     * Returns the Y axis intervals for active datasets.
     */
    public Intervals getIntervalsY()
    {
        return getAxisY().getIntervals();
    }

    /**
     * Returns the dataset color at index.
     */
    public Color getColor(int anIndex)  { return getChart().getColor(anIndex); }

    /**
     * Returns the Symbol shape at index.
     */
    public Shape getSymbolShape(int anIndex)  { return getChart().getSymbolShape(anIndex); }

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
        Intervals intervals = getIntervalsX();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        double axisX = ins.left;
        double areaW = getWidth() - ins.getWidth();
        return axisX + (dataX - dataMin)/(dataMax - dataMin)*areaW;
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
        double areaY = ins.top;
        double areaH = getHeight() - ins.getHeight();
        return areaY + areaH - (dataY - dataMin)/(dataMax - dataMin)*areaH;
    }

    /**
     * Converts a X coord from view coords to data coords.
     */
    public double viewToDataX(double dispX)
    {
        Insets ins = getInsetsAll();
        Intervals intervals = getIntervalsX();
        double dataMin = intervals.getMin();
        double dataMax = intervals.getMax();
        double dispMin = ins.left;
        double dispMax = dispMin + getWidth() - ins.getWidth();
        return dataMin + (dispX - dispMin)/(dispMax - dispMin)*(dataMax - dataMin);
    }

    /**
     * Paints chart axis lines.
     */
    protected void paintFront(Painter aPntr)
    {
        // Get insets and chart content width/height (minus insets)
        Insets ins = getInsetsAll();
        double viewW = getWidth();
        double viewH = getHeight();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = viewW - ins.getWidth();
        double areaH = viewH - ins.getHeight();

        // Set axis line color and stroke
        double lineWidth = 1;
        double dashes[] = getAxisY().getGridLineDashArray();
        Stroke stroke = dashes==null && lineWidth==1 ? Stroke.Stroke1 : new Stroke(lineWidth, dashes, 0);
        aPntr.setStroke(stroke);

        // Paint grid lines
        aPntr.setColor(GRID_LINES_COLOR);
        if (getAxisY().isVisible())
            paintGridlines(aPntr, areaX, areaY, areaW, areaH);

        // Paint Border
        aPntr.setColor(BORDER_COLOR);
        aPntr.drawRect(areaX, areaY, areaW, areaH);

        // Paint Axis lines
        aPntr.setColor(AXIS_LINES_COLOR);
        aPntr.drawLine(areaX, areaY, areaX, areaY + areaH);
        aPntr.drawLine(areaX, areaY + areaH, areaX + areaW, areaY + areaH);

        // Paint chart
        paintChart(aPntr, ins.left, ins.top, areaW, areaH);
    }

    /**
     * Paints chart axis lines.
     */
    protected void paintGridlines(Painter aPntr, double aX, double aY, double aW, double aH)
    {
        //if (!isChartTypeBar()) {
            double tickLenX = getAxisX().getAxis().getTickLength();
            Intervals ivalsX = getIntervalsX();
            for (int i=0, iMax=ivalsX.getCount(); i<iMax; i++) {
                double dataX = ivalsX.getInterval(i);
                double dispX = (int) Math.round(dataToViewX(dataX));
                aPntr.setColor(GRID_LINES_COLOR);
                aPntr.drawLine(dispX, aY, dispX, aH);
                aPntr.setColor(TICK_LINES_COLOR);
                aPntr.drawLine(dispX, aY + aH - tickLenX, dispX, aY + aH);
            }
        //}

        // Draw gridlines lines Y
        double tickLen = getAxisX().getAxis().getTickLength();
        Intervals ivalsY = getIntervalsY();
        for (int i=0, iMax=ivalsY.getCount(); i<iMax; i++) {
            double dataY = ivalsY.getInterval(i);
            double dispY = (int) Math.round(dataToViewY(dataY));
            aPntr.setColor(GRID_LINES_COLOR);
            aPntr.drawLine(aX, dispY, aW, dispY);
            aPntr.setColor(TICK_LINES_COLOR);
            aPntr.drawLine(aX, dispY, aX + tickLen, dispY);
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
            Point pnt = localToParent(anEvent.getX(), anEvent.getY(), _chartView);
            _chartView.setTargPoint(pnt);
        }

        // Handle MouseClick
        if (anEvent.isMouseClick()) {
            DataPoint dpnt = getDataPointForXY(anEvent.getX(), anEvent.getY());
            if (dpnt==_chartView.getSelDataPoint()) dpnt = null;
            _chartView.setSelDataPoint(dpnt);
        }

        // Handle MouseExit
        if (anEvent.isMouseExit())
            _chartView.setTargPoint(null);
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
    protected void resetView()  { }

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