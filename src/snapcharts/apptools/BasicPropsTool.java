package snapcharts.apptools;

import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snapcharts.app.ChartView;
import snapcharts.app.EditorPane;

/**
 * A class to manage UI to edit a ChartView.
 */
public class BasicPropsTool extends ViewOwner {
    
    // The ChartView
    ChartView        _chartView;

    /**
     * Constructor.
     */
    public BasicPropsTool(EditorPane anEP)
    {
        _chartView = anEP.getChartView();
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Reset TitleText, SubtitleText, YAxisTitleText
        setViewValue("TitleText", _chartView.getTitle());
        setViewValue("SubtitleText", _chartView.getSubtitle());
        setViewValue("YAxisTitleText", _chartView.getYAxis().getTitle());

        // Reset ShowLegendCheckBox, PartialYAxisCheckBox
        setViewValue("ShowLegendCheckBox", _chartView.isShowLegend());
        setViewValue("PartialYAxisCheckBox", _chartView.isShowPartialY());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle TitleText, SubtitleText, YAxisTitleText
        if(anEvent.equals("TitleText")) _chartView.setTitle(anEvent.getStringValue());
        if(anEvent.equals("SubtitleText")) _chartView.setSubtitle(anEvent.getStringValue());
        if(anEvent.equals("YAxisTitleText")) _chartView.getYAxis().setTitle(anEvent.getStringValue());

        // Handle ShowLegendCheckBox, PartialYAxisCheckBox
        if(anEvent.equals("ShowLegendCheckBox")) _chartView.setShowLegend(anEvent.getBoolValue());
        if(anEvent.equals("PartialYAxisCheckBox")) _chartView.setShowPartialY(anEvent.getBoolValue());
    }
}