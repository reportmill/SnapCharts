package snapcharts.apptools;
import snap.view.ViewOwner;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;

/**
 * A ViewOwner subclass for ChartPart inspectors.
 */
public abstract class ChartPartInsp extends ViewOwner {

    // The ChartPane
    protected ChartPane _chartPane;

    /**
     * Constructor.
     */
    public ChartPartInsp(ChartPane aChartPane)
    {
        _chartPane = aChartPane;
    }

    /**
     * Returns the name.
     */
    public abstract String getName();

    /**
     * Returns the ChartPane.
     */
    public ChartPane getChartPane()  { return _chartPane; }

    /**
     * Returns the Chart.
     */
    public Chart getChart()  { return _chartPane.getChart(); }
}
