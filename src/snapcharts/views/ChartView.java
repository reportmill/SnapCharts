package snapcharts.views;
import snap.geom.*;
import snap.gfx.*;
import snap.util.DeepChangeListener;
import snap.util.PropChange;
import snap.util.PropChangeListener;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;

/**
 * A view to render a chart.
 */
public class ChartView extends ChartPartView {

    // The Chart
    private Chart  _chart;

    // The view to display chart parts at top of chart
    private HeaderView _chartTop;

    // The view to manage essential chart parts: DataView and AxisViews
    private ChartArea  _chartArea;

    // The Legend
    private LegendView  _legend;
    
    // The view to hold ChartArea and Legend
    private RowView  _rowView;

    // The ToolTipView
    private ToolTipView  _toolTipView;

    // The selected point
    private DataPoint  _selPoint;

    // The targeted (under mouse) point
    private Point  _targPoint;

    // The targeted data point
    private DataPoint  _targDataPoint;

    // The runnable to trigger resetView() before layout/paint
    private Runnable  _resetViewRun;

    // The runnable to trigger resetView() before layout/paint (permanent)
    private Runnable  _resetViewRunShared = () -> { resetView(); _resetViewRun = null; };

    // The PropChangeListener
    private PropChangeListener  _pcl = pc -> chartDidPropChange();
    
    // The DeepChangeListener
    private DeepChangeListener  _dcl = (src,pc) -> chartDidDeepChange(pc);

    // Constants
    public static final String SelDataPoint_Prop = "SelDataPoint";
    public static final String TargPoint_Prop = "TargPoint";
    public static final String TargDataPoint_Prop = "TargDataPoint";

    /**
     * Creates a ChartView.
     */
    public ChartView()
    {
        // Create new chart
        setChart(new Chart());

        // Configure this view
        setPadding(10,10,10,10);
        setAlign(Pos.CENTER);
        setGrowWidth(true);
        setFill(Color.WHITE);

        // Create/add ChartTop
        _chartTop = new HeaderView(this);
        addChild(_chartTop);

        // Create RowView
        _rowView = new RowView();
        _rowView.setAlign(Pos.CENTER_LEFT);
        _rowView.setSpacing(8);
        _rowView.setGrowWidth(true);
        _rowView.setGrowHeight(true);
        addChild(_rowView);

        // Create/add ChartArea
        _chartArea = new ChartArea(this);
        _rowView.addChild(_chartArea);

        // Create/configure ChartLegend
        _legend = new LegendView();
        _rowView.addChild(_legend);

        // Create ToolTipView
        _toolTipView = new ToolTipView(this);

        // Set sample values
        //setTitle("Sample Growth by Sector, 2012-2018");
        getDataSetList().addDataSetForNameAndValues("Sample", 1d, 2d, 2d, 3d, 4d, 5d);
        resetLater();
    }

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()  { return this; }

    /**
     * Sets the Chart.
     */
    public void setChart(Chart aChart)
    {
        // If already set, just return
        if (aChart==_chart) return;

        // Stop listening to old chart
        if (_chart!=null) {
            _chart.removePropChangeListener(_pcl);
            _chart.removeDeepChangeListener(_dcl);
        }

        // Set Chart
        _chart = aChart;

        // Start listening to new chart
        _chart.addPropChangeListener(_pcl);
        _chart.addDeepChangeListener(_dcl);

        // Reset
        resetLater();
    }

    /**
     * Returns the dataset.
     */
    public DataSetList getDataSetList()  { return getChart().getDataSetList(); }

    /**
     * Returns the Header View.
     */
    public HeaderView getHeader()  { return _chartTop; }

    /**
     * Returns the X Axis View.
     */
    public AxisViewX getAxisX()  { return _chartArea.getAxisX(); }

    /**
     * Returns the Y Axis View.
     */
    public AxisViewY getAxisY()  { return _chartArea.getAxisY(); }

    /**
     * Returns the DataView.
     */
    public DataView getDataView()  { return _chartArea.getDataView(); }

