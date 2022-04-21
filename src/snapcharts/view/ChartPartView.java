package snapcharts.view;
import snap.geom.Point;
import snap.geom.Pos;
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
     * Returns the TraceList.
     */
    public TraceList getTraceList()  { return getChart().getTraceList(); }

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
     * Returns the DataView.
     */
    public DataView getDataView()
    {
        ChartView chartView = getChartView();
        return chartView.getDataView();
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

        // Update Border, Fill, Effect, Font
        if (isResetProp(ChartPart.Border_Prop))
            setBorder(chartPart.getBorder());
        if (isResetProp(ChartPart.Fill_Prop))
            setFill(chartPart.getFill());
        if (isResetProp(ChartPart.Effect_Prop))
            setEffect(chartPart.getEffect());
        if (isResetProp(ChartPart.Opacity_Prop))
            setOpacity(chartPart.getOpacity());
        if (isResetProp(ChartPart.Font_Prop))
            setFont(chartPart.getFont());

        // Update Align, Margin, Padding, Spacing
        if (isResetProp(ChartPart.Align_Prop))
            setAlign(chartPart.getAlign());
        if (isResetProp(ChartPart.Margin_Prop))
            setMargin(chartPart.getMargin());
        if (isResetProp(ChartPart.Padding_Prop))
            setPadding(chartPart.getPadding());
        if (isResetProp(ChartPart.Spacing_Prop))
            setSpacing(chartPart.getSpacing());
    }

    /**
     * Returns whether a given property should be updated.
     */
    protected boolean isResetProp(String aPropName)
    {
        return true;
    }
}
