package snapcharts.doc;
import snapcharts.model.ChartPart;
import snapcharts.model.DataSet;

/**
 * A DocItem subclass to hold a chart.
 */
public class DocItemDataSet extends DocItem<DataSet> {

    /**
     * Constructor.
     */
    public DocItemDataSet(DataSet aDataSet)
    {
        super(aDataSet);
    }

    /**
     * Returns the DataSet.
     */
    public DataSet getDataSet()  { return _content; }

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
