package snapcharts.apptools;

import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.views.ChartView;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;

/**
 * A class to manage UI to edit a ChartView.
 */
public class BasicPropsTool extends ViewOwner {
    
    // The ChartView
    ChartView        _chartView;

    /**
     * Constructor.
     */
    public BasicPropsTool(ChartPane anEP)
    {
        _chartView = anEP.getChartView();
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Chart
        Chart chart = _chartView.getChart();

        // Reset TitleText, SubtitleText, YAxisTitleText
        setViewValue("TitleText", chart.getTitle());
        setViewValue("SubtitleText", chart.getSubtitle());
        setViewValue("YAxisTitleText", chart.getAxisY().getTitle());

        // Reset ShowLegendCheckBox, PartialYAxisCheckBox
        setViewValue("ShowLegendCheckBox", chart.isShowLegend());
        setViewValue("PartialYAxisCheckBox", chart.isShowPartialY());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Chart
        Chart chart = _chartView.getChart();

        // Handle TitleText, SubtitleText, YAxisTitleText
        if(anEvent.equals("TitleText")) chart.setTitle(anEvent.getStringValue());
        if(anEvent.equals("SubtitleText")) chart.setSubtitle(anEvent.getStringValue());
        if(anEvent.equals("YAxisTitleText")) chart.getAxisY().setTitle(anEvent.getStringValue());

        // Handle ShowLegendCheckBox, PartialYAxisCheckBox
        if(anEvent.equals("ShowLegendCheckBox")) chart.setShowLegend(anEvent.getBoolValue());
        if(anEvent.equals("PartialYAxisCheckBox")) chart.setShowPartialY(anEvent.getBoolValue());
    }
}