package snapcharts.viewx;
import snapcharts.model.*;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import snapcharts.view.DataArea;

/**
 * A ChartHelper for ChartType PIE.
 */
public class PieChartHelper extends ChartHelper {

    // Whether legend was showing
    private boolean  _showLegend;

    // The DataArea
    protected PieDataArea _dataArea;

    /**
     * Constructor.
     */
    public PieChartHelper(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()  { return ChartType.PIE; }

    /**
     * Returns the AxisTypes.
     */
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
        return new DataArea[] { _dataArea = new PieDataArea(this, dset) };
    }

    /**
     * Override to hide x/y axis and legend.
     */
    public void activate()
    {
        // Get info
        DataSetList dataSetList = getDataSetListAll();
        int dsetCount = dataSetList.getDataSetCount();

        // Update parts
        Legend legend = getChart().getLegend();
        _showLegend = legend.isShowLegend();
        legend.setShowLegend(dsetCount>1);

        // If multiple datasets, make sure only first is enabled
        if (dsetCount>1) {
            dataSetList.getDataSet(0).setDisabled(false);
            for (int i=1; i<dsetCount; i++)
                dataSetList.getDataSet(i).setDisabled(true);
        }
    }

    /**
     * Override to restore x/y axis and legend.
     */
    public void deactivate()
    {
        // Reset Legend.ShowLegend
        Legend legend = getChart().getLegend();
        legend.setShowLegend(_showLegend);
    }

    /**
     * Override to select first data point.
     */
    public void resetView()
    {
        // Make sure all DataSet.AxisTypeY are just Y
        DataSetList dsetList = getDataSetList(); if (dsetList.getDataSetCount()==0) return;
        for (DataSet dset : dsetList.getDataSets().toArray(new DataSet[0]))
            dset.setAxisTypeY(AxisType.Y);
        dsetList = getDataSetList();

        // Do normal version
        super.resetView();

        // Update SelDataPoint
        DataSet dset = dsetList.getDataSet(0); if (dset.getPointCount()==0) return;
        DataPoint dp = dset.getPoint(0);
        _dataArea._disableMorph = true;
        _chartView.setSelDataPoint(dp);
        _dataArea._disableMorph = false;

        // Fix padding to accommodate bottom label, if needed
        _dataArea.fixPaddingForBottomLabelIfNeeded();
    }
}
