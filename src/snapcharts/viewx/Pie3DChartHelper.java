/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.ChartView;
import snapcharts.view.TraceView;

/**
 * A ChartHelper for ChartType PIE_3D.
 */
public class Pie3DChartHelper extends ChartHelper3D {

    /**
     * Constructor.
     */
    public Pie3DChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    @Override
    public ChartType getChartType()  { return ChartType.PIE_3D; }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        // return new AxisType[0];
        return new AxisType[] { AxisType.X, AxisType.Y, AxisType.Z };
    }

    /**
     * Creates an AxisView for given type.
     */
    protected AxisView createAxisView(AxisType anAxisType)
    {
        AxisView axisView = super.createAxisView(anAxisType);
        axisView.setVisible(false);
        axisView.setPickable(false);
        return axisView;
    }

    /**
     * Override to return placeholder intervals.
     */
    protected Intervals createIntervals(AxisView axisView)
    {
        return Intervals.getIntervalsSimple(0, 1);
    }

    /**
     * Returns a trace.
     */
    public Trace getTrace()
    {
        Content content = getContent();
        Trace[] traces = content.getEnabledTraces();
        if (traces.length == 0)
            traces = content.getTraces();
        return traces.length > 0 ? traces[0] : null;
    }

    /**
     * Creates the TraceViews.
     */
    protected TraceView[] createTraceViews()
    {
        Trace trace = getTrace();
        return new TraceView[] { new Pie3DTraceView(this, trace) };
    }

    @Override
    protected TraceView3D createProjectionTraceView()
    {
        Trace trace = getTrace();
        return new Pie3DTraceView(this, trace);
    }
}
