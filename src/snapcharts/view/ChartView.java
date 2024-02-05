package snapcharts.view;
import snap.geom.*;
import snap.props.DeepChangeListener;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.util.*;
import snap.view.*;
import snapcharts.app.AppEnv;
import snapcharts.charts.*;
import snapcharts.viewx.EmptyChartHelper;
import java.util.Objects;

/**
 * A view to render a chart.
 */
public class ChartView extends ChartPartView<Chart> {

    // The Chart
    private Chart  _chart;

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The view to display chart parts at top of chart
    private HeaderView  _headerView;

    // The view to manage essential chart parts: ContentView and AxisViews
    private ContentView  _contentView;

    // The Legend
    private LegendView  _legendView;

    // The view that shows contour color bar scale
    private ContourAxisView _contourAxisView;

    // The MarkerViews
    private MarkerView[]  _markerViews;

    // The amount of the chart to show horizontally (0-1)
    private double  _reveal = 1;

    // Whether to animate on show
    private boolean  _animateOnShow;

    // The ToolTipView
    private ToolTipView  _toolTipView;

    // The selected point
    private TracePoint _selPoint;

    // The targeted (under mouse) point
    private Point  _targPoint;

    // The targeted data point
    private TracePoint _targDataPoint;

    // The preferred content bounds (optional)
    protected Rect  _prefContentBounds;

    // A helper class for layout
    protected ChartViewLayout  _layout = new ChartViewLayout(this);

    // The runnable to trigger resetView() before layout/paint
    private Runnable  _resetViewRun;

    // The runnable to trigger resetView() before layout/paint (permanent)
    private Runnable  _resetViewRunShared = () -> { resetView(); _resetViewRun = null; };

    // The PropChangeListener
    private PropChangeListener _pcl = pc -> chartDidPropChange(pc);
    
    // The DeepChangeListener
    private DeepChangeListener _dcl = (src, pc) -> chartDidDeepChange(pc);

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
        // Basic
        setBorderRadius(4);
        setFocusable(true);
        setFocusWhenPressed(true);

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

        // Create/configure ContourAxisView
        _contourAxisView = new ContourAxisView();
        addChild(_contourAxisView);

        // Create/add ContentView
        _contentView = new ContentView(this);
        addChild(_contentView);

        // Create ToolTipView
        _toolTipView = new ToolTipView(this);

        // Set sample values
        //setTitle("Sample Growth by Sector, 2012-2018");
        //getContent().addTraceForNameAndValues("Sample", 1d, 2d, 2d, 3d, 4d, 5d);
        resetLater();

