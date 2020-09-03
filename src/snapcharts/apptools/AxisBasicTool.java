package snapcharts.apptools;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.model.Chart;
import snapcharts.app.ChartPane;

/**
 * A class to manage UI to edit a ChartView.
 */
public class AxisBasicTool extends ViewOwner {

    // The ChartView
    private ChartPane _epane;

    /**
     * Constructor.
     */
    public AxisBasicTool(ChartPane anEP)
    {
        _epane = anEP;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get chart
        Chart chart = _epane.getChart();

        // Reset PartialYAxisCheckBox
        setViewValue("PartialYAxisCheckBox", chart.isShowPartialY());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get chart and type
        Chart chart = _epane.getChart();

        // Handle PartialYAxisCheckBox
        if(anEvent.equals("PartialYAxisCheckBox")) chart.setShowPartialY(anEvent.getBoolValue());
    }
}