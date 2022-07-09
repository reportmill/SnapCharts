package snapcharts.view;
import snap.geom.*;
import snap.gfx.Border;
import snap.view.*;
import snapcharts.viewx.PolarChartHelper;
import java.util.Arrays;

/**
 * A class to layout ChartView component parts (Header, Legend, Axes, Content).
 */
public class ChartViewLayout {

    // The Chart that owns this layout
    protected ChartView  _chartView;

    // The preferred content bounds (optional)
    protected Rect _prefContentBounds;

    // The layout proxy
    private ViewProxy<ChartView>  _chartProxy;

    // The header proxy
    private ViewProxy<HeaderView>  _headerProxy;

    // The legend proxy
    private ViewProxy<LegendView>  _legendProxy;

    // The contour proxy
    private ViewProxy<ContourAxisView>  _contourProxy;

    // The content proxy
    private ViewProxy<ContentView>  _contentProxy;

    // The current inset for ContentView
    protected Insets _contentInsets = new Insets(0, 0, 0, 0);

    // The maximum size that a side can be as ratio of chart size ( 30% )
    private final double MAX_SIDE_RATIO = .4;

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
     * Top level layout.
     */
    public void layoutChart()
    {
        // Create/set ViewProxy for Chart and Children
        _chartProxy = new ViewProxy<>(_chartView);
        _headerProxy = _chartProxy.getChildForClass(HeaderView.class);
        _legendProxy = _chartProxy.getChildForClass(LegendView.class);
        _contourProxy = _chartProxy.getChildForClass(ContourAxisView.class);
        _contentProxy = _chartProxy.getChildForClass(ContentView.class);

        // Get PrefContentBounds
        _prefContentBounds = _chartView.getPrefContentBounds();

        // Layout top, bottom
        layoutTopSide();
        layoutBottomSide();

        // Layout left side, right side
        layoutLeftSide();
        layoutRightSide();

        // Set ContentBounds (either from PrefContentBounds or from resulting layout insets)
        Rect contentBounds = _prefContentBounds;
        if (contentBounds == null) {
            contentBounds = new Rect(0, 0, _chartView.getWidth(), _chartView.getHeight());
            contentBounds.inset(_contentInsets);
        }

        // Set ContentView.Bounds
        _contentProxy.setBounds(contentBounds);

        // Adjust axes for ContentBounds
        for (ViewProxy<?> child : _chartProxy.getChildren()) {
            if (child.getView() instanceof AxisViewX) {
                child.setX(contentBounds.x);
                child.setWidth(contentBounds.width);
            }
            if (child.getView() instanceof AxisViewY) {
                child.setY(contentBounds.y);
                child.setHeight(contentBounds.height);
            }
        }

        // Handle special legend layout (Inside, Floating)
        if (_legendProxy != null && _legendProxy.isVisible())
            layoutLegendSpecialCases(contentBounds);

        // Copy back to views
        _chartProxy.setBoundsInClient();
    }

    /**
     * Layout of top views: Header, legend (if top), top axis (if polar?).
     */
    protected void layoutTopSide()
    {
        // Create view proxy for layout of chart top
        ColViewProxy<?> topProxy = getTopViewProxy();

        // Get top Width/Height for proxy: If no PrefContentBounds, use proxy PrefHeight
        Rect topBounds = getBoundsForSide(Side.TOP);
        double topHeight = topBounds.height;
        if (_prefContentBounds == null) {
            double prefHeight = topProxy.getPrefHeight(topBounds.width);
            topHeight = Math.min(prefHeight, topHeight);
        }

        // Set proxy size and layout as ColView
        topProxy.setSize(topBounds.width, topHeight);
        topProxy.layoutProxy();

        // Update insets
        _contentInsets.top = topHeight;
    }

