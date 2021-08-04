/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Painter;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

/**
 * A ChartHelper for ChartType BAR.
 */
public class BarChartHelper extends ChartHelper {

    /**
     * Constructor.
     */
    public BarChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.BAR; }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        return new AxisType[] { AxisType.X, AxisType.Y };
    }

    /**
     * Creates the DataAreas.
     */
    protected DataArea[] createDataAreas()
    {
        DataSetList dataSetList = getDataSetList();
        if (dataSetList.getDataSetCount() == 0)
            return new DataArea[0];
        DataSet dset = dataSetList.getDataSet(0);
        return new DataArea[] { new BarDataArea(this, dset) };
    }

    /**
     * Paints chart axis lines.
     */
    @Override
    public void paintGridlines(Painter aPntr)
    {
        XYChartHelper.paintGridlinesXY(this, aPntr);
    }

    /**
     * Paints chart border.
     */
    @Override
    public void paintBorder(Painter aPntr)
    {
        XYChartHelper.paintBorderXY(this, aPntr);
    }

    /**
     * Paints chart border.
     */
    @Override
    public void paintAxesLinesAndTicks(Painter aPntr)
    {
        XYChartHelper.paintAxesLinesAndTicksXY(this, aPntr);
    }

    /**
     * Override for chart type.
     */
    public void resetView()
    {
        // Make sure all DataSet.AxisTypeY are just Y
        DataSetList dsetList = getDataSetList();
        DataSet[] dataSets = dsetList.getDataSets(); if (dataSets.length == 0) return;
        for (DataSet dset : dataSets)
            dset.setAxisTypeY(AxisType.Y);

        // Do normal version
        super.resetView();
    }
}
