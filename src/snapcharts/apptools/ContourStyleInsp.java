package snapcharts.apptools;

import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.model.ChartStyle;
import snapcharts.modelx.ContourStyle;

/**
 * A class to manage UI to edit a ContourStyle.
 */
public class ContourStyleInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public ContourStyleInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Contour Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart().getChartStyle(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get ChartStyle
        ChartStyle chartStyle = getChart().getChartStyle();
        ContourStyle contourStyle = chartStyle instanceof ContourStyle ? (ContourStyle) chartStyle : null;
        if (contourStyle == null) return;

        // Reset LevelsText, ShowLinesCheckBox, ShowMeshCheckBox
        setViewValue("LevelsText", contourStyle.getLevelCount());
        setViewValue("ShowLinesCheckBox", contourStyle.isShowLines());
        setViewValue("ShowMeshCheckBox", contourStyle.isShowMesh());
        setViewEnabled("ReverseScaleCheckBox", false);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get ChartStyle
        ChartStyle chartStyle = getChart().getChartStyle();
        ContourStyle contourStyle = chartStyle instanceof ContourStyle ? (ContourStyle) chartStyle : null;
        if (contourStyle == null) return;

        // Reset LevelsText, LevelsButtons
        if (anEvent.equals("LevelsText"))
            contourStyle.setLevelCount(anEvent.getIntValue());
        if (anEvent.equals("LevelsButton4"))
            contourStyle.setLevelCount(4);
        if (anEvent.equals("LevelsButton8"))
            contourStyle.setLevelCount(8);
        if (anEvent.equals("LevelsButton16"))
            contourStyle.setLevelCount(16);
        if (anEvent.equals("LevelsButton32"))
            contourStyle.setLevelCount(32);
        if (anEvent.equals("LevelsButton64"))
            contourStyle.setLevelCount(64);

        // Handle ShowLinesCheckBox, ShowMeshCheckBox
        if (anEvent.equals("ShowLinesCheckBox"))
            contourStyle.setShowLines(anEvent.getBoolValue());
        if (anEvent.equals("ShowMeshCheckBox"))
            contourStyle.setShowMesh(anEvent.getBoolValue());
    }
}