    /**
     * Returns the ViewProxy to layout chart top.
     */
    private ColViewProxy<?> getTopViewProxy()
    {
        ColViewProxy<?> viewProxy = new ColViewProxy<>(_chartView);
        viewProxy.setBorder(Border.emptyBorder());
        viewProxy.setAlign(Pos.BOTTOM_CENTER);
        viewProxy.setSpacing(VIEW_SPACING);
        viewProxy.setPadding(getPaddingForSide(Side.TOP));
        viewProxy.setFillWidth(true);
        ViewProxy<?>[] topViews = getViewsForSide(Side.TOP);
        viewProxy.setChildren(topViews);
        return viewProxy;
    }

    /**
     * Layout of bottom views: X Axis, legend (if bottom).
     */
    protected void layoutBottomSide()
    {
        // Create view proxy for layout of chart bottom
        ColViewProxy<?> bottomProxy = getBottomViewProxy();

        // Get/set bottomHeight for proxy: If no PrefContentBounds, use proxy PrefHeight
        Rect bottomBounds = getBoundsForSide(Side.BOTTOM);
        double bottomHeight = bottomBounds.height;
        if (_prefContentBounds == null) {
            double prefHeight = bottomProxy.getPrefHeight(bottomBounds.width);
            prefHeight = Math.max(prefHeight, RIGHT_MARGIN_MIN);
            bottomHeight = Math.min(prefHeight, bottomHeight);
        }

        // Set proxy size and layout as ColView
        bottomProxy.setSize(bottomBounds.width, bottomHeight);
        bottomProxy.layoutProxy();

        // Shift views to bottomBounds.y
        double bottomY = _chartProxy.getHeight() - bottomHeight;
        for (ViewProxy<?> proxy : bottomProxy.getChildren())
            proxy.setY(proxy.getY() + bottomY);

        // Update insets
        _contentInsets.bottom = bottomHeight;
    }

    /**
     * Returns the ViewProxy to layout chart bottom.
     */
    private ColViewProxy<?> getBottomViewProxy()
    {
        ColViewProxy<?> viewProxy = new ColViewProxy<>(_chartView);
        viewProxy.setBorder(Border.emptyBorder());
        viewProxy.setAlign(Pos.TOP_CENTER);
        viewProxy.setSpacing(VIEW_SPACING);
        viewProxy.setFillWidth(true);
        ViewProxy<?>[] bottomViews = getViewsForSide(Side.BOTTOM);
        viewProxy.setChildren(bottomViews);
        viewProxy.setPadding(getPaddingForSide(Side.BOTTOM));
        return viewProxy;
    }

    /**
     * Layout of left views: Legend (if left), Y/Y3 axis (when available).
     */
    protected void layoutLeftSide()
    {
        // Create view proxy for layout of chart left side
        RowViewProxy<?> leftProxy = getLeftViewProxy();

        // Get/set leftWidth for proxy: If no PrefContentBounds, use proxy PrefWidth
        Rect leftBounds = getBoundsForSide(Side.LEFT);
        double leftWidth = leftBounds.width;
        if (_prefContentBounds == null) {
            double prefWidth = leftProxy.getPrefWidth(-1);
            prefWidth = Math.max(prefWidth, RIGHT_MARGIN_MIN);
            leftWidth = Math.min(prefWidth, leftWidth);
        }

        // Set proxy size and layout as RowView
        leftProxy.setSize(leftWidth, leftBounds.height);
        leftProxy.layoutProxy();

        // Update insets
        _contentInsets.left = leftWidth;
    }

    /**
     * Returns the ViewProxy to layout chart left side.
     */
    private RowViewProxy<?> getLeftViewProxy()
    {
        RowViewProxy<?> viewProxy = new RowViewProxy<>(_chartView);
        viewProxy.setBorder(Border.emptyBorder());
        viewProxy.setAlign(Pos.CENTER_RIGHT);
        ViewProxy<?>[] leftViews = getViewsForSide(Side.LEFT);
        viewProxy.setChildren(leftViews);
        viewProxy.setSpacing(VIEW_SPACING);
        viewProxy.setFillHeight(true);
        viewProxy.setPadding(getPaddingForSide(Side.LEFT));
        return viewProxy;
    }

