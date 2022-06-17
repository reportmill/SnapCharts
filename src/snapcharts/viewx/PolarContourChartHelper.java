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
        Trace[] traces = aChartView.getTraceList().getTraces();
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
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces();
        int traceCount = traces.length;

        DataArea[] dataAreas = new DataArea[traceCount];
        for (int i=0; i<traceCount; i++) {
            Trace trace = traces[i];
            dataAreas[i] = new PolarContourDataArea(this, trace);
        }

        return dataAreas;
    }

    /**
     * Called when a ChartPart changes.
     */
    @Override
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle Trace/TraceList change
        Object src = aPC.getSource();
        if (src instanceof Trace || src instanceof TraceList || src instanceof TraceStyle || src instanceof ContourAxis) {
            _contourHelper.resetCachedValues();
        }
    }
}
