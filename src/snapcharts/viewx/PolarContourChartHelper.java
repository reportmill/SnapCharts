package snapcharts.viewx;
import snap.props.PropChange;
import snapcharts.model.*;
import snapcharts.modelx.ContourStyle;
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
            TraceStyle traceStyle = trace.getTraceStyle();
            if (traceStyle instanceof ContourStyle) {
                _contourHelper = new ContourHelper(this, (ContourStyle) traceStyle);
                break;
            }
        }
        if (_contourHelper == null) {
            System.err.println("PolarContourChartHelper.init: No Contour Trace!");
        }
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.POLAR_CONTOUR; }

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
        if (src instanceof Trace || src instanceof Content || src instanceof TraceStyle || src instanceof ContourAxis) {
            _contourHelper.resetCachedValues();
        }
    }
}
