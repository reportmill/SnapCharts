package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.model.Trace;
import snapcharts.modelx.ContourTrace;

/**
 * A class to manage UI to edit a ContourTrace.
 */
public class ContourTraceInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public ContourTraceInsp(ChartPane aChartPane)
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
    public ChartPart getChartPart()  { return getContourTrace(); }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()
    {
        ChartPart selPart = _chartPane.getSelChartPart();
        return selPart instanceof Trace ? (Trace) selPart : null;
    }

    /**
     * Returns the ContourTrace.
     */
    public ContourTrace getContourTrace()
    {
        Trace trace = getTrace();
        return trace instanceof ContourTrace ? (ContourTrace) trace : null;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get ContourTrace
        ContourTrace contourTrace = getContourTrace(); if (contourTrace == null) return;

        // Reset ShowLinesCheckBox, ShowMeshCheckBox
        setViewValue("ShowLinesCheckBox", contourTrace.isShowLines());
        setViewValue("ShowMeshCheckBox", contourTrace.isShowMesh());
        setViewEnabled("ReverseScaleCheckBox", false);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get ContourTrace
        ContourTrace contourTrace = getContourTrace(); if (contourTrace == null) return;

        // Handle ShowLinesCheckBox, ShowMeshCheckBox
        if (anEvent.equals("ShowLinesCheckBox"))
            contourTrace.setShowLines(anEvent.getBoolValue());
        if (anEvent.equals("ShowMeshCheckBox"))
            contourTrace.setShowMesh(anEvent.getBoolValue());
    }
}