/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.gfx.Color;
import snap.util.StringUtils;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.*;

/**
 * A class to manage UI to edit a TraceStyle.
 */
public class TraceAreaStyleInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public TraceAreaStyleInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Trace Area Settings"; }

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
        // Configure FillModeComboBox to show FillModes
        ComboBox<TraceStyle.FillMode> fillModeComboBox = getView("FillModeComboBox", ComboBox.class);
        fillModeComboBox.setItems(TraceStyle.FillMode.values());
        fillModeComboBox.setItemTextFunction(item -> StringUtils.fromCamelCase(item.toString()));
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Trace, TraceStyle
        Trace trace = getTrace(); if (trace == null) return;
        TraceStyle traceStyle = getTraceStyle(); if (traceStyle == null) return;

        // Reset ShowAreaCheckBox
        boolean showArea = trace.isShowArea();
        setViewValue("ShowAreaCheckBox", showArea);

        // Reset FillColorButton, FillColorResetButton
        setViewValue("FillColorButton", traceStyle.getFillColor());
        setViewVisible("FillColorResetButton", traceStyle.isFillSet());

        // Reset FillModeComboBox
        setViewSelItem("FillModeComboBox", traceStyle.getFillMode());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get TraceStyle
        Trace trace = getTrace(); if (trace == null) return;
        TraceStyle traceStyle = getTraceStyle(); if (traceStyle == null) return;

        // Handle ShowAreaCheckBox, FillModeComboBox
        if (anEvent.equals("ShowAreaCheckBox")) {
            boolean showArea = anEvent.getBoolValue();
            trace.setShowArea(showArea);
        }

        // Handle FillColorButton, FillColorResetButton
        if (anEvent.equals("FillColorButton")) {
            Color color = (Color) getViewValue("FillColorButton");
            color = color.getAlpha() <= .5 ? color : color.copyForAlpha(.5);
            traceStyle.setFill(color);
        }
        if (anEvent.equals("FillColorResetButton"))
            traceStyle.setFill(null);

        // Handle FillModeComboBox
        if (anEvent.equals("FillModeComboBox")) {
            TraceStyle.FillMode fillMode = (TraceStyle.FillMode) getViewSelItem("FillModeComboBox");
            traceStyle.setFillMode(fillMode);
        }
    }
}