package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.TraceView;

/**
 * A ChartHelper for TraceType Pie.
 */
public class PieChartHelper extends ChartHelper {

    // Whether legend was showing
    private boolean  _showLegend;

    // The PieTraceView
    protected PieTraceView  _traceView;

    /**
     * Constructor.
     */
    public PieChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the trace type.
     */
    public TraceType getTraceType()  { return TraceType.Pie; }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()  { return new AxisType[0]; }

    /**
     * Creates the TraceViews.
     */
    protected TraceView[] createTraceViews()
    {
        Content content = getContent();
        Trace[] traces = content.getEnabledTraces();
        if (traces.length == 0)
            traces = content.getTraces();
        if (traces.length == 0)
            return new TraceView[0];
        Trace trace = traces[0];
        return new TraceView[] { _traceView = new PieTraceView(this, trace) };
    }

    /**
     * Override to hide x/y axis and legend.
     */
    public void activate()
    {
        // Get info
        Content content = getContent();
        Trace[] traces = content.getTraces();
        int traceCount = traces.length;

        // Update parts
        Legend legend = getChart().getLegend();
        _showLegend = legend.isShowLegend();
        legend.setShowLegend(traceCount>1);

        // If multiple traces, make sure only first is enabled
        for (int i = 1; i < traceCount; i++)
            traces[i].setDisabled(i > 0);
    }

    /**
     * Override to restore x/y axis and legend.
     */
    public void deactivate()
    {
        // Reset Legend.ShowLegend
        Legend legend = getChart().getLegend();
        legend.setShowLegend(_showLegend);
    }

    /**
     * Override to select first data point.
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

        // Update SelDataPoint
        Trace trace = content.getTrace(0);
        if (trace.getPointCount()==0) return;
        TracePoint tracePoint = trace.getPoint(0);
        _traceView._disableMorph = true;
        _chartView.setSelDataPoint(tracePoint);
        _traceView._disableMorph = false;

        // Fix padding to accommodate bottom label, if needed
        _traceView.fixPaddingForBottomLabelIfNeeded();
    }
}
