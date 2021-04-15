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
        DataSet[] dataSets = dataSetList.getEnabledDataSets();
        if (dataSets.length == 0)
            dataSets = dataSetList.getDataSets();
        if (dataSets.length == 0)
            return new DataArea[0];
        DataSet dset = dataSets[0];
        return new DataArea[] { _dataArea = new PieDataArea(this, dset) };
    }

    /**
     * Override to hide x/y axis and legend.
     */
    public void activate()
    {
        // Get info
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getDataSets();
        int dsetCount = dataSets.length;

        // Update parts
        Legend legend = getChart().getLegend();
        _showLegend = legend.isShowLegend();
        legend.setShowLegend(dsetCount>1);

        // If multiple datasets, make sure only first is enabled
        for (int i=1; i<dsetCount; i++)
            dataSets[i].setDisabled(i > 0);
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
        DataSetList dsetList = getDataSetList();
        DataSet[] dataSets = dsetList.getDataSets(); if (dataSets.length == 0) return;
        for (DataSet dset : dataSets)
            dset.setAxisTypeY(AxisType.Y);

        // Do normal version
        super.resetView();

        // Update SelDataPoint
        DataSet dset = dsetList.getDataSet(0);
        if (dset.getPointCount()==0) return;
        DataPoint dp = dset.getPoint(0);
        _dataArea._disableMorph = true;
        _chartView.setSelDataPoint(dp);
        _dataArea._disableMorph = false;

        // Fix padding to accommodate bottom label, if needed
        _dataArea.fixPaddingForBottomLabelIfNeeded();
    }
}
