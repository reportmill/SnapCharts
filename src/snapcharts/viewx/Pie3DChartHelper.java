package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

/**
 * A ChartHelper for ChartType PIE.
 */
public class Pie3DChartHelper extends PieChartHelper {

    /**
     * Constructor.
     */
    public Pie3DChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    @Override
    public ChartType getChartType()  { return ChartType.PIE_3D; }

    /**
     * Returns the AxisTypes.
     */
    @Override
    protected AxisType[] getAxisTypesImpl()  { return new AxisType[0]; }

    /**
     * Creates the DataAreas.
     */
    protected DataArea[] createDataAreas()
    {
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getEnabledDataSets();
        if (dataSets.length == 0)
            dataSets = dataSetList.getDataSets();
        if (dataSets.length == 0)
            return new DataArea[0];
        DataSet dset = dataSets[0];
        return new DataArea[] { _dataArea = new Pie3DDataArea(this, dset) };
    }
}
