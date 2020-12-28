package snapcharts.views;
import snapcharts.model.*;

/**
 * A ChartHelper for ChartType PIE.
 */
public class ChartHelperPie extends ChartHelper {

    // Whether legend was showing
    private boolean  _showLegend;

    // The DataArea
    private DataAreaPie  _dataArea;

    /**
     * Constructor.
     */
    protected ChartHelperPie(ChartView aChartView)
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
        return new DataArea[] { _dataArea = new DataAreaPie(this, null) };
    }

    /**
     * Override to hide x/y axis and legend.
     */
    public void activate()
    {
        // Get info
        DataSetList dataSetList = _chartView.getDataSetList();
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
    public void reactivate()
    {
        DataSetList dset = _chartView.getDataSetList(); if (dset.getDataSetCount()==0 || dset.getPointCount()==0) return;
        DataPoint dp = dset.getDataSet(0).getPoint(0);
        _dataArea._disableMorph = true;
        _chartView.setSelDataPoint(dp);
        _dataArea._disableMorph = false;

        // Fix padding to accommodate bottom label, if needed
        _dataArea.fixPaddingForBottomLabelIfNeeded();
    }
}
