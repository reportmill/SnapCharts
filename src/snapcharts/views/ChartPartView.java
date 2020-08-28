package snapcharts.views;
import snap.view.ParentView;
import snapcharts.model.Chart;

/**
 * A superclass for ChartView views.
 */
public class ChartPartView extends ParentView {

    /**
     * Returns the chart.
     */
    public Chart getChart()
    {
        ChartView chartView = getChartView();
        return chartView.getChart();
    }

    /**
     * Returns the ChartView.
     */
    public ChartView getChartView()
    {
        return getParent(ChartView.class);
    }

    /**
     * Called to reset view from updated Chart.
     */
    protected void resetView()  { }
}
