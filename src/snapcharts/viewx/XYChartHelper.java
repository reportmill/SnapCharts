package snapcharts.viewx;
import snapcharts.model.ChartType;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

import java.util.List;

/**
 * A ChartHelper for common XY ChartTypes: LINE, AREA, SCATTER.
 */
public class XYChartHelper extends ChartHelper {

    // The ChartType
    private ChartType  _chartType;

    /**
     * Constructor.
     */
    public XYChartHelper(ChartView aChartView, ChartType aChartType)
    {
        super(aChartView);
        _chartType = aChartType;
    }

    /**
     * Returns the type.
     */
    @Override
    public ChartType getChartType()  { return _chartType; }

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
            dataAreas[i] = new XYDataArea(this, dset);
        }

        return dataAreas;
    }
}
