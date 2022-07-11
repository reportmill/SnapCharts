/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.props.PropChange;
import snapcharts.model.*;
import snapcharts.modelx.ContourStyle;
import snapcharts.view.*;

/**
 * A ChartHelper for common Contour types.
 */
public class Contour3DChartHelper extends ChartHelper3D {

    // An object to help with Contours
    protected ContourHelper  _contourHelper;

    /**
     * Constructor.
     */
    public Contour3DChartHelper(ChartView aChartView)
    {
        super(aChartView);

        // This is bogus
        Content content = aChartView.getContent();
        Trace[] traces = content.getTraces();
        for (Trace trace : traces) {
            TraceStyle traceStyle = trace.getTraceStyle();
            if (traceStyle instanceof ContourStyle) {
                _contourHelper = new ContourHelper(this, (ContourStyle) traceStyle);
                break;
            }
        }
        if (_contourHelper == null) {
            System.err.println("ContourChartHelper.init: No Contour Trace!");
        }
    }

    /**
     * Returns the TraceType.
     */
    @Override
    public TraceType getTraceType()  { return TraceType.Contour3D; }

    /**
     * Returns the ContourHelper.
     */
    public ContourHelper getContourHelper()  { return _contourHelper; }

    /**
     * Returns the ContourAxisView.
     */
    public ContourAxisView getContourAxisView()
    {
        return _chartView.getContourAxisView();
    }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        return new AxisType[] { AxisType.X, AxisType.Y, AxisType.Z };
        //return new AxisType[] { AxisType.X, AxisType.Y };
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
     * Creates the TraceView3Ds.
     */
    @Override
    protected TraceView[] createTraceViews()
    {
        // Get Traces
        Content content = getContent();
        Trace[] traces = content.getTraces();
        int traceCount = traces.length;

        // Iterate over traces and create TraceViews
        TraceView[] traceViews = new TraceView[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            traceViews[i] = new Contour3DTraceView(this, trace, i == 0);
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
        return trace != null ? new Contour3DTraceView(this, trace, true) : null;
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
