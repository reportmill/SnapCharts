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
        Trace[] traces = aChartView.getTraceList().getTraces();
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
     * Returns the ChartType.
     */
    @Override
    public ChartType getChartType()  { return ChartType.CONTOUR_3D; }

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
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        // Get Traces
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces();
        int traceCount = traces.length;

        // Iterate over traces and create DataAreas
        DataArea[] dataAreas = new DataArea[traceCount];
        for (int i = 0; i < traceCount; i++) {
            Trace trace = traces[i];
            dataAreas[i] = new Contour3DDataArea(this, trace, i == 0);
        }

        // Return DataAreas
        return dataAreas;
    }

    /**
     * Creates a DataArea3D for projections.
     */
    protected DataArea3D createProjectionDataArea()
    {
        TraceList traceList = getTraceList();
        Trace[] traces = traceList.getTraces();
        Trace trace = traces.length > 0 ? traces[0] : null;
        return trace != null ? new Contour3DDataArea(this, trace, true) : null;
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
