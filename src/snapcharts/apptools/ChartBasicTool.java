package snapcharts.apptools;

import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.model.ChartType;
import snapcharts.views.ChartView;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;

/**
 * A class to manage UI to edit a ChartView.
 */
public class ChartBasicTool extends ViewOwner {
    
    // The ChartPane
    private ChartPane  _chartPane;

    /**
     * Constructor.
     */
    public ChartBasicTool(ChartPane aCP)
    {
        _chartPane = aCP;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Chart
        Chart chart = _chartPane.getChart();

        // Reset NameText, TitleText, SubtitleText, XAxisTitleText, YAxisTitleText
        setViewValue("NameText", chart.getName());
        setViewValue("TitleText", chart.getTitle());
        setViewValue("SubtitleText", chart.getSubtitle());
        setViewValue("XAxisTitleText", chart.getAxisX().getTitle());
        setViewValue("YAxisTitleText", chart.getAxisY().getTitle());

        // Reset ShowLegendCheckBox
        setViewValue("ShowLegendCheckBox", chart.isShowLegend());

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
        // Get Chart
        Chart chart = _chartPane.getChart();

        // Handle NameText
        if(anEvent.equals("NameText")) {
            chart.setName(anEvent.getStringValue());
            _chartPane.getDocPane().docItemNameChanged();
        }

        // Handle TitleText, SubtitleText, XAxisTitleText, YAxisTitleText
        if(anEvent.equals("TitleText")) chart.setTitle(anEvent.getStringValue());
        if(anEvent.equals("SubtitleText")) chart.setSubtitle(anEvent.getStringValue());
        if(anEvent.equals("XAxisTitleText")) chart.getAxisX().setTitle(anEvent.getStringValue());
        if(anEvent.equals("YAxisTitleText")) chart.getAxisY().setTitle(anEvent.getStringValue());

        // Handle ShowLegendCheckBox
        if(anEvent.equals("ShowLegendCheckBox")) chart.setShowLegend(anEvent.getBoolValue());

        // Handle BarChartButton, LineChartButton, PieChartButton
        if(anEvent.equals("BarChartButton")) chart.setType(ChartType.BAR);
        if(anEvent.equals("Bar3DChartButton")) chart.setType(ChartType.BAR_3D);
        if(anEvent.equals("LineChartButton")) chart.setType(ChartType.LINE);
        if(anEvent.equals("PieChartButton")) chart.setType(ChartType.PIE);
    }
}