        //
        addEventFilter(e -> chartViewDidMouseRelease(e), MouseRelease);
    }

    /**
     * Returns the ChartPart.
     */
    @Override
    public Chart getChartPart()  { return _chart; }

    /**
     * Returns the ChartView.
     */
    @Override
    public ChartView getChartView()  { return this; }

    /**
     * Returns the Chart.
     */
    @Override
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
        _chart = aChart;

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

        // Add TraceViews
        _contentView.setTraceViews(_chartHelper.getTraceViews());

        // Activate
        _chartHelper.activate();

        // Animate 3D charts (I can't help myself)
        if (getTraceType().is3D())
            animateOnShow();

        // If AnimateOnShow, reset reveal
        if (_animateOnShow)
            setReveal(0);
    }

    /**
     * Returns the Content.
     */
    public Content getContent()
    {
        return _chartHelper.getContent();
    }

    /**
     * Returns the HeaderView.
     */
    public HeaderView getHeaderView()  { return _headerView; }

    /**
     * Returns the ContentView.
     */
    public ContentView getContentView()  { return _contentView; }

    /**
     * Returns the LegendView.
     */
    public LegendView getLegendView()  { return _legendView; }

    /**
     * Returns the ContourAxisView.
     */
    public ContourAxisView getContourAxisView()  { return _contourAxisView; }

    /**
     * Returns the MarkerViews.
     */
    public MarkerView[] getMarkerViews()
    {
        // If already set, just return
        if (_markerViews != null) return _markerViews;

        // Get array of markers and create array for MarkerViews
        Chart chart = getChart();
        Marker[] markers = chart.getMarkers();
        MarkerView[] markerViews = new MarkerView[markers.length];

        // Iterate over Markers and create MarkerView for each
        for (int i = 0; i < markers.length; i++) {
            Marker marker = markers[i];
            MarkerView markerView = markerViews[i] = new MarkerView(marker);
            addChild(markerView);
        }

        // Set and return
        return _markerViews = markerViews;
    }

    /**
     * Called to reset view from Chart.
     */
    protected void resetView()
    {
        // Make sure if ChartHelper is right
        ChartHelper chartHelper = getChartHelper();
        if (chartHelper == null || getTraceType() != chartHelper.getTraceType()) {
            chartHelper = ChartHelper.createChartHelper(this);
            setChartHelper(chartHelper);
        }

        // Do normal version
        super.resetView();

        // Reset ChartTop
        _headerView.resetView();

        // Reset ContentView
        _contentView.resetView();

        // Reset Legend
        _legendView.resetView();

        // Reset ContourAxis
        _contourAxisView.resetView();

        // Reset MarkerViews
        MarkerView[] markerViews = getMarkerViews();
        for (MarkerView markerView : markerViews)
            markerView.resetView();

        // Reset ChartHelper
        chartHelper.resetView();

        // Trigger animate on show
        if (_animateOnShow && isShowing())
            animate();
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

    /**
     * Override to trigger reset on showing
     */
    @Override
    protected void setShowing(boolean aValue)
    {
        // Do normal version
        if (aValue==isShowing()) return;
        super.setShowing(aValue);

        // If Showing, trigger reset
        if (aValue) {
            resetLater();
        }
    }

    /**
     * Returns the selected data point.
     */
    public TracePoint getSelDataPoint()  { return _selPoint; }

    /**
     * Sets the selected data point.
     */
    public void setSelDataPoint(TracePoint aDP)
    {
        if (Objects.equals(aDP, _selPoint)) return;
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
        if (Objects.equals(aPoint, _targPoint)) return;

        // Set and firePropChange
        firePropChange(TargPoint_Prop, _targPoint, _targPoint = aPoint);

        // Update TargDataPoint
        TracePoint dataPoint = null;
        if (aPoint != null)
            dataPoint = _chartHelper.getDataPointForViewXY(this, aPoint.x, aPoint.y);
        setTargDataPoint(dataPoint);
    }

    /**
     * Returns whether to show TargDataPoint.
     */
    public boolean isShowTargDataPoint()
    {
        if (getTargDataPoint() == null)
            return false;
        if (getChartHelper().isZoomSelectMode())
            return false;
        return true;
    }

    /**
     * Returns the targeted data point.
     */
    public TracePoint getTargDataPoint()  { return _targDataPoint; }

    /**
     * Sets the targeted data point.
     */
    public void setTargDataPoint(TracePoint aDP)
    {
        // If already set, just return
        if (Objects.equals(aDP, _targDataPoint)) return;

        // Set and firePropChange
        firePropChange(TargDataPoint_Prop, _targDataPoint, _targDataPoint = aDP);

        // Notify ToolTipView
        _toolTipView.reloadContents();
        repaint();
    }

    /**
     * Returns the preferred bounds for the ContentView.
     */
    public Rect getPrefContentBounds()  { return _prefContentBounds; }

    /**
     * Sets the preferred bounds for the ContentView.
     */
    public void setPrefContentBounds(Rect aRect)
    {
        if (Objects.equals(aRect, _prefContentBounds)) return;
        _prefContentBounds = aRect;
        relayout();
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
        _contentView.setReveal(aValue);

        // Repaint ContentView (expanded so adjacent AxisViews will repaint axis line and ticks)
        _contentView.repaint(-20, -20, _contentView.getWidth() + 40, _contentView.getHeight() + 40);
    }

    /**
     * Returns the time in milliseconds recommended for animation.
     */
    protected int getRevealTime()
    {
        return _contentView.getRevealTime();
    }

    /**
     * Registers for animation.
     */
    public void animate()
    {
        // Trigger animate (after delay so size is set for first time)
        setReveal(0);
        ViewUtils.runLater(() -> animateImpl());
        _animateOnShow = false;
    }

    /**
     * Registers for animation.
     */
    private void animateImpl()
    {
        setReveal(0);
        int revealTime = getRevealTime();
        getAnimCleared(revealTime).setValue(Reveal_Prop,1).setLinear().play();
    }

    /**
     * Whether to animate on show.
     */
    public void animateOnShow()
    {
        _animateOnShow = true;
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName.equals(Reveal_Prop)) return getReveal();
        return super.getPropValue(aPropName);
    }

    /**
     * Sets the value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        if (aPropName.equals(Reveal_Prop)) setReveal(Convert.doubleValue(aValue));
        else super.setPropValue(aPropName, aValue);
    }

    /**
     * Called when Chart has a PropChange.
     */
    protected void chartDidPropChange(PropChange aPC)
    {
        String propName = aPC.getPropName();
        if (propName == Chart.Markers_Prop) {
            _markerViews = null;
            relayout();
        }

        // Trigger reset
        resetLater();
    }

    /**
     * Called when Chart has a DeppChange.
     */
    protected void chartDidDeepChange(PropChange aPC)
    {
        // Forward to ChartHelper
        _chartHelper.chartPartDidChange(aPC);

        // Handle PointStyle, TagStyle: Repaint
        Object src = aPC.getSource();
        if (src instanceof PointStyle || src instanceof TagStyle)
            repaint();

        // Handle Marker change
        if (aPC.getSource() instanceof Marker) {
            relayout();
        }

        // Trigger reset
        resetLater();
    }

    /**
     * Override to return default chart width.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        return CHART_WIDTH;
    }

    /**
     * Override to return default chart height (or proportional height if given width).
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        // Return proportional height for given width, to retain aspect ratio
        double prefH = CHART_HEIGHT;
        if (aW > 0)
            prefH = Math.round(aW * CHART_HEIGHT / CHART_WIDTH);
        return prefH;
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        // Get whether this is first pass
        ContentView contentView = getContentView();
        boolean firstPass = contentView.getWidth() == 0 && contentView.getHeight() == 0;

        _layout.layoutChart();

        // If first pass, try again so AxisView can do a better job of getting intervals
        if (firstPass)
            _layout.layoutChart();

        layoutMarkers();
    }

    /**
     * Does layout for Chart Markers.
     */
    protected void layoutMarkers()
    {
        MarkerView[] markerViews = getMarkerViews();
        for (MarkerView markerView : markerViews) {
            Rect bnds = markerView.getMarkerBoundsInChartViewCoords();
            markerView.setBounds(bnds);
            markerView.resetView();
        }
    }

    /**
     * Debug feature to open chart in browser.
     */
    private void chartViewDidMouseRelease(ViewEvent anEvent)
    {
        if (anEvent.getX() > getWidth() - 90 && anEvent.getY() < 90 && anEvent.isShiftDown()) {
            AppEnv.getEnv().openChartInBrowser(getChart(), anEvent.isControlDown());
        }
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        String str = "ChartView { Type:" + getTraceType();
        str += ", Bounds:" + getBounds();
        return str + " }";
    }
}