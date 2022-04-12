/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snapcharts.model.Trace;
import snapcharts.view.ChartHelper;

/**
 * A DataArea3D subclass to display the contents of Contour3D chart.
 */
public class Contour3DDataArea extends DataArea3D {

    /**
     * Constructor.
     */
    public Contour3DDataArea(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace, isVisible);
    }

    /**
     * Override to return new Contour3DChartBuilder.
     */
    protected AxisBoxBuilder createChartBuilder()
    {
        return new Contour3DChartBuilder(this, _scene);
    }
}