package snapcharts.apptools;

import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.model.ChartTypeProps;
import snapcharts.modelx.ContourProps;

/**
 * A class to manage UI to edit a ChartTypeProps.
 */
public class ContourPropsInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public ContourPropsInsp(ChartPane aChartPane)
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
    public ChartPart getChartPart()  { return getChart().getTypeProps(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get TypeProps
        ChartTypeProps typeProps = getChart().getTypeProps();
        ContourProps contourProps = typeProps instanceof ContourProps ? (ContourProps) typeProps : null;
        if (contourProps == null) return;

        // Reset LevelsText, ShowLinesCheckBox, ShowMeshCheckBox
        setViewValue("LevelsText", contourProps.getLevelCount());
        setViewValue("ShowLinesCheckBox", contourProps.isShowLines());
        setViewValue("ShowMeshCheckBox", contourProps.isShowMesh());
        setViewEnabled("ReverseScaleCheckBox", false);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get TypeProps
        ChartTypeProps typeProps = getChart().getTypeProps();
        ContourProps contourProps = typeProps instanceof ContourProps ? (ContourProps) typeProps : null;
        if (contourProps == null) return;

        // Reset LevelsText, LevelsButtons
        if (anEvent.equals("LevelsText"))
            contourProps.setLevelCount(anEvent.getIntValue());
        if (anEvent.equals("LevelsButton4"))
            contourProps.setLevelCount(4);
        if (anEvent.equals("LevelsButton8"))
            contourProps.setLevelCount(8);
        if (anEvent.equals("LevelsButton16"))
            contourProps.setLevelCount(16);
        if (anEvent.equals("LevelsButton32"))
            contourProps.setLevelCount(32);
        if (anEvent.equals("LevelsButton64"))
            contourProps.setLevelCount(64);

        // Handle ShowLinesCheckBox, ShowMeshCheckBox
        if (anEvent.equals("ShowLinesCheckBox"))
            contourProps.setShowLines(anEvent.getBoolValue());
        if (anEvent.equals("ShowMeshCheckBox"))
            contourProps.setShowMesh(anEvent.getBoolValue());
    }
}