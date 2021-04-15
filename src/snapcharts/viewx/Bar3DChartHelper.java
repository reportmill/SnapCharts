package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.*;

/**
 * A ChartHelper for ChartType BAR_3D.
 */
public class Bar3DChartHelper extends ChartHelper {

    /**
     * Constructor.
     */
    public Bar3DChartHelper(ChartView aChartView)
    {
        super(aChartView);

        // Hide axes
        for (AxisView axisView : getAxisViews())
            axisView.setVisible(false);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.BAR_3D; }

    /**
     * Returns the AxisTypes.
     */
    protected AxisType[] getAxisTypesImpl()
    {
        return new AxisType[] { AxisType.X, AxisType.Y };
    }

    /**
     * Creates an AxisView for given type.
     */
    protected AxisView createAxisView(AxisType anAxisType)
    {
        AxisView axisView = super.createAxisView(anAxisType);
        axisView.setVisible(false);
        return axisView;
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
        return new DataArea[] { new Bar3DDataArea(this, dset) };
    }

    /**
     * Override for chart type.
     */
    @Override
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

    /**
     * Override to reset view transform.
     */
    @Override
    public void resetAxesAnimated()
    {
        DataArea[] dataAreas = getDataAreas();
        for (DataArea dataArea : dataAreas)
            if (dataArea instanceof Bar3DDataArea)
                ((Bar3DDataArea)dataArea).resetViewMatrixAnimated();
    }
}
