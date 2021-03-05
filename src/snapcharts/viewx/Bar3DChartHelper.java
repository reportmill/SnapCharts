package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

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
        Chart chart = getChart();
        DataSetList dataSetList = chart.getDataSetList();
        if (dataSetList.getDataSetCount() == 0)
            return new DataArea[0];
        DataSet dset = dataSetList.getDataSet(0);
        return new DataArea[] { new Bar3DDataArea(this, dset) };
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
