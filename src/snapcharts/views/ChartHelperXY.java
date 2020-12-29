package snapcharts.views;
import snapcharts.model.ChartType;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import java.util.List;

/**
 * A ChartHelper for common XY ChartTypes: LINE, AREA, SCATTER.
 */
public class ChartHelperXY extends ChartHelper {

    // The ChartType
    private ChartType  _chartType;

    /**
     * Constructor.
     */
    public ChartHelperXY(ChartView aChartView, ChartType aChartType)
    {
        super(aChartView);
        _chartType = aChartType;
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return _chartType; }

    /**
     * Creates the DataAreas.
     */
    protected DataArea[] createDataAreas()
    {
        ChartView chartView = getChartView();
        DataSetList dataSetList = chartView.getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
        int dsetCount = dsets.size();

        DataArea[] dataAreas = new DataArea[dsetCount];
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dsets.get(i);
            dataAreas[i] = new DataAreaXY(this, dset);
        }

        return dataAreas;
    }
}
