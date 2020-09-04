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
    public String getName()  { return "Header Settings"; }

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

        // Reset TitleText, SubtitleText
        //setViewValue("TitleText", chart.getTitle());
        //setViewValue("SubtitleText", chart.getSubtitle());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Legend
        Legend legend = getChart().getLegend();

        // Handle TitleText, SubtitleText
        //if(anEvent.equals("TitleText")) chart.setTitle(anEvent.getStringValue());
        //if(anEvent.equals("SubtitleText")) chart.setSubtitle(anEvent.getStringValue());
    }
}