/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.charts.ChartPart;
import snapcharts.charts.Trace;
import snapcharts.charts.traces.PolarTrace;

/**
 * A class to manage UI to edit a PolarTrace.
 */
public class PolarTraceInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public PolarTraceInsp(ChartPane aChartPane)
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
    public ChartPart getChartPart()  { return getPolarTrace(); }

    /**
     * Returns the Trace.
     */
    public Trace getTrace()
    {
        ChartPart selPart = _chartPane.getSelChartPart();
        return selPart instanceof Trace ? (Trace) selPart : null;
    }

    /**
     * Returns the PolarTrace.
     */
    public PolarTrace getPolarTrace()
    {
        Trace trace = getTrace();
        return trace instanceof PolarTrace ? (PolarTrace) trace : null;
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get PolarTrace
        PolarTrace polarTrace = getPolarTrace(); if (polarTrace == null) return;

        // Reset ThetaUnitComboBox
        //setViewValue("ThetaUnitComboBox", polarTrace.getThetaUnit());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get PolarTrace
        PolarTrace polarTrace = getPolarTrace(); if (polarTrace == null) return;

        // Handle ThetaUnitComboBox
        //if (anEvent.equals("ThetaUnitComboBox"))
        //    polarTrace.setThetaUnit((PolarTrace.ThetaUnit) anEvent.getSelItem());
    }
}