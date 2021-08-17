package snapcharts.view;
import snap.view.ParentView;
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
     * Called to reset view from ChartPart to make sure it is up to date.
     */
    protected void resetView()
    {
        // Get ChartPart
        ChartPart chartPart = getChartPart();
        resetViewFromStyledChartPart(chartPart);
    }

    /**
     * Called to reset view paint style attributes from updated ChartPartPainted.
     */
    protected void resetViewFromStyledChartPart(ChartPart aChartPart)
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
