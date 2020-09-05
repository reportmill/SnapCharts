package snapcharts.apptools;

import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.Legend;

/**
 * A class to manage UI to edit a ChartView Legend.
 */
public class LegendInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public LegendInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Legend Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart().getLegend(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Legend
        Legend legend = getChart().getLegend();

        // Reset ShowLegendCheckBox
        setViewValue("ShowLegendCheckBox", legend.isShowLegend());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Legend
        Legend legend = getChart().getLegend();

        // Handle ShowLegendCheckBox
        if(anEvent.equals("ShowLegendCheckBox")) legend.setShowLegend(anEvent.getBoolValue());
    }
}