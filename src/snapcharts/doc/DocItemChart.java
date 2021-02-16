package snapcharts.doc;
import snap.util.PropChange;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemChart extends DocItem {

    // The Chart
    private Chart _chart;

    /**
     * Constructor.
     */
    public DocItemChart(Chart aChart)
    {
        // Set Chart
        _chart = aChart;

        // Add Items for DataSets
        DataSetList dsetList = _chart.getDataSetList();
        for (DataSet dset : dsetList.getDataSets())
            addItem(new DocItemDataSet(dset));

        // Start listening to prop changes
        _chart.addPropChangeListener(aPC -> chartDidPropChange(aPC));
        _chart.addDeepChangeListener((obj,aPC) -> chartDidPropChange(aPC));
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Override to return true.
     */
    public boolean isParent()  { return true; }

    /**
     * Override to return Chart name.
     */
    @Override
    public String getName()
    {
        return getChart().getName();
    }

    /**
     * Override to return Chart.
     */
    @Override
    public ChartPart getChartPart()  { return getChart(); }

    /**
     * Override to accept DataSets.
     */
    public DocItem addChartPart(ChartPart aChartPart, DocItem aChildItem)
    {
        // Handle add DataSet
        if (aChartPart instanceof DataSet) {
            DataSet dset = (DataSet)aChartPart;
            int ind = aChildItem!=null ? aChildItem.getIndex() + 1 : getItemCount();
            getChart().getDataSetList().addDataSet(dset, ind);
            return getItem(ind);
        }

        // Otherwise, try parent
        return super.addChartPart(aChartPart, aChildItem);
    }

    /**
     * Called when PropChanges.
     */
    private void chartDidPropChange(PropChange aPC)
    {
        // Get property name
        String propName = aPC.getPropName();

        // Handle DataSet add/remove
        if (propName==DataSetList.DataSet_Prop) {
            int ind = aPC.getIndex(); if (ind<0) return;
            DataSet newVal = (DataSet)aPC.getNewValue();
            if (newVal!=null)
                addItem(new DocItemDataSet(newVal));
            else removeItem(ind);
        }
    }
}
