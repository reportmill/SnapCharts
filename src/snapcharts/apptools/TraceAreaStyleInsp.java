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
        return getTrace();
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
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Configure FillModeComboBox to show FillModes
        ComboBox<Trace.FillMode> fillModeComboBox = getView("FillModeComboBox", ComboBox.class);
        fillModeComboBox.setItems(Trace.FillMode.values());
        fillModeComboBox.setItemTextFunction(item -> StringUtils.fromCamelCase(item.toString()));
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Trace
        Trace trace = getTrace(); if (trace == null) return;

        // Reset ShowAreaCheckBox
        boolean showArea = trace.isShowArea();
        setViewValue("ShowAreaCheckBox", showArea);

        // Reset FillColorButton, FillColorResetButton
        setViewValue("FillColorButton", trace.getFillColor());
        setViewVisible("FillColorResetButton", !trace.isPropDefault(Trace.Fill_Prop));

        // Reset FillModeComboBox
        setViewSelItem("FillModeComboBox", trace.getFillMode());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Trace
        Trace trace = getTrace(); if (trace == null) return;

        // Handle ShowAreaCheckBox, FillModeComboBox
        if (anEvent.equals("ShowAreaCheckBox")) {
            boolean showArea = anEvent.getBoolValue();
            trace.setShowArea(showArea);
        }

        // Handle FillColorButton, FillColorResetButton
        if (anEvent.equals("FillColorButton")) {
            Color color = (Color) getViewValue("FillColorButton");
            color = color.getAlpha() <= .5 ? color : color.copyForAlpha(.5);
            trace.setFill(color);
        }
        if (anEvent.equals("FillColorResetButton"))
            trace.setFill(null);

        // Handle FillModeComboBox
        if (anEvent.equals("FillModeComboBox")) {
            Trace.FillMode fillMode = (Trace.FillMode) getViewSelItem("FillModeComboBox");
            trace.setFillMode(fillMode);
        }
    }
}