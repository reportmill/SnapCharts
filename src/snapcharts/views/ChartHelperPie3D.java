package snapcharts.views;
import snapcharts.model.*;

/**
 * A ChartHelper for ChartType PIE.
 */
public class ChartHelperPie3D extends ChartHelperPie {

    /**
     * Constructor.
     */
    protected ChartHelperPie3D(ChartView aChartView)
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
        if (dataSetList.getDataSetCount() == 0)
            dataSetList = getDataSetListAll();
        if (dataSetList.getDataSetCount() == 0)
            return new DataArea[0];
        DataSet dset = dataSetList.getDataSet(0);
        return new DataArea[] { _dataArea = new DataAreaPie3D(this, dset) };
    }
}
