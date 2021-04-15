package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

/**
 * A ChartHelper for 3D Line chart (ChartType.LINE_3D).
 */
public class Line3DChartHelper extends ChartHelper {

    /**
     * Constructor.
     */
    public Line3DChartHelper(ChartView aChartView)
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
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getDataSets();
        if (dataSets.length == 0)
            return new DataArea[0];
        DataSet dset = dataSets[0];
        return new DataArea[] { new Line3DDataArea(this, dset) };
    }

    /**
     * Override for chart type.
     */
    public void resetView()
    {
        // Make sure all DataSet.AxisTypeY are just Y
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getDataSets(); if (dataSets.length == 0) return;
        for (DataSet dset : dataSets)
            dset.setAxisTypeY(AxisType.Y);

        // Do normal version
        super.resetView();
    }
}
