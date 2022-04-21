package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.*;

/**
 * A ChartHelper for ChartType BAR_3D.
 */
public class Bar3DChartHelper extends ChartHelper3D {

    /**
     * Constructor.
     */
    public Bar3DChartHelper(ChartView aChartView)
    {
        super(aChartView);

        // Hide axes
        for (AxisView axisView : getAxisViews())
            axisView.setVisible(false);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.BAR_3D; }

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
     * Creates the DataAreas.
     */
    protected DataArea[] createDataAreas()
    {
        // Get traces
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces();
        int traceCount = traces.length;

        // Create DataArea for each Trace
        DataArea[] dataAreas = new DataArea[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            dataAreas[i] = new Bar3DDataArea(this, trace, i == 0);
        }

        // Return DataAreas
        return dataAreas;
    }

    /**
     * Override to handle Z axis special.
     */
    protected Intervals createIntervals(AxisView axisView)
    {
        // Handle Z Axis: Just return default { 0, 1 } because there really is no Z axis
        AxisType axisType = axisView.getAxisType();
        if (axisType == AxisType.Z)
            return Intervals.getIntervalsSimple(0, 1);

        // Do normal version
        return super.createIntervals(axisView);
    }

    /**
     * Override for chart type.
     */
    @Override
    public void resetView()
    {
        // Make sure all Trace.AxisTypeY are just Y
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces(); if (traces.length == 0) return;
        for (Trace trace : traces)
            trace.setAxisTypeY(AxisType.Y);

        // Do normal version
        super.resetView();
    }

    /**
     * Override to reset view transform.
     */
    @Override
    public void resetAxesAnimated()
    {
        DataArea[] dataAreas = getDataAreas();
        for (DataArea dataArea : dataAreas)
            if (dataArea instanceof Bar3DDataArea)
                ((Bar3DDataArea)dataArea).resetViewMatrixAnimated();
    }
}
