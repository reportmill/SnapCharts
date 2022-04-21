/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snapcharts.model.Axis;
import snapcharts.model.AxisType;
import snapcharts.model.ChartPart;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartPartView;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

/**
 * This ChartHelper subclass adds support for 3D.
 */
public abstract class ChartHelper3D extends ChartHelper {

    /**
     * Constructor.
     */
    protected ChartHelper3D(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the DataArea3D.
     */
    public DataArea3D getDataArea3D()
    {
        DataArea[] dataAreas = getDataAreas();
        return dataAreas.length > 0 && dataAreas[0] instanceof DataArea3D ? (DataArea3D) dataAreas[0] : null;
    }

    /**
     * Override to AxisProxy for Axis.
     */
    public ChartPartView getChartPartViewForPart(ChartPart aChartPart)
    {
        // Handle Axis: Return appropriate AxisProxy
        if (aChartPart instanceof Axis) {
            Axis axis = (Axis) aChartPart;
            AxisType axisType = axis.getType();
            DataArea3D dataArea3D = getDataArea3D();
            if (axisType.isX())
                return dataArea3D._axisProxyX;
            if (axisType.isAnyY())
                return dataArea3D._axisProxyY;
            if (axisType.isZ())
                return dataArea3D._axisProxyZ;
        }

        // Do normal version
        return super.getChartPartViewForPart(aChartPart);
    }
}
