package snapcharts.view;
import snap.view.ParentView;
import snap.view.ViewEvent;
import snapcharts.model.*;

/**
 * A superclass for ChartView views.
 */
public abstract class ChartPartView<T extends ChartPart> extends ParentView {

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
     * Returns the DataSetList.
     */
    public DataSetList getDataSetList()  { return getChart().getDataSetList(); }

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
