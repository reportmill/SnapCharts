package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.charts.AxisBound;
import snapcharts.charts.ChartPart;
import snapcharts.charts.ContourAxis;

/**
 * A class to manage UI to edit a ChartView ContourAxis.
 */
public class ContourAxisInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public ContourAxisInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Contour Axis Settings"; }

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
        // Get ContourAxis
        ContourAxis contourAxis = getChart().getContourAxis();

        // Reset TitleText
        setViewValue("TitleText", contourAxis.getTitle());

        // Reset MinBoundAutoButton, MinBoundDataButton, MinBoundValueButton, MinBoundText
        AxisBound minBound = contourAxis.getMinBound();
        setViewValue("MinBoundAutoButton", minBound == AxisBound.AUTO);
        setViewValue("MinBoundDataButton", minBound == AxisBound.DATA);
        setViewValue("MinBoundValueButton", minBound == AxisBound.VALUE);
        double minVal = 0; //contourAxis.getChartHelper().getAxisMinForIntervalCalc(axisView);
        setViewValue("MinBoundText", minVal);
        setViewVisible("MinBoundText", minBound != AxisBound.AUTO);
        setViewEnabled("MinBoundText", minBound == AxisBound.VALUE);

        // Reset MaxBoundAutoButton, MaxBoundDataButton, MaxBoundValueButton, MaxBoundText
        AxisBound maxBound = contourAxis.getMaxBound();
        setViewValue("MaxBoundAutoButton", maxBound == AxisBound.AUTO);
        setViewValue("MaxBoundDataButton", maxBound == AxisBound.DATA);
        setViewValue("MaxBoundValueButton", maxBound == AxisBound.VALUE);
        double maxVal = 1; //axisView.getChartHelper().getAxisMaxForIntervalCalc(axisView);
        setViewValue("MaxBoundText", maxVal);
        setViewVisible("MaxBoundText", maxBound != AxisBound.AUTO);
        setViewEnabled("MaxBoundText", maxBound == AxisBound.VALUE);

        // Reset LevelsText
        setViewValue("LevelsText", contourAxis.getLevelCount());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get ContourAxis
        ContourAxis contourAxis = getChart().getContourAxis();

        // Handle MinBoundAutoButton, MinBoundDataButton, MinBoundValueButton
        if (anEvent.equals("MinBoundAutoButton"))
            contourAxis.setMinBound(AxisBound.AUTO);
        if (anEvent.equals("MinBoundDataButton"))
            contourAxis.setMinBound(AxisBound.DATA);
        if (anEvent.equals("MinBoundValueButton")) {
            double axisMin = 0; //contourAxis.getAxisMin();
            contourAxis.setMinBound(AxisBound.VALUE);
            contourAxis.setMinValue(axisMin);
        }
        if (anEvent.equals("MinBoundText")) {
            double val = anEvent.getFloatValue();
            contourAxis.setMinBound(AxisBound.VALUE);
            contourAxis.setMinValue(val);
        }

        // Handle MaxBoundAutoButton, MaxBoundDataButton, MaxBoundValueButton, MaxBoundText
        if (anEvent.equals("MaxBoundAutoButton"))
            contourAxis.setMaxBound(AxisBound.AUTO);
        if (anEvent.equals("MaxBoundDataButton"))
            contourAxis.setMaxBound(AxisBound.DATA);
        if (anEvent.equals("MaxBoundValueButton")) {
            double axisMax = 1; //contourAxis.getAxisMax();
            contourAxis.setMaxBound(AxisBound.VALUE);
            contourAxis.setMaxValue(axisMax);
        }
        if (anEvent.equals("MaxBoundText")) {
            double val = anEvent.getFloatValue();
            contourAxis.setMaxBound(AxisBound.VALUE);
            contourAxis.setMaxValue(val);
        }

        // Reset LevelsText, LevelsButtons
        if (anEvent.equals("LevelsText"))
            contourAxis.setLevelCount(anEvent.getIntValue());
        if (anEvent.equals("LevelsButton4"))
            contourAxis.setLevelCount(4);
        if (anEvent.equals("LevelsButton8"))
            contourAxis.setLevelCount(8);
        if (anEvent.equals("LevelsButton16"))
            contourAxis.setLevelCount(16);
        if (anEvent.equals("LevelsButton32"))
            contourAxis.setLevelCount(32);
        if (anEvent.equals("LevelsButton64"))
            contourAxis.setLevelCount(64);
    }
}