package snapcharts.model;

import snapcharts.app.ChartPane;
import snapcharts.app.DocItemPane;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemChart extends DocItem {

    // The Chart
    private Chart  _chart;

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
            addDataSet(dset);
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Override to return Chart name.
     */
    @Override
    public String getName()
    {
        return getChart().getName();
    }

    /**
     * Creates the ItemPane.
     */
    @Override
    protected DocItemPane createItemPane()
    {
        ChartPane pane = new ChartPane();
        pane.setChart(getChart());
        return pane;
    }

    /**
     * Adds a DataSet.
     */
    public DocItemDataSet addDataSet(DataSet aDataSet)
    {
        DocItemDataSet dataSetItem = new DocItemDataSet(aDataSet);
        addItem(dataSetItem);
        return dataSetItem;
    }
}
