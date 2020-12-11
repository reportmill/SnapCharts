package snapcharts.views;
import snap.geom.*;
import snap.view.ColView;
import snap.view.RowView;
import snap.view.ViewProxy;
import java.util.Arrays;

/**
 * A class to layout ChartView component parts (Header, Legend, Axes, DataView).
 */
public class ChartViewLayout {

    // The Chart that owns this layout
    private ChartView  _chartView;

    // The preferred DataArea bounds (optional)
    private Rect  _prefDataAreaBounds;

    // The layout proxy
    private ViewProxy<ChartView>  _chartProxy;

    // The header proxy
    private ViewProxy<HeaderView>  _headerProxy;

    // The legend proxy
    private ViewProxy<LegendView>  _legendProxy;

    // The DataArea proxy
    private ViewProxy<ChartArea>  _dataAreaProxy;

    // The current inset for DataArea
    protected Insets _dataAreaInsets = new Insets(0, 0, 0, 0);

    // The resulting DataArea bounds
    protected Rect  _dataAreaBounds;

    // The maximum size that a side can be as ratio of plot size ( 30% )
    private final double MAX_SIDE_RATIO = .3;

    // The default side padding size
    private final int SIDE_MARGIN = 10;

    // The default spacing between views
    private final int VIEW_SPACING = 8;

    // The default min right margin size
    private final int RIGHT_MARGIN_MIN = 30;

    /**
     * Constructor
     */
    public ChartViewLayout(ChartView aChartView)
    {
        _chartView = aChartView;
    }

    /**
     * Initializes chart proxies.
     */
    private void init()
    {
        _chartProxy = new ViewProxy<>(_chartView);
        _chartProxy.setChildren(ViewProxy.getProxies(_chartView.getChildren()));
        _headerProxy = _chartProxy.getChildForClass(HeaderView.class);
        _legendProxy = _chartProxy.getChildForClass(LegendView.class);
        _dataAreaProxy = _chartProxy.getChildForClass(ChartArea.class);
    }

    /**
     * Top level layout.
     */
    public void layoutChart()
    {
        init();

        // Layout parts
        layoutSideTop();
        layoutSideBottom();
        layoutSideLeft();
        layoutSideRight();

        // Set DataAreaBounds
        _dataAreaBounds = _chartProxy.getBounds().clone();
        _dataAreaBounds.inset(_dataAreaInsets);

        // Set DataArea.Bounds
        _dataAreaProxy.setBounds(_dataAreaBounds);

        // Adjust axes
        for (ViewProxy child : _chartProxy.getChildren()) {
            if (child.getView() instanceof AxisViewX) {
                child.setX(_dataAreaBounds.x);
                child.setWidth(_dataAreaBounds.width);
            }
            if (child.getView() instanceof AxisViewY) {
                child.setY(_dataAreaBounds.y);
                child.setHeight(_dataAreaBounds.height);
            }
        }

        // Copy back to views
        _chartProxy.setBoundsInClient();
    }

    /**
     * Layout of top views: Header, legend (if top), top axis (if polar?).
     */
    protected void layoutSideTop()
    {
        // Get views and bounds above DataArea
        ViewProxy<?>[] sideViews = getViewsForSide(Side.TOP);
        Rect sideBounds = getBoundsForSide(Side.TOP);

        // Create temp proxy for layout
        ViewProxy<?> sideProxy = new ViewProxy<>(_chartView);
        sideProxy.setSize(sideBounds.width, sideBounds.height);
        sideProxy.setAlign(Pos.TOP_CENTER);
        sideProxy.setChildren(sideViews);
        sideProxy.setInsets(new Insets(SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN));
        sideProxy.setSpacing(VIEW_SPACING);

        // Layout ColView
        ColView.layoutProxy(sideProxy, true);

        // Update insets
        ViewProxy<?> sideViewN = sideViews[sideViews.length-1];
        double maxY = sideViewN.getMaxY() + VIEW_SPACING;
        _dataAreaInsets.top = maxY - _chartProxy.getY();
    }

