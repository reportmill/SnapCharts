package snapcharts.view;
import snap.view.ParentView;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.ChartType;
import snapcharts.model.DataSetList;

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
     * Called to reset view from updated Chart.
     */
    protected void resetView()
    {
        // Get Axis
        ChartPart chartPart = getChartPart(); if (chartPart==null) return;

        // Update basic props
        setFont(chartPart.getFont());
        setFill(chartPart.getFill());
        if (!(this instanceof ChartView)) {
            setBorder(chartPart.getBorder());
            setEffect(chartPart.getEffect());
        }
    }
}
