/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.ChartPart;
import snapcharts.model.Trace;
import snapcharts.model.TraceStyle;
import snapcharts.modelx.PolarStyle;

/**
 * A class to manage UI to edit a ContourStyle.
 */
public class PolarStyleInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public PolarStyleInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Polar Style"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getPolarStyle(); }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()
    {
        ChartPart selPart = _chartPane.getSelChartPart();
        return selPart instanceof Trace ? (Trace) selPart : null;
    }

    /**
     * Returns the PolarStyle.
     */
    public PolarStyle getPolarStyle()
    {
        Trace trace = getTrace();
        TraceStyle traceStyle = trace != null ? trace.getTraceStyle() : null;
        return traceStyle instanceof PolarStyle ? (PolarStyle) traceStyle : null;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get PolarStyle
        PolarStyle polarStyle = getPolarStyle(); if (polarStyle == null) return;

        // Reset ThetaUnitComboBox
        //setViewValue("ThetaUnitComboBox", polarStyle.getThetaUnit());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get PolarStyle
        PolarStyle polarStyle = getPolarStyle(); if (polarStyle == null) return;

        // Handle ThetaUnitComboBox
        //if (anEvent.equals("ThetaUnitComboBox"))
        //    polarStyle.setThetaUnit((PolarStyle.ThetaUnit) anEvent.getSelItem());
    }
}