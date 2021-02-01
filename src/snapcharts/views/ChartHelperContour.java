package snapcharts.views;
import snapcharts.model.ChartType;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import java.util.List;

/**
 * A ChartHelper for common Contour types.
 */
public class ChartHelperContour extends ChartHelper {

    /**
     * Constructor.
     */
    protected ChartHelperContour(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the ChartType.
     */
    @Override
    public ChartType getChartType()  { return ChartType.CONTOUR; }

    /**
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
        int dsetCount = dsets.size();

        DataArea[] dataAreas = new DataArea[dsetCount];
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dsets.get(i);
            dataAreas[i] = new DataAreaContour(this, dset);
        }

        return dataAreas;
    }
}
