package snapcharts.viewx;
import snap.props.PropChange;
import snapcharts.model.*;
import snapcharts.modelx.ContourTrace;
import snapcharts.view.*;

/**
 * A (Polar)ChartHelper subclass for PolarContour charts.
 */
public class PolarContourChartHelper extends PolarChartHelper {

    // An object to help with Contours
    protected ContourHelper  _contourHelper;

    /**
     * Constructor.
     */
    public PolarContourChartHelper(ChartView aChartView)
    {
        super(aChartView);

        // This is bogus
        Trace[] traces = aChartView.getContent().getTraces();
        for (Trace trace : traces) {
            if (trace instanceof ContourTrace) {
                _contourHelper = new ContourHelper(this, (ContourTrace) trace);
                break;
            }
        }
        if (_contourHelper == null) {
            System.err.println("PolarContourChartHelper.init: No Contour Trace!");
        }
    }

    /**
     * Returns the trace type.
     */
    public TraceType getTraceType()  { return TraceType.PolarContour; }

    /**
     * Returns the ContourHelper.
     */
    public ContourHelper getContourHelper()  { return _contourHelper; }

    /**
     * Creates the TraceViews.
     */
    @Override
    protected TraceView[] createTraceViews()
    {
        Content content = getContent();
        Trace[] traces = content.getTraces();
        int traceCount = traces.length;

        TraceView[] traceViews = new TraceView[traceCount];
        for (int i=0; i<traceCount; i++) {
            Trace trace = traces[i];
            traceViews[i] = new PolarContourTraceView(this, trace);
        }

        return traceViews;
    }

    /**
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Trace/Content change
        Object src = aPC.getSource();
        if (src instanceof Trace || src instanceof Content || src instanceof ContourAxis) {
            _contourHelper.resetCachedValues();
        }
    }
}
