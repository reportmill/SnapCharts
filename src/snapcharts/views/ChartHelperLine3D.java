package snapcharts.views;
import snapcharts.model.*;

/**
 * A ChartHelper for 3D Line chart (ChartType.LINE_3D).
 */
public class ChartHelperLine3D extends ChartHelper {

    /**
     * Constructor.
     */
    protected ChartHelperLine3D(ChartView aChartView)
    {
        super(aChartView);

        // Hide axes
        for (AxisView axisView : getAxisViews())
            axisView.setVisible(false);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.LINE_3D; }

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
        Chart chart = getChart();
        DataSetList dataSetList = chart.getDataSetList();
        if (dataSetList.getDataSetCount() == 0)
            return new DataArea[0];
        DataSet dset = dataSetList.getDataSet(0);
        return new DataArea[] { new DataAreaLine3D(this, dset) };
    }

    /**
     * Override for chart type.
     */
    public void resetView()
    {
        // Make sure all DataSet.AxisTypeY are just Y
        DataSetList dsetList = getDataSetList();
        if (dsetList.getDataSetCount() == 0) return;
        for (DataSet dset : dsetList.getDataSets())
            dset.setAxisTypeY(AxisType.Y);

        // Do normal version
        super.resetView();
    }
}