    /**
     * Layout of right views: Y2/Y4 axis (when available), Legend (if right side).
     */
    protected void layoutRightSide()
    {
        // Create view proxy for layout of chart right side
        RowViewProxy<?> rightProxy = getRightViewProxy();

        // Get/set rightWidth for proxy: If no PrefContentBounds, use proxy PrefWidth
        Rect rightBounds = getBoundsForSide(Side.RIGHT);
        double rightWidth = rightBounds.width;
        if (_prefContentBounds == null) {
            double prefWidth = rightProxy.getPrefWidth(-1);
            prefWidth = Math.max(prefWidth, RIGHT_MARGIN_MIN);
            rightWidth = Math.min(prefWidth, rightWidth);
        }

        // Set proxy size and layout as RowView
        rightProxy.setSize(rightWidth, rightBounds.height);
        rightProxy.layoutProxy();

        // Shift views to rightBounds.x
        double rightX = _chartProxy.getWidth() - rightWidth;
        for (ViewProxy<?> proxy : rightProxy.getChildren())
            proxy.setX(proxy.getX() + rightX);

        // Update insets
        _contentInsets.right = rightWidth;
    }

    /**
     * Returns the ViewProxy to layout chart right side.
     */
    private RowViewProxy<?> getRightViewProxy()
    {
        RowViewProxy<?> viewProxy = new RowViewProxy<>(_chartView);
        viewProxy.setBorder(Border.emptyBorder());
        viewProxy.setAlign(Pos.CENTER_LEFT);
        ViewProxy<?>[] rightViews = getViewsForSide(Side.RIGHT);
        viewProxy.setChildren(rightViews);
        viewProxy.setSpacing(VIEW_SPACING);
        viewProxy.setFillHeight(true);
        viewProxy.setPadding(getPaddingForSide(Side.RIGHT));
        return viewProxy;
    }

    /**
     * Lays out the legend after normal layout to handle special cases (Inside, Floating).
     */
    private void layoutLegendSpecialCases(Rect contentBounds)
    {
        // If Inside, layout special
        LegendView legendView = _legendProxy.getView();
        if (legendView.isInside())
            layoutLegendInside();

        // If Position is Top/Bottom, restrict to ContentBounds
        else if (legendView.getPosition().isTopOrBottom()) {
            _legendProxy.setX(contentBounds.x);
            _legendProxy.setWidth(contentBounds.width);
        }

        // Handle Legend.Floating (Uses Legend.Marker for location with some hacks)
        else if (legendView.isFloating()) {
            Rect legendBounds = legendView.getFloatingBounds();
            _legendProxy.setBounds(legendBounds);
        }
    }

    /**
     * Lays out the legend inside (when Legend.Inside is set).
     */
    private void layoutLegendInside()
    {
        // Get LegendMargin - if empty, change to 5, otherwise looks too close to border
        Insets legendMargin = _legendProxy.getMargin();
        if (legendMargin.isEmpty())
            legendMargin = new Insets(5, 5, 5, 5);

        // Get Legend width/height (no larger than ContentView bounds)
        Rect dataBnds = _contentProxy.getBounds();
        double legendW = Math.min(_legendProxy.getBestWidth(-1) + legendMargin.getWidth(), dataBnds.width);
        double legendH = Math.min(_legendProxy.getBestHeight(-1) + legendMargin.getHeight(), dataBnds.height);

        // Get Legend XY for position and size
        Pos legendPos = _legendProxy.getView().getPosition();
        Point legendXY = Rect.getPointForPositionAndSize(dataBnds, legendPos, legendW, legendH);

        // Eliminate the margin and round
        legendXY.x += legendMargin.left;
        legendXY.y += legendMargin.top;
        legendW -= legendMargin.getWidth();
        legendH -= legendMargin.getHeight();
        legendXY.snap();

        // Set new legend bounds
        _legendProxy.setBounds(legendXY.x, legendXY.y, legendW, legendH);
    }