    /**
     * Returns the Legend.
     */
    public LegendView getLegend()  { return _legend; }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Reset ChartTop
        _chartTop.resetView();

        // Reset ChartArea
        _chartArea.resetView();

        // Reset Legend
        _legend.resetView();
    }

    /**
     * Registers view to reset later.
     */
    public void resetLater()
    {
        if (_resetViewRun!=null) return;
        ViewUpdater updater = getUpdater();
        if (updater!=null)
            updater.runBeforeUpdate(_resetViewRun = _resetViewRunShared);
    }

    @Override
    protected void setShowing(boolean aValue)
    {
        if (aValue==isShowing()) return; super.setShowing(aValue);
        if (aValue)
            resetLater();
    }

    /**
     * Returns the selected data point.
     */
    public DataPoint getSelDataPoint()  { return _selPoint; }

    /**
     * Sets the selected data point.
     */
    public void setSelDataPoint(DataPoint aDP)
    {
        if (SnapUtils.equals(aDP, _selPoint)) return;
        firePropChange(SelDataPoint_Prop, _selPoint, _selPoint = aDP);
        repaint();
    }

    /**
     * Returns the targeted display point (mouse over location).
     */
    public Point getTargPoint()  { return _targPoint; }

    /**
     * Sets the targeted display point (mouse over location).
     */
    public void setTargPoint(Point aPoint)
    {
        // If already set, just return
        if (SnapUtils.equals(aPoint, _targPoint)) return;

        // Set and firePropChange
        firePropChange(TargPoint_Prop, _targPoint, _targPoint = aPoint);

        // Update TargDataPoint
        DataPoint dataPoint = aPoint!=null ? getTargDataPointForXY(aPoint.x, aPoint.y) : null;
        setTargDataPoint(dataPoint);

        // This is a hack
        if (getDataView() instanceof DataViewPie)
            _toolTipView.reloadContents();
    }

    /**
     * Returns the targeted data point.
     */
    public DataPoint getTargDataPoint()  { return _targDataPoint; }

    /**
     * Sets the targeted data point.
     */
    public void setTargDataPoint(DataPoint aDP)
    {
        // If already set, just return
        if (SnapUtils.equals(aDP, _targDataPoint)) return;

        // Set and firePropChange
        firePropChange(TargDataPoint_Prop, _targDataPoint, _targDataPoint = aDP);

        // Notify ToolTipView
        _toolTipView.reloadContents();
    }

    /**
     * Returns the targeted data point.
     */
    public DataPoint getTargDataPointForXY(double aX, double aY)
    {
        DataView dataView = getDataView();
        Point pnt = dataView.parentToLocal(aX, aY, this);
        return dataView.getDataPointForXY(pnt.x, pnt.y);
    }

    /**
     * Returns the targeted data point.
     */
    public Point getTargDataPointXY()
    {
        // Get DataPoint
        DataPoint dataPoint = getTargDataPoint(); if (dataPoint==null) return null;
        double dataX = dataPoint.getX();
        double dataY = dataPoint.getY();

        // Get DataView - Handle Pie special - but really need to return center of wedge
        DataView dataView = getDataView();
        if (dataView instanceof DataViewPie)
            return getTargPoint();

        // Convert dataPoint XY to DataView and return
        Point pnt = dataView.dataToView(dataX, dataY);
        return dataView.localToParent(pnt.x, pnt.y, this);
    }

    /**
     * Called when Chart has a PropChange.
     */
    protected void chartDidPropChange()
    {
        resetLater();
    }

    /**
     * Called when Chart has a DeppChange.
     */
    protected void chartDidDeepChange(PropChange aPC)
    {
        resetLater();

        // If DataSet change, clear caches
        Object src = aPC.getSource();
        if (src instanceof DataSet || src instanceof DataSetList) {
            getDataView().clearCache();
        }
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        return ColView.getPrefWidth(this, aH);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return ColView.getPrefHeight(this, aW);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        ColView.layout(this, false);
    }
}