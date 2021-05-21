package snapcharts.view;
import snap.view.ParentView;
import snapcharts.model.*;

/**
 * A superclass for ChartView views.
 */
public abstract class ChartPartView<T extends ChartPart> extends ParentView {

    /**
     * Constructor.
     */
    public ChartPartView()
    {
        super();
    }

    /**
     * Returns the ChartPart.
     */
    public abstract T getChartPart();

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
     * Called to reset view from ChartPart to make sure it is up to date.
     */
    protected void resetView()
    {
        // Get ChartPart
        ChartPart chartPart = getChartPart();
        if (chartPart instanceof StyledChartPart)
            resetViewFromStyledChartPart((StyledChartPart) chartPart);
    }

    /**
     * Called to reset view paint style attributes from updated ChartPartPainted.
     */
    protected void resetViewFromStyledChartPart(StyledChartPart aChartPart)
    {
        // Update Font, Fill
        setFont(aChartPart.getFont());
        setFill(aChartPart.getFill());

        // Update Border, Effect
        if (!(this instanceof ChartView)) {
            setBorder(aChartPart.getBorder());
            setEffect(aChartPart.getEffect());
        }
    }
}
