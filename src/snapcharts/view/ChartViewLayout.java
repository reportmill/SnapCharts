package snapcharts.view;
import snap.geom.*;
import snap.view.ColView;
import snap.view.RowView;
import snap.view.ViewProxy;
import snapcharts.viewx.PolarChartHelper;

import java.util.Arrays;

/**
 * A class to layout ChartView component parts (Header, Legend, Axes, DataView).
 */
public class ChartViewLayout {

    // The Chart that owns this layout
    protected ChartView  _chartView;

    // The preferred DataArea bounds (optional)
    protected Rect _prefDataBounds;

    // The layout proxy
    private ViewProxy<ChartView>  _chartProxy;

    // The header proxy
    private ViewProxy<HeaderView>  _headerProxy;

    // The legend proxy
    private ViewProxy<LegendView>  _legendProxy;

    // The contour proxy
    private ViewProxy<ColorBarView>  _contourProxy;

    // The DataArea proxy
    private ViewProxy<DataView>  _dataAreaProxy;

    // The current inset for DataArea
    protected Insets _dataAreaInsets = new Insets(0, 0, 0, 0);

    // The resulting DataArea bounds
    protected Rect  _dataAreaBounds;

    // The maximum size that a side can be as ratio of chart size ( 30% )
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
        _headerProxy = _chartProxy.getChildForClass(HeaderView.class);
        _legendProxy = _chartProxy.getChildForClass(LegendView.class);
        _contourProxy = _chartProxy.getChildForClass(ColorBarView.class);
        _dataAreaProxy = _chartProxy.getChildForClass(DataView.class);
    }

    /**
     * Top level layout.
     */
    public void layoutChart()
    {
        init();

        // Layout parts
        layoutTopSide();
        layoutBottomSide();
        layoutLeftSide();
        layoutRightSide();

        // Set DataAreaBounds (either from PrefDataAreaBounds or from resulting layout insets)
        _dataAreaBounds = _prefDataBounds;
        if (_dataAreaBounds==null) {
            _dataAreaBounds = _chartProxy.getBounds().clone();
            _dataAreaBounds.inset(_dataAreaInsets);
        }

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

        // Clear PrefDataAreaBounds
        _prefDataBounds = null;
    }

    /**
     * Layout of top views: Header, legend (if top), top axis (if polar?).
     */
    protected void layoutTopSide()
    {
        // Get views and bounds above DataArea
        ViewProxy<?>[] sideViews = getViewsForSide(Side.TOP);
        Rect sideBounds = getBoundsForSide(Side.TOP);

        // Create temp proxy for layout
        ViewProxy<?> sideProxy = new ViewProxy<>(_chartView);
        sideProxy.setAlign(Pos.BOTTOM_CENTER);
        sideProxy.setChildren(sideViews);
        sideProxy.setSpacing(VIEW_SPACING);

        // Set SideProxy.Insets, with special accommodation for AxisViewX (no inset)
        ViewProxy<?> sideViewN = sideViews.length > 0 ? sideViews[sideViews.length-1] : null;
        double insBottom = sideViewN!=null && sideViewN.getView() instanceof AxisView ? 0 : SIDE_MARGIN;
        sideProxy.setInsets(new Insets(SIDE_MARGIN, SIDE_MARGIN, insBottom, SIDE_MARGIN));

        // Get/set sideHeight for proxy: If no, PrefDataBounds, use proxy PrefHeight
        double sideHeight = sideBounds.height;
        if (_prefDataBounds == null) {
            double prefHeight = ColView.getPrefHeightProxy(sideProxy, sideBounds.width);
            sideHeight = Math.min(prefHeight, sideHeight);
        }
        sideProxy.setSize(sideBounds.width, sideHeight);

        // Layout ColView
        ColView.layoutProxy(sideProxy, true);

        // Update insets
        _dataAreaInsets.top = sideHeight;
    }

    /**
     * Layout of bottom views: X Axis, legend (if bottom).
     */
    protected void layoutBottomSide()
    {
        // Get views and bounds below DataArea
        ViewProxy<?>[] sideViews = getViewsForSide(Side.BOTTOM);
        Rect sideBounds = getBoundsForSide(Side.BOTTOM);

        // Create temp proxy for layout
        ViewProxy<?> sideProxy = new ViewProxy<>(_chartView);
        sideProxy.setAlign(Pos.TOP_CENTER);
        sideProxy.setChildren(sideViews);
        sideProxy.setSpacing(VIEW_SPACING);

        // Set SideProxy.Insets, with special accommodation for AxisViewX (no inset)
        ViewProxy sideView0 = sideViews.length > 0 ? sideViews[0] : null;
        double insTop = sideView0!=null && sideView0.getView() instanceof AxisViewX ? 0 : SIDE_MARGIN;
        sideProxy.setInsets(new Insets(insTop, SIDE_MARGIN, SIDE_MARGIN, SIDE_MARGIN));

        // Get/set sideHeight for proxy: If no, PrefDataBounds, use proxy PrefHeight
        double sideHeight = sideBounds.height;
        if (_prefDataBounds == null) {
            double prefHeight = ColView.getPrefHeightProxy(sideProxy, sideBounds.width);
            prefHeight = Math.max(prefHeight, RIGHT_MARGIN_MIN);
            sideHeight = Math.min(prefHeight, sideHeight);
        }
        sideProxy.setSize(sideBounds.width, sideHeight);

        // Layout ColView
        ColView.layoutProxy(sideProxy, true);

        // Shift views to sideBounds origin y
        double sideY = _chartProxy.getHeight() - sideHeight;
        for (ViewProxy proxy : sideViews)
            proxy.setY(proxy.getY() + sideY);

        // Update insets
        _dataAreaInsets.bottom = sideHeight;
    }

    /**
     * Layout of left views: Legend (if left), Y/Y3 axis (when available).
     */
    protected void layoutLeftSide()
    {
        // Get views and bounds above DataArea
        ViewProxy<?>[] sideViews = getViewsForSide(Side.LEFT);
        Rect sideBounds = getBoundsForSide(Side.LEFT);

        // Create temp proxy for layout
        ViewProxy<?> sideProxy = new ViewProxy<>(_chartView);
        sideProxy.setAlign(Pos.CENTER_RIGHT);
        sideProxy.setChildren(sideViews);
        sideProxy.setSpacing(VIEW_SPACING);

        // Set SideProxy.Insets, with special accommodation for AxisViewY (no inset)
        ViewProxy<?> sideViewN = sideViews.length > 0 ? sideViews[sideViews.length-1] : null;
        double insRight = sideViewN!=null && sideViewN.getView() instanceof AxisView ? 0 : SIDE_MARGIN;
        sideProxy.setInsets(new Insets(_dataAreaInsets.top, insRight, _dataAreaInsets.bottom, SIDE_MARGIN));

        // Get/set sideWidth for proxy: If no, PrefDataBounds, use proxy PrefWidth
        double sideWidth = sideBounds.width;
        if (_prefDataBounds == null) {
            double prefWidth = RowView.getPrefWidthProxy(sideProxy, -1);
            prefWidth = Math.max(prefWidth, RIGHT_MARGIN_MIN);
            sideWidth = Math.min(prefWidth, sideWidth);
        }
        sideProxy.setSize(sideWidth, sideBounds.height);

        // Layout RowView
        RowView.layoutProxy(sideProxy, true);

        // If PrefDataAreaBounds set and left side bounds needed was less than available, slide over
        /*if (_prefDataBounds !=null && sideWidth < sideBounds.width) {
            double shift = sideBounds.width - sideWidth;
            for (ViewProxy proxy : sideViews)
                proxy.setX(proxy.getX() + shift);
        }*/

        // Update insets
        _dataAreaInsets.left = sideWidth;
    }

    /**
     * Layout of right views: Y2/Y4 axis (when available), Legend (if right side).
     */
    protected void layoutRightSide()
    {
        // Get views and bounds above DataArea
        ViewProxy<?>[] sideViews = getViewsForSide(Side.RIGHT);
        Rect sideBounds = getBoundsForSide(Side.RIGHT);

        // Create temp proxy for layout
        ViewProxy<?> sideProxy = new ViewProxy<>(_chartView);
        sideProxy.setAlign(Pos.CENTER_LEFT);
        sideProxy.setChildren(sideViews);
        sideProxy.setSpacing(VIEW_SPACING);

        // Set SideProxy.Insets, with special accommodation for AxisViewY (no inset)
        double insLeft = sideViews.length>0 && (sideViews[0].getView() instanceof AxisView) ? 0 : SIDE_MARGIN;
        sideProxy.setInsets(new Insets(_dataAreaInsets.top, SIDE_MARGIN, _dataAreaInsets.bottom, insLeft));

        // Get/set sideWidth for proxy: If no, PrefDataBounds, use proxy PrefWidth
        double sideWidth = sideBounds.width;
        if (_prefDataBounds == null) {
            double prefWidth = RowView.getPrefWidthProxy(sideProxy, -1);
            prefWidth = Math.max(prefWidth, RIGHT_MARGIN_MIN);
            sideWidth = Math.min(prefWidth, sideWidth);
        }
        sideProxy.setSize(sideWidth, sideBounds.height);

        // Layout RowView
        RowView.layoutProxy(sideProxy, true);

        // Shift views to sideBounds origin x
        double sideX = _chartProxy.getWidth() - sideWidth;
        for (ViewProxy proxy : sideViews)
            proxy.setX(proxy.getX() + sideX);

        // Update insets
        _dataAreaInsets.right = sideWidth;
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
        boolean hasLegend = _legendProxy!=null && _legendProxy.getView().getPosition().getSide() == aSide;
        ViewProxy<?> legend = hasLegend ? _legendProxy : null;
        if (hasLegend) {
            legend.setGrowWidth(aSide==Side.LEFT || aSide==Side.RIGHT);
            legend.setGrowHeight(aSide==Side.TOP || aSide==Side.BOTTOM);
        }

        // Get Contour if on given side.
        boolean hasContour = _contourProxy!=null && _contourProxy.isVisible() && aSide == Side.RIGHT;
        ViewProxy<?> contour = hasContour ? _contourProxy : null;

        // Handle sides
        switch (aSide)
        {
            case TOP: return getNonNullArray(_headerProxy, legend);
            case BOTTOM: return getNonNullArray(axis1, legend);
            case LEFT: return getNonNullArray(legend, axis2, axis1);
            case RIGHT: return getNonNullArray(axis1, axis2, legend, contour);
            default: throw new RuntimeException("ChartViewLayout.getViewsForSide: Unknown side: " + aSide);
        }
    }

    /**
     * Returns the axes for given side.
     */
    public ViewProxy[] getAxesForSide(Side aSide)
    {
        // Handle ChartHelperPolar
        if (_chartView.getChartHelper() instanceof PolarChartHelper)
            return new ViewProxy[0];

        // Handle Top
        if (aSide == Side.TOP)
            return new ViewProxy[0];

        // Handle Bottom
        if (aSide == Side.BOTTOM)
            return new ViewProxy[] { _chartProxy.getChildForClass(AxisViewX.class) };

        // Handle LEFT/RIGHT
        ViewProxy<AxisViewY>[] axisViews = _chartProxy.getChildrenForClass(AxisViewY.class);
        for (int i=0; i<axisViews.length; i++) {
            ViewProxy<AxisViewY> axisView = axisViews[i];
            if (axisView.getView().getAxis().getSide() != aSide || !axisView.getView().isVisible())
                axisViews[i] = null;
        }
        return getNonNullArray(axisViews);
    }

    /**
     * Returns the available bounds available on given side.
     */
    protected Rect getBoundsForSide(Side aSide)
    {
        // If PrefDataAreaBounds set, return bounds for side using that
        if (_prefDataBounds !=null)
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
                bounds.height = _prefDataBounds.y;
                break;
            case BOTTOM:
                bounds.height = bounds.height - _prefDataBounds.getMaxY();
                bounds.y = _prefDataBounds.getMaxY();
                break;
            case LEFT:
                bounds.width = _prefDataBounds.x;
                break;
            case RIGHT:
                bounds.width = bounds.width - _prefDataBounds.getMaxX();
                bounds.x = _prefDataBounds.getMaxX();
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
