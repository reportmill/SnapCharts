package snapcharts.view;
import snap.geom.Point;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Effect;
import snap.view.ParentView;
import snap.view.View;
import snap.view.ViewEvent;
import snapcharts.model.*;

/**
 * A superclass for ChartView views.
 */
public class ChartPartView<T extends ChartPart> extends ParentView {

    // The ChartPart
    protected T  _chartPart;

    // Whether various properties have been overridden
    protected boolean  _borderOverride, _effectOverride;

    /**
     * Constructor.
     */
    public ChartPartView()
    {
        super();
    }

    /**
     * Constructor.
     */
    public ChartPartView(T aChartPart)
    {
        super();
        _chartPart = aChartPart;
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return _chartPart; }

    /**
     * Returns the chart.
     */
    public Chart getChart()
    {
        ChartView chartView = getChartView();
        return chartView.getChart();
    }

    /**
     * Returns the ChartType.
     */
    public ChartType getChartType()  { return getChart().getType(); }

    /**
     * Returns the Content.
     */
    public Content getContent()  { return getChart().getContent(); }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()
    {
        return getParent(ChartView.class);
    }

    /**
     * Returns the ChartHelper.
     */
    public ChartHelper getChartHelper()
    {
        ChartView chartView = getChartView();
        return chartView.getChartHelper();
    }

    /**
     * Returns the ContentView.
     */
    public ContentView getContentView()
    {
        ChartView chartView = getChartView();
        return chartView.getContentView();
    }

    /**
     * Returns the child of given class hit by coords.
     */
    public ChartPartView getChildChartPartViewForXY(double aX, double aY)
    {
        View[] children = getChildren();
        for (int i = children.length - 1; i >= 0; i--) {
            View child = children[i];
            Point pointInChild = child.parentToLocal(aX, aY);
            if (!child.isVisible())
                continue;
            if (child instanceof ChartPartView && child.contains(pointInChild.x, pointInChild.y))
                return (ChartPartView) child;
        }
        return null;
    }

    /**
     * Returns the ChartPart for given point XY in ChartView coords.
     */
    public ChartPartView getChildChartPartViewDeepForXY(double aX, double aY)
    {
        // Get child ChartPartView at point
        ChartPartView chartPartChild = getChildChartPartViewForXY(aX, aY);

        // If found, recurse
        if (chartPartChild != null) {
            Point pointInChild = chartPartChild.parentToLocal(aX, aY);
            ChartPartView chartPartChildDeep = chartPartChild.getChildChartPartViewDeepForXY(pointInChild.x, pointInChild.y);
            if (chartPartChildDeep != null)
                return chartPartChildDeep;
        }

        // Return child
        return chartPartChild;
    }

    /**
     * Returns whether this view is movable.
     */
    public boolean isMovable()  { return false; }

    /**
     * Returns whether this view is resizable.
     */
    public boolean isResizable()  { return false; }

    /**
     * Called to handle a move event.
     */
    public void processMoveEvent(ViewEvent anEvent, ViewEvent lastEvent)  { }

    /**
     * Called to handle a resize event.
     */
    public void processResizeEvent(ViewEvent anEvent, ViewEvent lastEvent, Pos aHandlePos)  { }

    /**
     * Returns handle positions for view.
     */
    public Pos[] getHandlePositions()  { return Pos.ALL_OUTER; }

    /**
     * Called to reset view from ChartPart to make sure it is up to date.
     */
    protected void resetView()
    {
        // Get ChartPart
        ChartPart chartPart = getChartPart();

        // Update Border (if no override)
        if (!_borderOverride)
            super.setBorder(chartPart.getBorder());

        // Update Fill
        setFill(chartPart.getFill());

        // Update Effect (if no override)
        if (!_effectOverride)
            super.setEffect(chartPart.getEffect());

        // Update Opacity, Font
        setOpacity(chartPart.getOpacity());
        setFont(chartPart.getFont());

        // Update Align, Margin, Padding, Spacing
        setAlign(chartPart.getAlign());
        setMargin(chartPart.getMargin());
        setPadding(chartPart.getPadding());
        setSpacing(chartPart.getSpacing());
    }

    /**
     * Override to detect override.
     */
    @Override
    public void setBorder(Border aBorder)
    {
        super.setBorder(aBorder);
        _borderOverride = aBorder != null;
    }

    /**
     * Override to detect override.
     */
    @Override
    public void setEffect(Effect anEff)
    {
        super.setEffect(anEff);
        _effectOverride = anEff != null;
    }
}
