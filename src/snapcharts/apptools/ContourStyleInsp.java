package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.model.Trace;
import snapcharts.model.TraceStyle;
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
    public String getName()  { return "Contour Style"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getContourStyle(); }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()
    {
        ChartPart selPart = _chartPane.getSelChartPart();
        return selPart instanceof Trace ? (Trace) selPart : null;
    }

    /**
     * Returns the TraceStyle.
     */
    public ContourStyle getContourStyle()
    {
        Trace trace = getTrace();
        TraceStyle traceStyle = trace != null ? trace.getTraceStyle() : null;
        return traceStyle instanceof ContourStyle ? (ContourStyle) traceStyle : null;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get ContourStyle
        ContourStyle contourStyle = getContourStyle(); if (contourStyle == null) return;

        // Reset ShowLinesCheckBox, ShowMeshCheckBox
        setViewValue("ShowLinesCheckBox", contourStyle.isShowLines());
        setViewValue("ShowMeshCheckBox", contourStyle.isShowMesh());
        setViewEnabled("ReverseScaleCheckBox", false);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get ContourStyle
        ContourStyle contourStyle = getContourStyle(); if (contourStyle == null) return;

        // Handle ShowLinesCheckBox, ShowMeshCheckBox
        if (anEvent.equals("ShowLinesCheckBox"))
            contourStyle.setShowLines(anEvent.getBoolValue());
        if (anEvent.equals("ShowMeshCheckBox"))
            contourStyle.setShowMesh(anEvent.getBoolValue());
    }
}