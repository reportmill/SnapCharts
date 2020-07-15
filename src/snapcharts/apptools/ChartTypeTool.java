package snapcharts.apptools;

import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.model.Chart;
import snapcharts.model.ChartType;
import snapcharts.app.ChartPane;

/**
 * A class to manage UI to edit a ChartView.
 */
public class ChartTypeTool extends ViewOwner {

    // The ChartView
    private ChartPane _epane;

    /**
     * Constructor.
     */
    public ChartTypeTool(ChartPane anEP)
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

        // Reset LineChartButton, BarChartButton
        ChartType type = chart.getType();
        String typeName = type.getStringPlain() + "ChartButton";
        setViewValue(typeName, true);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get chart and type
        Chart chart = _epane.getChart();

        // Handle BarChartButton, LineChartButton, PieChartButton
        if(anEvent.equals("BarChartButton")) chart.setType(ChartType.BAR);
        if(anEvent.equals("Bar3DChartButton")) chart.setType(ChartType.BAR_3D);
        if(anEvent.equals("LineChartButton")) chart.setType(ChartType.LINE);
        if(anEvent.equals("PieChartButton")) chart.setType(ChartType.PIE);
    }
}