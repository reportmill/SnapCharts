package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.*;

/**
 * A ChartHelper for ChartType BAR_3D.
 */
public class Bar3DChartHelper extends ChartHelper {

    // Constant for default chart depth
    public static final double DEFAULT_DEPTH = 100;

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
        return new AxisType[] { AxisType.X, AxisType.Y };
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
        TraceList traceList = getTraceList();
        if (traceList.getTraceCount() == 0)
            return new DataArea[0];
        Trace trace = traceList.getTrace(0);
        return new DataArea[] { new Bar3DDataArea(this, trace) };
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
