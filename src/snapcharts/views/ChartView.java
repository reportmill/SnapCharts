package snapcharts.views;
import snap.geom.*;
import snap.gfx.Border;
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
public class ChartView<T extends Chart> extends ChartPartView<T> {

    // The Chart
    private Chart  _chart;

    // The view to display chart parts at top of chart
    private HeaderView _chartTop;

    // The view to manage essential chart parts: DataView and AxisViews
    private ChartArea  _chartArea;

    // The Legend
    private LegendView  _legend;
    
    // The ToolTipView
    private ToolTipView  _toolTipView;

    // The selected point
    private DataPoint  _selPoint;

    // The targeted (under mouse) point
    private Point  _targPoint;

    // The targeted data point
    private DataPoint  _targDataPoint;

    // A helper class for layout
    private ChartViewLayout  _layout = new ChartViewLayout(this);

    // The runnable to trigger resetView() before layout/paint
    private Runnable  _resetViewRun;

    // The runnable to trigger resetView() before layout/paint (permanent)
    private Runnable  _resetViewRunShared = () -> { resetView(); _resetViewRun = null; };

    // The PropChangeListener
    private PropChangeListener  _pcl = pc -> chartDidPropChange();
    
    // The DeepChangeListener
    private DeepChangeListener  _dcl = (src,pc) -> chartDidDeepChange(pc);

    // Constants for properties
    public static final String SelDataPoint_Prop = "SelDataPoint";
    public static final String TargPoint_Prop = "TargPoint";
    public static final String TargDataPoint_Prop = "TargDataPoint";

    // Constants
    private static final int CHART_WIDTH = 640;
    private static final int CHART_HEIGHT = 480;

    /**
     * Creates a ChartView.
     */
    public ChartView()
    {
        // Create new chart
        setChart(new Chart());

        // Configure this view
        setGrowWidth(true);

        // Create/add ChartTop
        _chartTop = new HeaderView(this);
        addChild(_chartTop);

        // Create/configure ChartLegend
        _legend = new LegendView();
        addChild(_legend);

        // Create/add ChartArea
        _chartArea = new ChartArea(this);
        addChild(_chartArea.getAxisX());
        addChild(_chartArea.getAxisY());
        addChild(_chartArea);

        // Create ToolTipView
        _toolTipView = new ToolTipView(this);

        // Set sample values
        //setTitle("Sample Growth by Sector, 2012-2018");
        getDataSetList().addDataSetForNameAndValues("Sample", 1d, 2d, 2d, 3d, 4d, 5d);
        resetLater();
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return (T) getChart(); }

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
     * Returns the ChartArea.
     */
    public ChartArea getChartArea()  { return _chartArea; }

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
        // Do normal version
        super.resetView();

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
        DataPoint dataPoint = aPoint!=null ? getDataPointForXY(aPoint.x, aPoint.y) : null;
        setTargDataPoint(dataPoint);
    }

    /**
     * Returns whether to show TargDataPoint.
     */
    public boolean isShowTargDataPoint()
    {
        if (getTargDataPoint()==null)
            return false;
        if (getDataView() instanceof DataViewPanZoom && ((DataViewPanZoom)getDataView()).isZoomSelectMode())
            return false;
        return true;
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
     * Returns the data point for given X/Y.
     */
    public DataPoint getDataPointForXY(double aX, double aY)
    {
        DataView dataView = getDataView();
        Point pnt = dataView.parentToLocal(aX, aY, this);
        return dataView.getDataPointForXY(pnt.x, pnt.y);
    }

    /**
     * Returns the given data point X/Y in this view coords.
     */
    public Point getDataPointXYLocal(DataPoint aDP)
    {
        DataView dataView = getDataView();
        Point pnt = dataView.getDataPointXYLocal(aDP);
        return dataView.localToParent(pnt.x, pnt.y, this);
    }

    /**
     * Override to handle optional rounding radius.
     */
    public Shape getBoundsShape()
    {
        return new RoundRect(0,0, getWidth(), getHeight(), 8);
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
        return 640;
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        // Return proportional height for given width, to retain aspect ratio
        return aW>0 ? Math.round(aW*CHART_HEIGHT/CHART_WIDTH) : CHART_HEIGHT;
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        _layout.layoutChart();
    }
}