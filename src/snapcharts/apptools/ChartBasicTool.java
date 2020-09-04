package snapcharts.apptools;

import snap.view.ViewEvent;
import snapcharts.model.ChartType;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;

/**
 * A class to manage UI to edit a ChartView.
 */
public class ChartBasicTool extends ChartPartInsp {
    
    /**
     * Constructor.
     */
    public ChartBasicTool(ChartPane aCP)
    {
        super(aCP);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Chart Settings"; }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Chart
        Chart chart = getChart();

        // Reset NameText, TitleText, SubtitleText
        setViewValue("NameText", chart.getName());
        setViewValue("TitleText", chart.getTitle());
        setViewValue("SubtitleText", chart.getSubtitle());

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
        Chart chart = getChart();

        // Handle NameText
        if(anEvent.equals("NameText")) {
            chart.setName(anEvent.getStringValue());
            getChartPane().getDocPane().docItemNameChanged();
        }

        // Handle TitleText, SubtitleText
        if(anEvent.equals("TitleText")) chart.setTitle(anEvent.getStringValue());
        if(anEvent.equals("SubtitleText")) chart.setSubtitle(anEvent.getStringValue());

        // Handle ShowLegendCheckBox
        if(anEvent.equals("ShowLegendCheckBox")) chart.setShowLegend(anEvent.getBoolValue());

        // Handle BarChartButton, LineChartButton, PieChartButton
        if(anEvent.equals("BarChartButton")) chart.setType(ChartType.BAR);
        if(anEvent.equals("Bar3DChartButton")) chart.setType(ChartType.BAR_3D);
        if(anEvent.equals("LineChartButton")) chart.setType(ChartType.LINE);
        if(anEvent.equals("PieChartButton")) chart.setType(ChartType.PIE);
    }
}