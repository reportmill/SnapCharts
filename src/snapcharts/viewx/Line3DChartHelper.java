package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

/**
 * A ChartHelper for 3D Line chart (ChartType.LINE_3D).
 */
public class Line3DChartHelper extends ChartHelper {

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
        // Get traces
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces();
        int traceCount = traces.length;

        // Create DataArea for each Trace
        DataArea[] dataAreas = new DataArea[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            dataAreas[i] = new Line3DDataArea(this, trace, i == 0);
        }

        // Return DataAreas
        return dataAreas;
    }

    /**
     * Override for chart type.
     */
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
}
