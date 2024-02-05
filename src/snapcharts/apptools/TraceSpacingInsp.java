/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.charts.*;

/**
 * A class to manage UI to edit Trace.PointStyle spacing properties.
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
        return getPointStyle();
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
     * Returns the PointStyle.
     */
    public PointStyle getPointStyle()
    {
        Trace trace = getTrace();
        return trace != null ? trace.getPointStyle() : null;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get PointStyle
        PointStyle pointStyle = getPointStyle(); if (pointStyle == null) return;

        // Reset PointSpacingText, PointSpacingResetButton
        setViewValue("PointSpacingText", pointStyle.getPointSpacing());
        setViewVisible("PointSpacingResetButton", pointStyle.getPointSpacing() != PointStyle.DEFAULT_POINT_SPACING);

        // Reset SkipPointCountText, SkipPointCountResetButton
        setViewValue("SkipPointCountText", pointStyle.getSkipPointCount());
        setViewVisible("SkipPointCountResetButton", pointStyle.getSkipPointCount() != PointStyle.DEFAULT_SKIP_POINT_COUNT);

        // Reset MaxPointCountText, MaxPointCountResetButton
        setViewValue("MaxPointCountText", pointStyle.getMaxPointCount());
        setViewVisible("MaxPointCountResetButton", pointStyle.getMaxPointCount() != PointStyle.DEFAULT_MAX_POINT_COUNT);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get PointStyle
        PointStyle pointStyle = getPointStyle(); if (pointStyle == null) return;

        // Handle PointSpacingText, PointSpacingAdd1Button, PointSpacingSub1Button, PointSpacingResetButton
        if (anEvent.equals("PointSpacingText"))
            pointStyle.setPointSpacing(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("PointSpacingAdd1Button"))
            pointStyle.setPointSpacing(pointStyle.getPointSpacing() + 1);
        if (anEvent.equals("PointSpacingSub1Button"))
            pointStyle.setPointSpacing(Math.max(pointStyle.getPointSpacing() - 1, 0));
        if (anEvent.equals("PointSpacingResetButton"))
            pointStyle.setPointSpacing(PointStyle.DEFAULT_POINT_SPACING);

        // Handle SkipPointCountText, SkipPointCountAdd1Button, SkipPointCountSub1Button, SkipPointCountResetButton
        if (anEvent.equals("SkipPointCountText"))
            pointStyle.setSkipPointCount(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("SkipPointCountAdd1Button"))
            pointStyle.setSkipPointCount(pointStyle.getSkipPointCount() + 1);
        if (anEvent.equals("SkipPointCountSub1Button"))
            pointStyle.setSkipPointCount(Math.max(pointStyle.getSkipPointCount() - 1, 0));
        if (anEvent.equals("SkipPointCountResetButton"))
            pointStyle.setSkipPointCount(PointStyle.DEFAULT_SKIP_POINT_COUNT);

        // Handle MaxPointCountText, MaxPointCountAdd1Button, MaxPointCountSub1Button, MaxPointCountResetButton
        if (anEvent.equals("MaxPointCountText"))
            pointStyle.setMaxPointCount(Math.max(anEvent.getIntValue(), 0));
        if (anEvent.equals("MaxPointCountAdd1Button"))
            pointStyle.setMaxPointCount(pointStyle.getMaxPointCount() + 1);
        if (anEvent.equals("MaxPointCountSub1Button"))
            pointStyle.setMaxPointCount(Math.max(pointStyle.getMaxPointCount() - 1, 0));
        if (anEvent.equals("MaxPointCountResetButton"))
            pointStyle.setMaxPointCount(PointStyle.DEFAULT_MAX_POINT_COUNT);
    }
}