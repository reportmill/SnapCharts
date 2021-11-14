/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.app.ChartPane;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.DataSet;
import snapcharts.model.DataStyle;
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
    public ChartPart getChartPart()  { return getChart().getDataStyle(); }

    /**
     * Returns the PolarStyle.
     */
    public PolarStyle getPolarStyle()
    {
        ChartPart chartPart = _chartPane.getSelChartPart();
        if (chartPart instanceof DataSet) {
            DataSet dataSet = (DataSet) chartPart;
            DataStyle dataStyle = dataSet.getDataStyle();
            if (dataStyle instanceof PolarStyle)
                return (PolarStyle) dataStyle;
        }

        Chart chart = getChart();
        DataStyle dataStyle = chart.getDataStyle();
        if (dataStyle instanceof PolarStyle)
            return (PolarStyle) dataStyle;
        return null;
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