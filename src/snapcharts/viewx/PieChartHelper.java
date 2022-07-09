package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

/**
 * A ChartHelper for ChartType PIE.
 */
public class PieChartHelper extends ChartHelper {

    // Whether legend was showing
    private boolean  _showLegend;

    // The DataArea
    protected PieDataArea _dataArea;

    /**
     * Constructor.
     */
    public PieChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.PIE; }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()  { return new AxisType[0]; }

    /**
     * Creates the DataAreas.
     */
    protected DataArea[] createDataAreas()
    {
        Content content = getContent();
        Trace[] traces = content.getEnabledTraces();
        if (traces.length == 0)
            traces = content.getTraces();
        if (traces.length == 0)
            return new DataArea[0];
        Trace trace = traces[0];
        return new DataArea[] { _dataArea = new PieDataArea(this, trace) };
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
        _dataArea._disableMorph = true;
        _chartView.setSelDataPoint(tracePoint);
        _dataArea._disableMorph = false;

        // Fix padding to accommodate bottom label, if needed
        _dataArea.fixPaddingForBottomLabelIfNeeded();
    }
}