    /**
     * Returns the array of views for given side.
     */
    protected ViewProxy<?>[] getViewsForSide(Side aSide)
    {
        // Get Axes
        ViewProxy<?>[] axes = getAxesForSide(aSide);
        ViewProxy<?> axis1 = axes.length > 0 ? axes[0] : null;
        ViewProxy<?> axis2 = axes.length > 1 ? axes[1] : null;

        // Get Legend if on given side.
        LegendView legendView = _legendProxy!=null ? _legendProxy.getView() : null;
        boolean hasLegend = legendView!=null && !legendView.isInside() && legendView.getPosition().getSide() == aSide;
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
    public ViewProxy<?>[] getAxesForSide(Side aSide)
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
        // If PrefContentBounds set, return bounds for side using that
        if (_prefContentBounds != null)
            return getBoundsForSideFixed(aSide);

        // Return bounds for side (size is MAX_SIDE_RATIO of chart size)
        double chartW = _chartProxy.getWidth();
        double chartH = _chartProxy.getHeight();
        Rect bounds = new Rect(0, 0, chartW, chartH);
        switch (aSide)
        {
            case TOP:
                bounds.height = Math.round(MAX_SIDE_RATIO * chartH);
                break;
            case BOTTOM:
                bounds.height = Math.round(MAX_SIDE_RATIO * chartH);
                bounds.y = chartH - bounds.height;
                break;
            case LEFT:
                bounds.width = Math.round(MAX_SIDE_RATIO * chartW);
                break;
            case RIGHT:
                bounds.width = Math.round(MAX_SIDE_RATIO * chartW);
                bounds.x = chartW - bounds.width;
                break;
            default: throw new RuntimeException("ChartViewLayout.getBoundsForSide: Unknown side" + aSide);
        }

        return bounds;
    }

    /**
     * Returns the available bounds on given side for when PrefContentBounds is set.
     */
    protected Rect getBoundsForSideFixed(Side aSide)
    {
        Rect bounds = new Rect(0, 0, _chartProxy.getWidth(), _chartProxy.getHeight());
        switch (aSide)
        {
            case TOP:
                bounds.height = _prefContentBounds.y;
                break;
            case BOTTOM:
                bounds.height = bounds.height - _prefContentBounds.getMaxY();
                bounds.y = _prefContentBounds.getMaxY();
                break;
            case LEFT:
                bounds.width = _prefContentBounds.x;
                break;
            case RIGHT:
                bounds.width = bounds.width - _prefContentBounds.getMaxX();
                bounds.x = _prefContentBounds.getMaxX();
                break;
            default: throw new RuntimeException("ChartViewLayout.getBoundsForSideFixed: Unknown side: " + aSide);
        }
        return bounds;
    }

    /**
     * Returns the padding for side.
     */
    private Insets getPaddingForSide(Side aSide)
    {
        // Start with ChartView.Padding
        Insets padding = _chartView.getPadding().clone();

        // Adjust for side
        switch (aSide) {

            // Handle TOP
            case TOP: padding.bottom = 0; break;

            // Handle BOTTOM
            case BOTTOM: padding.top = 0; break;

            // Handle LEFT
            case LEFT:
                padding.top = _contentInsets.top;
                padding.bottom = _contentInsets.bottom;
                padding.right = 0;
                break;

            // Handle RIGHT
            case RIGHT:
                padding.top = _contentInsets.top;
                padding.bottom = _contentInsets.bottom;
                padding.left = 0;
                break;
        }

        // Return padding
        return padding;
    }

    /**
     * Returns array of non-null components.
     */
    private static <T> T[] getNonNullArray(T ... theItems)
    {
        int len = 0; for (T item : theItems) if (item != null) len++;
        if (len == theItems.length) return theItems;
        T[] items = Arrays.copyOf(theItems, len);
        for (int i=0, j=0; i<theItems.length; i++)
            if (theItems[i] != null) items[j++] = theItems[i];
        return items;
    }
}
