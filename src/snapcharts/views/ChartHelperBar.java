package snapcharts.views;
import snapcharts.model.*;

/**
 * A ChartHelper for ChartType BAR.
 */
public class ChartHelperBar extends ChartHelper {

    /**
     * Constructor.
     */
    protected ChartHelperBar(ChartView aChartView)
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
        Chart chart = getChart();
        DataSetList dataSetList = chart.getDataSetList();
        if (dataSetList.getDataSetCount() == 0)
            return new DataArea[0];
        DataSet dset = dataSetList.getDataSet(0);
        return new DataArea[] { new DataAreaBar(this, dset) };
    }
}
