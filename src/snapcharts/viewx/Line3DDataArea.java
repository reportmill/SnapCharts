/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;

/**
 * A DataArea3D subclass to display the contents of Line3D chart.
 */
public class Line3DDataArea extends DataArea3D {

    /**
     * Constructor.
     */
    public Line3DDataArea(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace, isVisible);
    }

    /**
     * Override to return new Line3DChartBuilder.
     */
    protected AxisBoxBuilder createChartBuilder()
    {
        return new Line3DChartBuilder(this, _scene);
    }
}