    /**
     * Layout of bottom views: X Axis, legend (if bottom).
     */
    protected void layoutSideBottom()
    {
        // Get views and bounds below DataArea
        ViewProxy<?>[] sideViews = getViewsForSide(Side.BOTTOM);
        Rect sideBounds = getBoundsForSide(Side.BOTTOM);
        ViewProxy sideView0 = sideViews.length > 0 ? sideViews[0] : null;
        double sideView0Margin = sideView0!=null && sideView0.getView() instanceof AxisViewX ? 0 : SIDE_MARGIN;

        // Make SideView0 shift down if needed
        if (sideView0!=null)
            sideView0.getView().setLeanY(VPos.BOTTOM);

        // Create temp proxy for layout
        ViewProxy<?> sideProxy = new ViewProxy<>(_chartView);
        sideProxy.setSize(sideBounds.width, sideBounds.height);
        sideProxy.setAlign(Pos.BOTTOM_CENTER);
        sideProxy.setChildren(sideViews);
        sideProxy.setInsets(new Insets(sideView0Margin, SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN));
        sideProxy.setSpacing(VIEW_SPACING);

        // Layout ColView
        ColView.layoutProxy(sideProxy, true);

        // Shift views to sideBounds origin y
        for (ViewProxy proxy : sideViews)
            proxy.setY(proxy.getY() + sideBounds.y);

        // Update insets
        double sideView0Y = sideView0!=null ? sideView0.getY() : 0;
        if (sideView0!=null && !(sideView0.getView() instanceof AxisView)) sideView0Y -= VIEW_SPACING;
        _dataAreaInsets.bottom = _chartProxy.getMaxY() - sideView0Y;
    }

    /**
     * Layout of left views: Legend (if left), Y/Y3 axis (when available).
     */
    protected void layoutSideLeft()
    {
        // Get views and bounds above DataArea
        ViewProxy<?>[] sideViews = getViewsForSide(Side.LEFT);
        Rect sideBounds = getBoundsForSide(Side.LEFT);

        // Create temp proxy for layout
        ViewProxy<?> sideProxy = new ViewProxy<>(_chartView);
        sideProxy.setSize(sideBounds.width, sideBounds.height);
        sideProxy.setAlign(Pos.CENTER_LEFT);
        sideProxy.setChildren(sideViews);
        sideProxy.setInsets(new Insets(SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN));
        sideProxy.setSpacing(VIEW_SPACING);

        // Layout RowView
        RowView.layoutProxy(sideProxy, true);

        // Update insets
        ViewProxy<?> topViewN = sideViews.length > 0 ? sideViews[sideViews.length-1] : null;
        double maxX = topViewN.getMaxX() + VIEW_SPACING;
        _dataAreaInsets.left = maxX - _chartProxy.getX();
    }

    /**
     * Layout of right views: Y2/Y4 axis (when available), Legend (if right side).
     */
    protected void layoutSideRight()
    {
        // Get views and bounds above DataArea
        ViewProxy<?>[] sideViews = getViewsForSide(Side.RIGHT);
        Rect sideBounds = getBoundsForSide(Side.RIGHT);

        // Create temp proxy for layout
        ViewProxy<?> sideProxy = new ViewProxy<>(_chartView);
        sideProxy.setSize(sideBounds.width, sideBounds.height);
        sideProxy.setAlign(Pos.CENTER_RIGHT);
        sideProxy.setChildren(sideViews);
        sideProxy.setInsets(new Insets(SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN));
        sideProxy.setSpacing(VIEW_SPACING);

        // Layout RowView
        RowView.layoutProxy(sideProxy, true);

        // Shift views to sideBounds origin x
        for (ViewProxy proxy : sideViews)
            proxy.setX(proxy.getX() + sideBounds.x);

        // Update insets
        ViewProxy<?> sideView0 = sideViews.length > 0 ? sideViews[0] : null;
        double maxX = sideView0!=null ? sideView0.getX() : _chartProxy.getMaxX();
        _dataAreaInsets.right = Math.max(_chartProxy.getMaxX() - maxX, RIGHT_MARGIN_MIN);
    }

