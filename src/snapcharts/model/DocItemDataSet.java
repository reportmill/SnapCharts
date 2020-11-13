package snapcharts.model;

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
     * Override to return DataSet.
     */
    @Override
    public ChartPart getChartPart()  { return getDataSet(); }
}
