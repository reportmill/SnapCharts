package snapcharts.view;
import snap.geom.*;
import snap.util.DeepChangeListener;
import snap.util.PropChange;
import snap.util.PropChangeListener;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.model.*;
import snapcharts.viewx.EmptyChartHelper;

/**
 * A view to render a chart.
 */
public class ChartView<T extends Chart> extends ChartPartView<T> {

    // The Chart
    private T  _chart;

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The view to display chart parts at top of chart
    private HeaderView _headerView;

    // The view to manage essential chart parts: DataView and AxisViews
    private DataView _dataView;

    // The Legend
    private LegendView _legendView;

    // The view that shows contour scale
    private ContourAxisView _contourView;

    // The amount of the chart to show horizontally (0-1)
    private double  _reveal = 1;

    // The ToolTipView
    private ToolTipView  _toolTipView;

    // The selected point
    private DataPoint  _selPoint;

    // The targeted (under mouse) point
    private Point  _targPoint;

    // The targeted data point
    private DataPoint  _targDataPoint;

    // A helper class for layout
    protected ChartViewLayout  _layout = new ChartViewLayout(this);

    // The runnable to trigger resetView() before layout/paint
    private Runnable  _resetViewRun;

    // The runnable to trigger resetView() before layout/paint (permanent)
    private Runnable  _resetViewRunShared = () -> { resetView(); _resetViewRun = null; };

    // The PropChangeListener
    private PropChangeListener  _pcl = pc -> chartDidPropChange();
    
    // The DeepChangeListener
    private DeepChangeListener  _dcl = (src,pc) -> chartDidDeepChange(pc);

    // Constants for properties
    public static String   Reveal_Prop = "Reveal";
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
        // Install bogus ChartHelper
        _chartHelper = new EmptyChartHelper(this);

        // Create new chart
        setChart(new Chart());

        // Configure this view
        setGrowWidth(true);

        // Create/add HeaderView
        _headerView = new HeaderView();
        addChild(_headerView);

        // Create/configure LegendView
        _legendView = new LegendView();
        addChild(_legendView);

        // Create/configure ContourView
        _contourView = new ContourAxisView();
        addChild(_contourView);

        // Create/add DataView
        _dataView = new DataView(this);
        addChild(_dataView);

        // Create ToolTipView
        _toolTipView = new ToolTipView(this);

        // Set sample values
        //setTitle("Sample Growth by Sector, 2012-2018");
        //getDataSetList().addDataSetForNameAndValues("Sample", 1d, 2d, 2d, 3d, 4d, 5d);
        resetLater();
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return _chart; }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()  { return this; }

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _chart; }

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
        _chart = (T) aChart;

        // Start listening to new chart
        _chart.addPropChangeListener(_pcl);
        _chart.addDeepChangeListener(_dcl);

        // Reset
        resetLater();
    }

    /**
     * Returns the ChartHelper.
     */
    public ChartHelper getChartHelper()  { return _chartHelper; }

    /**
     * Sets the ChartHelper.
     */
    protected void setChartHelper(ChartHelper aChartHelper)
    {
        // If already set, just return
        if (aChartHelper == _chartHelper) return;

        // Clean up old
        if (_chartHelper != null) {

            // Deactivate
            _chartHelper.deactivate();
            _chartHelper.removeAxisViews();
        }

        // Set new
        _chartHelper = aChartHelper;

        // Set layout
        _layout = _chartHelper.createLayout();

        // Add DataAreas
        _dataView.setDataAreas(_chartHelper.getDataAreas());

        // Activate
        _chartHelper.activate();

        // Trigger animate (after delay so size is set for first time)
        setReveal(0);
        ViewUtils.runLater(() -> animate());
    }

    /**
     * Returns the dataset.
     */
    public DataSetList getDataSetList()
    {
        return _chartHelper.getDataSetList();
    }

    /**
     * Returns the HeaderView.
     */
    public HeaderView getHeaderView()  { return _headerView; }

    /**
     * Returns the DataView.
     */
    public DataView getDataView()  { return _dataView; }

    /**
     * Returns the LegendView.
     */
    public LegendView getLegendView()  { return _legendView; }

    /**
     * Returns the ContourView.
     */
    public ContourAxisView getContourView()  { return _contourView; }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Make sure if ChartHelper is right
        ChartHelper chartHelper = getChartHelper();
        if (chartHelper==null || getChart().getType() != chartHelper.getChartType()) {
            chartHelper = ChartHelper.createChartHelper(this);
            setChartHelper(chartHelper);
        }

        // Do normal version
        super.resetView();

        // Reset ChartTop
        _headerView.resetView();

        // Reset DataView
        _dataView.resetView();

        // Reset Legend
        _legendView.resetView();

        // Reset Contour
        _contourView.resetView();

        // Reset ChartHelper
        chartHelper.resetView();
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
        if (getDataView().isZoomSelectMode())
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
     * Return the ratio of the chart to show horizontally.
     */
    public double getReveal()  { return _reveal; }

    /**
     * Sets the reation of the chart to show horizontally.
     */
    public void setReveal(double aValue)
    {
        _reveal = aValue;
        _dataView.setReveal(aValue);
        _dataView.repaint();
    }

    /**
     * Returns the time in milliseconds recommended for animation.
     */
    protected int getRevealTime()
    {
        return _dataView.getRevealTime();
    }

    /**
     * Registers for animation.
     */
    public void animate()
    {
        setReveal(0);
        int revealTime = getRevealTime();
        getAnimCleared(revealTime).setValue(Reveal_Prop,1).setLinear().play();
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
     * Override to handle optional rounding radius.
     */
    public Shape getBoundsShape()
    {
        return new RoundRect(0,0, getWidth(), getHeight(), 4);
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
        // Forward to ChartHelper
        _chartHelper.chartPartDidChange(aPC);

        // Trigger reset
        resetLater();
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