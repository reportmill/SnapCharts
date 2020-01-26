package snap.charts;
import snap.view.*;

/**
 * A class to manage UI to edit a ChartView.
 */
public class PropsPane extends ViewOwner {
    
    // The ChartView
    ChartView        _chartView;

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Reset LineChartButton, BarChartButton
    String typeName = _chartView.getType() + "ChartButton";
    setViewValue(typeName, true);
    
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
    // Handle BarChartButton, LineChartButton, PieChartButton
    if(anEvent.equals("BarChartButton")) _chartView.setType(ChartView.BAR_TYPE);
    if(anEvent.equals("Bar3DChartButton")) _chartView.setType(ChartView.BAR3D_TYPE);
    if(anEvent.equals("LineChartButton")) _chartView.setType(ChartView.LINE_TYPE);
    if(anEvent.equals("PieChartButton")) _chartView.setType(ChartView.PIE_TYPE);
    
    // Handle TitleText, SubtitleText, YAxisTitleText
    if(anEvent.equals("TitleText")) _chartView.setTitle(anEvent.getStringValue());
    if(anEvent.equals("SubtitleText")) _chartView.setSubtitle(anEvent.getStringValue());
    if(anEvent.equals("YAxisTitleText")) _chartView.getYAxis().setTitle(anEvent.getStringValue());
    
    // Handle ShowLegendCheckBox, PartialYAxisCheckBox
    if(anEvent.equals("ShowLegendCheckBox")) _chartView.setShowLegend(anEvent.getBoolValue());
    if(anEvent.equals("PartialYAxisCheckBox")) _chartView.setShowPartialY(anEvent.getBoolValue());
}

}