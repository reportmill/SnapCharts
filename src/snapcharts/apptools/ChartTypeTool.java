package snapcharts.apptools;

import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.views.ChartView;
import snapcharts.app.EditorPane;

/**
 * A class to manage UI to edit a ChartView.
 */
public class ChartTypeTool extends ViewOwner {

    // The ChartView
    ChartView        _chartView;

    /**
     * Constructor.
     */
    public ChartTypeTool(EditorPane anEP)
    {
        _chartView = anEP.getChartView();
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Reset LineChartButton, BarChartButton
        String typeName = _chartView.getType() + "ChartButton";
        setViewValue(typeName, true);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle BarChartButton, LineChartButton, PieChartButton
        if(anEvent.equals("BarChartButton")) _chartView.setType(ChartView.BAR_TYPE);
        if(anEvent.equals("Bar3DChartButton")) _chartView.setType(ChartView.BAR3D_TYPE);
        if(anEvent.equals("LineChartButton")) _chartView.setType(ChartView.LINE_TYPE);
        if(anEvent.equals("PieChartButton")) _chartView.setType(ChartView.PIE_TYPE);
    }
}