    /**
     * Returns the array of views for given side.
     */
    protected ViewProxy<?>[] getViewsForSide(Side aSide)
    {
        // Get Axes
        ViewProxy[] axes = getAxesForSide(aSide);
        ViewProxy axis1 = axes.length > 0 ? axes[0] : null;
        ViewProxy axis2 = axes.length > 1 ? axes[1] : null;

        // Get Legend if on given side.
        boolean hasLegend = _legendProxy.isVisible() && _legendProxy.getView().getPosition().getSide() == aSide;
        ViewProxy<?> legend = hasLegend ? _legendProxy : null;
        if (hasLegend && _prefDataAreaBounds!=null) {
            legend.setGrowWidth(aSide==Side.LEFT || aSide==Side.RIGHT);
            legend.setGrowHeight(aSide==Side.TOP || aSide==Side.BOTTOM);
        }

        // Handle sides
        switch (aSide)
        {
            case TOP: return getNonNullArray(_headerProxy, legend);
            case BOTTOM: return getNonNullArray(axis1, legend);
            case LEFT: return getNonNullArray(legend, axis1, axis2);
            case RIGHT: return getNonNullArray(axis1, axis2, legend);
            default: throw new RuntimeException("ChartViewLayout.getViewsForSide: Unknown side: " + aSide);
        }
    }

    /**
     * Returns the axes for given side.
     */
    public ViewProxy[] getAxesForSide(Side aSide)
    {
        // Handle sides
        switch (aSide)
        {
            case TOP: return new ViewProxy[0];
            case BOTTOM: return new ViewProxy[] { _chartProxy.getChildForClass(AxisViewX.class) };
            case LEFT: return new ViewProxy[] { _chartProxy.getChildForClass(AxisViewY.class) };
            case RIGHT: return new ViewProxy[0];
            default: throw new RuntimeException("ChartViewLayout.getAxesForSide: Unknown side: " + aSide);
        }
    }

    /**
     * Returns the available bounds available on given side.
     */
    protected Rect getBoundsForSide(Side aSide)
    {
        // If PrefDataAreaBounds set, return bounds for side using that
        if (_prefDataAreaBounds!=null)
            return getBoundsForSideFixed(aSide);

        // Return bounds for side (size is MAX_SIDE_RATIO of chart size)
        Rect bounds = new Rect(0, 0, _chartProxy.getWidth(), _chartProxy.getHeight());
        switch (aSide)
        {
            case TOP:
                bounds.height = Math.round(MAX_SIDE_RATIO * _chartProxy.getHeight());
                break;
            case BOTTOM:
                double maxY = bounds.getMaxY();
                bounds.height = Math.round(MAX_SIDE_RATIO * _chartProxy.getHeight());
                bounds.y = maxY - bounds.height;
                break;
            case LEFT:
                bounds.width = Math.round(MAX_SIDE_RATIO * _chartProxy.getWidth());
                break;
            case RIGHT:
                double maxX = bounds.getMaxX();
                bounds.width = Math.round(MAX_SIDE_RATIO * _chartProxy.getWidth());
                bounds.x = maxX - bounds.width;
                break;
            default: throw new RuntimeException("ChartViewLayout.getBoundsForSide: Unknown side" + aSide);
        }

        return bounds;
    }

    /**
     * Returns the available bounds on given side for when PrefDataAreaBounds is set.
     */
    protected Rect getBoundsForSideFixed(Side aSide)
    {
        Rect bounds = new Rect(0, 0, _chartProxy.getWidth(), _chartProxy.getHeight());
        switch (aSide)
        {
            case TOP:
                bounds.height = _prefDataAreaBounds.y - bounds.y;
                break;
            case BOTTOM:
                bounds.height = bounds.getMaxY() - _prefDataAreaBounds.getMaxY();
                bounds.y = _prefDataAreaBounds.getMaxY();
                break;
            case LEFT:
                bounds.width = _prefDataAreaBounds.x - bounds.x;
                break;
            case RIGHT:
                bounds.width = bounds.getMaxX() - _prefDataAreaBounds.getMaxX();
                bounds.x = _prefDataAreaBounds.getMaxX();
                break;
            default: throw new RuntimeException("ChartViewLayout.getBoundsForSideFixed: Unknown side: " + aSide);
        }
        return bounds;
    }

    /**
     * Returns array of non-null components.
     */
    private static <T> T[] getNonNullArray(T ... theItems)
    {
        int len = 0; for (T item : theItems) if (item!=null) len++;
        if (len==theItems.length) return theItems;
        T[] items = Arrays.copyOf(theItems, len);
        for (int i=0, j=0; i<theItems.length; i++)
            if (theItems[i]!=null) items[j++] = theItems[i];
        return items;
    }
}
