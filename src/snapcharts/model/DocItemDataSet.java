package snapcharts.model;

import snapcharts.app.ChartPane;
import snapcharts.app.DocItemPane;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemDataSet extends DocItem {

    // The DataSet
    private DataSet  _dset;

    /**
     * Constructor.
     */
    public DocItemDataSet(DataSet aDataSet)
    {
        _dset = aDataSet;
    }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _dset; }

    /**
     * Override to return DataSet name.
     */
    @Override
    public String getName()
    {
        return getDataSet().getName();
    }

    /**
     * Override to return false.
     */
    public boolean isParent()  { return false; }

    /**
     * Override to return DataSet.
     */
    @Override
    public ChartPart getChartPart()  { return getDataSet(); }

    /**
     * Creates the ItemPane.
     */
    @Override
    protected DocItemPane createItemPane()
    {
        ChartPane pane = new ChartPane();
        pane.setDataSet(getDataSet());
        return pane;
    }
}
