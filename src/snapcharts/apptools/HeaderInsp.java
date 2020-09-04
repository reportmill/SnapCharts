package snapcharts.apptools;

import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.*;

/**
 * A class to manage UI to edit a ChartView Header.
 */
public class HeaderInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public HeaderInsp(ChartPane aChartPane)
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
    public ChartPart getChartPart()  { return getChart().getHeader(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Chart
        Chart chart = getChart();

        // Reset TitleText, SubtitleText
        setViewValue("TitleText", chart.getTitle());
        setViewValue("SubtitleText", chart.getSubtitle());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Chart
        Chart chart = getChart();

        // Handle TitleText, SubtitleText
        if(anEvent.equals("TitleText")) chart.setTitle(anEvent.getStringValue());
        if(anEvent.equals("SubtitleText")) chart.setSubtitle(anEvent.getStringValue());
    }
}