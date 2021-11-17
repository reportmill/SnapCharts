/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.*;

/**
 * A class to manage UI to edit a TraceStyle.
 */
public class TraceSpacingInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public TraceSpacingInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Trace Spacing Settings"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()
    {
        return getTraceStyle();
    }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()
    {
        ChartPart selPart = _chartPane.getSelChartPart();
        return selPart instanceof Trace ? (Trace) selPart : null;
    }

    /**
     * Returns the TraceStyle.
     */
    public TraceStyle getTraceStyle()
    {
        Trace trace = getTrace();
        if (trace != null)
            return trace.getTraceStyle();

        return getChart().getTraceStyle();
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get TraceStyle
        TraceStyle traceStyle = getTraceStyle(); if (traceStyle == null) return;

        // Reset PointSpacingText, PointSpacingResetButton
        setViewValue("PointSpacingText", traceStyle.getPointSpacing());
        setViewVisible("PointSpacingResetButton", traceStyle.getPointSpacing() != TraceStyle.DEFAULT_POINT_SPACING);

        // Reset SkipPointCountText, SkipPointCountResetButton
        setViewValue("SkipPointCountText", traceStyle.getSkipPointCount());
        setViewVisible("SkipPointCountResetButton", traceStyle.getSkipPointCount() != TraceStyle.DEFAULT_SKIP_POINT_COUNT);

        // Reset MaxPointCountText, MaxPointCountResetButton
        setViewValue("MaxPointCountText", traceStyle.getMaxPointCount());
        setViewVisible("MaxPointCountResetButton", traceStyle.getMaxPointCount() != TraceStyle.DEFAULT_MAX_POINT_COUNT);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get TraceStyle
        TraceStyle traceStyle = getTraceStyle(); if (traceStyle == null) return;

        // Handle PointSpacingText, PointSpacingAdd1Button, PointSpacingSub1Button, PointSpacingResetButton
        if (anEvent.equals("PointSpacingText"))
            traceStyle.setPointSpacing(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("PointSpacingAdd1Button"))
            traceStyle.setPointSpacing(traceStyle.getPointSpacing() + 1);
        if (anEvent.equals("PointSpacingSub1Button"))
            traceStyle.setPointSpacing(Math.max(traceStyle.getPointSpacing() - 1, 0));
        if (anEvent.equals("PointSpacingResetButton"))
            traceStyle.setPointSpacing(TraceStyle.DEFAULT_POINT_SPACING);

        // Handle SkipPointCountText, SkipPointCountAdd1Button, SkipPointCountSub1Button, SkipPointCountResetButton
        if (anEvent.equals("SkipPointCountText"))
            traceStyle.setSkipPointCount(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("SkipPointCountAdd1Button"))
            traceStyle.setSkipPointCount(traceStyle.getSkipPointCount() + 1);
        if (anEvent.equals("SkipPointCountSub1Button"))
            traceStyle.setSkipPointCount(Math.max(traceStyle.getSkipPointCount() - 1, 0));
        if (anEvent.equals("SkipPointCountResetButton"))
            traceStyle.setSkipPointCount(TraceStyle.DEFAULT_SKIP_POINT_COUNT);

        // Handle MaxPointCountText, MaxPointCountAdd1Button, MaxPointCountSub1Button, MaxPointCountResetButton
        if (anEvent.equals("MaxPointCountText"))
            traceStyle.setMaxPointCount(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("MaxPointCountAdd1Button"))
            traceStyle.setMaxPointCount(traceStyle.getMaxPointCount() + 1);
        if (anEvent.equals("MaxPointCountSub1Button"))
            traceStyle.setMaxPointCount(Math.max(traceStyle.getMaxPointCount() - 1, 0));
        if (anEvent.equals("MaxPointCountResetButton"))
            traceStyle.setMaxPointCount(TraceStyle.DEFAULT_MAX_POINT_COUNT);
    }
}