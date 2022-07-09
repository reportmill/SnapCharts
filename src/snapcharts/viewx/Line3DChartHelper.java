package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.ChartView;
import snapcharts.view.TraceView;

/**
 * A ChartHelper for 3D Line chart (ChartType.LINE_3D).
 */
public class Line3DChartHelper extends ChartHelper3D {

    /**
     * Constructor.
     */
    public Line3DChartHelper(ChartView aChartView)
    {
        super(aChartView);

        // Hide axes
        for (AxisView axisView : getAxisViews())
            axisView.setVisible(false);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.LINE_3D; }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        return new AxisType[] { AxisType.X, AxisType.Y, AxisType.Z };
    }

    /**
     * Creates an AxisView for given type.
     */
    protected AxisView createAxisView(AxisType anAxisType)
    {
        AxisView axisView = super.createAxisView(anAxisType);
        axisView.setVisible(false);
        return axisView;
    }

    /**
     * Creates the TraceViews.
     */
    protected TraceView[] createTraceViews()
    {
        // Get traces
        Content content = getContent();
        Trace[] traces = content.getTraces();
        int traceCount = traces.length;

        // Create TraceView for each Trace
        TraceView[] traceViews = new TraceView[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            traceViews[i] = new Line3DTraceView(this, trace, i == 0);
        }

        // Return
        return traceViews;
    }

    /**
     * Creates a TraceView3D for projections.
     */
    protected TraceView3D createProjectionTraceView()
    {
        Content content = getContent();
        Trace[] traces = content.getTraces();
        Trace trace = traces.length > 0 ? traces[0] : null;
        return trace != null ? new Line3DTraceView(this, trace, true) : null;
    }

    /**
     * Override to handle Z axis special.
     */
    protected Intervals createIntervals(AxisView axisView)
    {
        // Handle Z Axis: Just return simple integer intervals { 0, TraceCount } because there really is no Z axis
        AxisType axisType = axisView.getAxisType();
        if (axisType == AxisType.Z) {
            Content content = getContent();
            int traceCount = content.getTraceCount();
            return Intervals.getCategoryIntervals(traceCount);
        }

        // Do normal version
        return super.createIntervals(axisView);
    }

    /**
     * Override for chart type.
     */
    public void resetView()
    {
        // Make sure all Trace.AxisTypeY are just Y
        Content content = getContent();
        Trace[] traces = content.getTraces(); if (traces.length == 0) return;
        for (Trace trace : traces)
            trace.setAxisTypeY(AxisType.Y);

        // Do normal version
        super.resetView();
    }
}
